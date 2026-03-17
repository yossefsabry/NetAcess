package com.netzone.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class LogActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = AppDatabase.getDatabase(this)
        val logDao = db.logDao()
        val preferenceManager = PreferenceManager(this)

        setContent {
            val isDarkMode by preferenceManager.isDarkMode.collectAsStateWithLifecycle(initialValue = false)
            
            NetZoneTheme(isDark = isDarkMode) {
                LogScreen(
                    logDao = logDao,
                    onBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogScreen(logDao: LogDao, onBack: () -> Unit) {
    val logs by logDao.getAllLogs().collectAsStateWithLifecycle(initialValue = emptyList())
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Access Log") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        coroutineScope.launch {
                            logDao.clearLogs()
                        }
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear Logs")
                    }
                }
            )
        }
    ) { padding ->
        if (logs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("No logs yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(logs) { log ->
                    LogItem(log)
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }
}

@Composable
fun LogItem(log: LogEntry) {
    val timeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    
    ListItem(
        headlineContent = { Text(log.appName, style = MaterialTheme.typography.bodyLarge) },
        supportingContent = { 
            Column {
                Text(log.packageName, style = MaterialTheme.typography.bodySmall)
                Text(
                    text = "${log.protocol} ${log.sourceAddress}:${log.sourcePort} -> ${log.destinationAddress}:${log.destinationPort}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        },
        trailingContent = {
            Text(timeFormat.format(Date(log.timestamp)), style = MaterialTheme.typography.labelSmall)
        },
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        if (log.blocked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        shape = androidx.compose.foundation.shape.CircleShape
                    )
            )
        }
    )
}
