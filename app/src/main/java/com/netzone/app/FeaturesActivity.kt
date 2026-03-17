package com.netzone.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

class FeaturesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val preferenceManager = PreferenceManager(this)

        setContent {
            val isDarkMode by preferenceManager.isDarkMode.collectAsStateWithLifecycle(initialValue = false)
            
            NetZoneTheme(isDark = isDarkMode) {
                FeaturesScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeaturesScreen(onBack: () -> Unit) {
    val features = listOf(
        Feature("Access Log", "Record and display all network traffic attempts.", Icons.AutoMirrored.Filled.List),
        Feature("Network Filtering", "Advanced packet filtering and domain blocking.", Icons.Default.FilterList),
        Feature("Custom DNS", "Use your preferred DNS servers for all apps.", Icons.Default.Dns),
        Feature("Traffic Speed", "Monitor real-time upload and download speeds.", Icons.Default.Speed),
        Feature("App Themes", "Choose between light and dark themes.", Icons.Default.Palette),
        Feature("Task Automation", "Schedule firewall rules based on triggers.", Icons.Default.AutoMode)
    )
    
    val uploadSpeed by NetZoneVpnService.uploadSpeed.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Features") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.medium)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Verified, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Unlock Your Device's Potential", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text("Current Speed: ${formatSpeed(uploadSpeed)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text("All features are free and open source.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(features) { feature ->
                    ListItem(
                        headlineContent = { Text(feature.title, fontWeight = FontWeight.SemiBold) },
                        supportingContent = { Text(feature.description) },
                        leadingContent = { Icon(feature.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        trailingContent = {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Available", tint = MaterialTheme.colorScheme.primary)
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
                }
                
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

fun formatSpeed(bytes: Long): String {
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    return when {
        mb >= 1.0 -> "%.2f MB/s".format(mb)
        kb >= 1.0 -> "%.2f KB/s".format(kb)
        else -> "$bytes B/s"
    }
}

data class Feature(val title: String, val description: String, val icon: ImageVector)
