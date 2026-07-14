package com.brokechango.hattitriki.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.brokechango.hattitriki.core.design.CrestBlack
import com.brokechango.hattitriki.core.design.CrestGold
import com.brokechango.hattitriki.core.design.CrestNavy

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
