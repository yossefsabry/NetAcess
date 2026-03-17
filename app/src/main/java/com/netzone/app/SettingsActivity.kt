package com.netzone.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val preferenceManager = PreferenceManager(this)

        setContent {
            val isDarkMode by preferenceManager.isDarkMode.collectAsStateWithLifecycle(initialValue = false)
            
            NetZoneTheme(isDark = isDarkMode) {
                SettingsScreen(
                    preferenceManager = preferenceManager,
                    onBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(preferenceManager: PreferenceManager, onBack: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    val manageSystemApps by preferenceManager.manageSystemApps.collectAsStateWithLifecycle(initialValue = false)
    val blockWhenScreenOff by preferenceManager.blockWhenScreenOff.collectAsStateWithLifecycle(initialValue = false)
    val isDarkMode by preferenceManager.isDarkMode.collectAsStateWithLifecycle(initialValue = false)
    val customDns by preferenceManager.customDns.collectAsStateWithLifecycle(initialValue = "8.8.8.8")

    var showDnsDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text("General", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            
            SettingsToggleItem(
                title = "Dark Mode",
                subtitle = "Use dark theme across the app",
                checked = isDarkMode,
                onCheckedChange = {
                    coroutineScope.launch {
                        preferenceManager.setDarkMode(it)
                    }
                }
            )
            
            SettingsToggleItem(
                title = "Manage System Apps",
                subtitle = "Show and manage firewall rules for system applications",
                checked = manageSystemApps,
                onCheckedChange = {
                    coroutineScope.launch {
                        preferenceManager.setManageSystemApps(it)
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            Text("Network", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))

            SettingsClickItem(
                title = "Custom DNS",
                subtitle = "Currently: $customDns",
                onClick = { showDnsDialog = true }
            )

            SettingsToggleItem(
                title = "Block when screen is off",
                subtitle = "Experimental: block all traffic when screen is turned off",
                checked = blockWhenScreenOff,
                onCheckedChange = {
                    coroutineScope.launch {
                        preferenceManager.setBlockWhenScreenOff(it)
                    }
                }
            )
        }
    }

    if (showDnsDialog) {
        var dnsInput by remember { mutableStateOf(customDns) }
        AlertDialog(
            onDismissRequest = { showDnsDialog = false },
            title = { Text("Set Custom DNS") },
            text = {
                OutlinedTextField(
                    value = dnsInput,
                    onValueChange = { dnsInput = it },
                    label = { Text("DNS Server") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    coroutineScope.launch {
                        preferenceManager.setCustomDns(dnsInput)
                    }
                    showDnsDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDnsDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SettingsToggleItem(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun SettingsClickItem(title: String, subtitle: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = androidx.compose.ui.graphics.Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(vertical = 12.dp)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
