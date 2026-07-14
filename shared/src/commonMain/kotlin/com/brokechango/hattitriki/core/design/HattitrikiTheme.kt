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

val PitchGreen = Color(0xFF0B5D35)
val DeepPitch = Color(0xFF063C28)
val LimeLine = Color(0xFFD4F35B)
val ClubGold = Color(0xFFFFC857)
val KitRed = Color(0xFFE64B3C)
val ChalkWhite = Color(0xFFF6F7EC)
val NightGrass = Color(0xFF061D16)

private val FootballColorScheme = darkColorScheme(
    primary = LimeLine,
    onPrimary = NightGrass,
    primaryContainer = PitchGreen,
    onPrimaryContainer = ChalkWhite,
    secondary = ClubGold,
    onSecondary = NightGrass,
    secondaryContainer = Color(0xFF5B4300),
    onSecondaryContainer = ChalkWhite,
    tertiary = KitRed,
    onTertiary = ChalkWhite,
    background = NightGrass,
    onBackground = ChalkWhite,
    surface = Color(0xFF0B2A20),
    onSurface = ChalkWhite,
    surfaceVariant = Color(0xFF123D2D),
    onSurfaceVariant = Color(0xFFD9E7D1),
    outline = Color(0xFF7FA889)
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
                colors = listOf(NightGrass, DeepPitch, PitchGreen)
            )
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(ChalkWhite.copy(alpha = 0.45f))
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
            color = if (highlight) LimeLine.copy(alpha = 0.8f) else colors.outline.copy(alpha = 0.45f)
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
    get() = LimeLine
