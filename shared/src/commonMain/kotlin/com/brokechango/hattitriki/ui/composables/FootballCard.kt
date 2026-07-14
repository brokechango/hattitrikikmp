package com.brokechango.hattitriki.ui.composables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.brokechango.hattitriki.core.design.CrestGold

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
