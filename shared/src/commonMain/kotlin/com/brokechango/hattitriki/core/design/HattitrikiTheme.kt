package com.brokechango.hattitriki.core.design

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val CrestBlack = Color(0xFF030405)
val CrestNavy = Color(0xFF001D43)
val CrestNavyLight = Color(0xFF073067)
val CrestGold = Color(0xFFE8B927)
val CrestGoldLight = Color(0xFFFFD95B)
val CrestRed = Color(0xFFC8102E)
val CrestMaroon = Color(0xFF8F1538)
val CrestWhite = Color(0xFFF8F8F2)

private val FootballColorScheme = darkColorScheme(
    primary = CrestGold,
    onPrimary = CrestBlack,
    primaryContainer = CrestNavy,
    onPrimaryContainer = CrestWhite,
    secondary = CrestRed,
    onSecondary = CrestWhite,
    secondaryContainer = CrestMaroon,
    onSecondaryContainer = CrestWhite,
    tertiary = CrestGoldLight,
    onTertiary = CrestBlack,
    background = CrestBlack,
    onBackground = CrestWhite,
    surface = Color(0xFF07162B),
    onSurface = CrestWhite,
    surfaceVariant = CrestNavyLight,
    onSurfaceVariant = Color(0xFFD9E2F2),
    outline = Color(0xFFD6A92D)
)

@Composable
fun HattitrikiTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = FootballColorScheme,
        content = content
    )
}

val ColorScheme.scoreWin: Color
    get() = CrestGold
