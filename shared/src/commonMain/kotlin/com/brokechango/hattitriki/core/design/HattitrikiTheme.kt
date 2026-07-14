package com.brokechango.hattitriki.core.design

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val CrestBlack = Color(0xFF07111F)
val CrestNavy = Color(0xFF0A1D34)
val CrestNavyLight = Color(0xFF14385D)
val CrestGold = Color(0xFFF2BE3E)
val CrestGoldLight = Color(0xFFFFD978)
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
    surface = Color(0xFF10243C),
    onSurface = CrestWhite,
    surfaceVariant = Color(0xFF173653),
    onSurfaceVariant = Color(0xFFB8CADC),
    outline = Color(0xFF58728C)
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
