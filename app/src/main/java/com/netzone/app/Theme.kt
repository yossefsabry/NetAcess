package com.netzone.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun NetZoneTheme(isDark: Boolean = false, content: @Composable () -> Unit) {
    val darkColorScheme = darkColorScheme(
        primary = Color(0xFFA5C9FF),
        onPrimary = Color(0xFF00325B),
        primaryContainer = Color(0xFF00497E),
        onPrimaryContainer = Color(0xFFD1E4FF),
        secondary = Color(0xFFBCC7DB),
        background = Color(0xFF1A1C1E),
        surface = Color(0xFF1A1C1E),
        surfaceVariant = Color(0xFF43474E),
        onSurfaceVariant = Color(0xFFC3C7CF)
    )

    val lightColorScheme = lightColorScheme(
        primary = Color(0xFF0061A4),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFD1E4FF),
        onPrimaryContainer = Color(0xFF001D36),
        secondary = Color(0xFF535F70),
        background = Color(0xFFFDFCFF),
        surface = Color(0xFFFDFCFF),
        surfaceVariant = Color(0xFFDFE2EB),
        onSurfaceVariant = Color(0xFF43474E)
    )

    val colors = if (isDark) darkColorScheme else lightColorScheme

    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}
