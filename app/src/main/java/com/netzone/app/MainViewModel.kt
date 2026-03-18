package com.netzone.app

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: RuleRepository,
    private val packageManager: PackageManager,
    private val appMetadataDao: AppMetadataDao,
    private val preferenceManager: PreferenceManager,
    private val logDao: LogDao
) : ViewModel() {
    val rules: Flow<List<Rule>> = repository.getAllRulesFlow()
    
    val searchQuery = MutableStateFlow("")
    val showOnlyBlocked = MutableStateFlow(false)
    val showOnlySystem = MutableStateFlow(true)
    val sortMode: Flow<AppSortMode> = preferenceManager.appSortMode

    private data class FilterState(
        val query: String,
        val blockedOnly: Boolean,
        val systemOnly: Boolean,
        val mode: AppSortMode,
        val recentPackages: List<String>
    )

    // Reactive filtering pipeline
    val filteredApps: StateFlow<List<AppMetadata>> = combine(
        appMetadataDao.getAllApps(),
        combine(
            searchQuery,
            showOnlyBlocked,
            showOnlySystem,
            sortMode,
            logDao.getRecentPackageNames(System.currentTimeMillis() - 10 * 60 * 1000)
        ) { query, blocked, system, mode, recent ->
            FilterState(query, blocked, system, mode, recent)
        },
        repository.rulesMap
    ) { apps, filters, rulesMap ->
        apps.filter { app ->
            val matchesSearch = filters.query.isEmpty() || app.name.contains(filters.query, ignoreCase = true) || 
                             app.packageName.contains(filters.query, ignoreCase = true)
            val matchesSystem = filters.systemOnly || !app.isSystem
            
            val rule = rulesMap[app.packageName]
            val isBlocked = rule != null && (rule.wifiBlocked || rule.mobileBlocked || rule.isScheduleEnabled)
            val matchesBlocked = !filters.blockedOnly || isBlocked
            
            matchesSearch && matchesSystem && matchesBlocked
        }.let { filtered ->
            when (filters.mode) {
                AppSortMode.NAME -> filtered.sortedBy { it.name }
                AppSortMode.UID -> filtered.sortedBy { it.uid }
                AppSortMode.SMART -> {
                    sortSmart(filtered, rulesMap, filters.recentPackages)
                }
            }
        }
    }.flowOn(Dispatchers.Default)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    companion object {
        fun sortSmart(
            apps: List<AppMetadata>,
            rulesMap: Map<String, Rule>,
            recentPackages: List<String>
        ): List<AppMetadata> {
            return apps.sortedWith(
                compareByDescending<AppMetadata> { app ->
                    val rule = rulesMap[app.packageName]
                    rule != null && (rule.wifiBlocked || rule.mobileBlocked || rule.isScheduleEnabled)
                }.thenByDescending { app ->
                    recentPackages.contains(app.packageName)
                }.thenBy { it.name }
            )
        }
    }

    init {
        syncAppsInBackground()
        
        // Synchronize showOnlySystem with preferenceManager
        viewModelScope.launch {
            preferenceManager.manageSystemApps.collect {
                showOnlySystem.value = it
            }
        }
    }

    private fun syncAppsInBackground() {
        viewModelScope.launch(Dispatchers.IO) {
            val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
                .map { appInfo ->
                    AppMetadata(
                        packageName = appInfo.packageName,
                        name = packageManager.getApplicationLabel(appInfo).toString(),
                        uid = appInfo.uid,
                        isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                    )
                }
                .sortedBy { it.name }
            
            val currentApps = appMetadataDao.getAllApps().first()
            
            val hasChanges = installedApps.size != currentApps.size || 
                installedApps.zip(currentApps).any { (new, old) -> 
                    new.packageName != old.packageName || 
                    new.name != old.name || 
                    new.uid != old.uid || 
                    new.isSystem != old.isSystem 
                }

            if (hasChanges) {
                appMetadataDao.deleteAll()
                appMetadataDao.insertApps(installedApps)
            }
        }
    }

    fun onSearchQueryChange(newQuery: String) {
        searchQuery.value = newQuery
    }

    fun toggleFilterBlocked() {
        showOnlyBlocked.value = !showOnlyBlocked.value
    }

    fun toggleFilterSystem() {
        viewModelScope.launch {
            preferenceManager.setManageSystemApps(!showOnlySystem.value)
        }
    }

    fun updateRule(rule: Rule) {
        viewModelScope.launch {
            repository.updateRule(rule)
        }
    }

    fun setSortMode(mode: AppSortMode) {
        viewModelScope.launch {
            preferenceManager.setAppSortMode(mode)
        }
    }
}
