package com.brokechango.hattitriki.ui.composables

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.brokechango.hattitriki.getPlatform
import com.brokechango.hattitriki.core.design.CrestGold

@Composable
fun FootballCard(
    modifier: Modifier = Modifier,
    highlight: Boolean = false,
    content: @Composable () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val isWeb = getPlatform().name == "Web"
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (highlight) colors.primaryContainer else colors.surface
        ),
        border = when {
            highlight -> androidx.compose.foundation.BorderStroke(1.dp, CrestGold.copy(alpha = 0.78f))
            isWeb -> androidx.compose.foundation.BorderStroke(1.dp, colors.outline.copy(alpha = 0.22f))
            else -> null
        },
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isWeb) 1.dp else 0.dp
        )
    ) {
        content()
    }
}
