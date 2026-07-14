package com.brokechango.hattitriki.core.design

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

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

@Composable
fun PitchBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.background(
            Brush.verticalGradient(
                colors = listOf(CrestBlack, Color(0xFF061124), CrestNavy)
            )
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(CrestGold.copy(alpha = 0.72f))
        )
        content()
    }
}

@Composable
fun FootballCard(
    modifier: Modifier = Modifier,
    highlight: Boolean = false,
    content: @Composable () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (highlight) colors.primaryContainer else colors.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (highlight) CrestGold.copy(alpha = 0.9f) else colors.outline.copy(alpha = 0.48f)
        )
    ) {
        content()
    }
}

@Composable
fun ScreenTitle(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ScorePill(
    score: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        Text(
            text = score,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
fun StatRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.Bold)
    }
}

val ColorScheme.scoreWin: Color
    get() = CrestGold
