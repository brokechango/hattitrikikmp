package com.brokechango.hattitriki.ui.composables

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.brokechango.hattitriki.core.design.CrestGold
import com.brokechango.hattitriki.ui.preview.HattitrikiPreview
import com.brokechango.hattitriki.ui.preview.PreviewTargets
import hattitriki.shared.generated.resources.Res
import hattitriki.shared.generated.resources.emoji_football
import org.jetbrains.compose.resources.painterResource

@Composable
fun SupabaseLoadingState(
    message: String,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val transition = rememberInfiniteTransition(label = "Carga de Supabase")
    val ballLift by transition.animateFloat(
        initialValue = 1f,
        targetValue = if (compact) -7f else -11f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 560, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Salto del balón"
    )
    val ballTilt by transition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 560, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Giro del balón"
    )
    val shadowScale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.62f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 560, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Sombra del balón"
    )
    val stageSize = if (compact) 54.dp else 76.dp
    val ringSize = if (compact) 44.dp else 64.dp
    val ballSize = if (compact) 24.dp else 32.dp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                contentDescription = message
                progressBarRangeInfo = ProgressBarRangeInfo.Indeterminate
            }
            .padding(vertical = if (compact) 12.dp else 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 12.dp)
    ) {
        Box(
            modifier = Modifier.size(stageSize),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = if (compact) 3.dp else 5.dp)
                    .width(if (compact) 22.dp else 30.dp)
                    .height(if (compact) 5.dp else 7.dp)
                    .graphicsLayer {
                        scaleX = shadowScale
                        alpha = 0.2f + (shadowScale * 0.22f)
                    }
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurface)
            )
            CircularProgressIndicator(
                color = CrestGold,
                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                strokeWidth = if (compact) 2.dp else 3.dp,
                modifier = Modifier.size(ringSize)
            )
            Image(
                painter = painterResource(Res.drawable.emoji_football),
                contentDescription = null,
                modifier = Modifier
                    .size(ballSize)
                    .offset(y = ballLift.dp)
                    .graphicsLayer { rotationZ = ballTilt }
            )
        }
        Text(
            text = message,
            style = if (compact) {
                MaterialTheme.typography.bodySmall
            } else {
                MaterialTheme.typography.bodyMedium
            },
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
    }
}

@PreviewTargets
@Composable
private fun SupabaseLoadingStatePreview() {
    HattitrikiPreview {
        SupabaseLoadingState(
            message = "Cargando las clasificaciones…",
            modifier = Modifier.fillMaxSize()
        )
    }
}
