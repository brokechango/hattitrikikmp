package com.brokechango.hattitriki.ui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brokechango.hattitriki.core.design.CrestGold
import com.brokechango.hattitriki.ui.preview.HattitrikiPreview
import com.brokechango.hattitriki.ui.preview.PreviewTargets

@Composable
fun ScreenTitle(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            letterSpacing = (-0.8).sp,
            fontWeight = FontWeight.Black,
            color = CrestGold
        )
        subtitle?.let {
            Text(
                text = subtitle,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@PreviewTargets
@Composable
private fun ScreenTitlePreview() {
    HattitrikiPreview {
        ScreenTitle(
            title = "Clasificaciones",
            subtitle = "Consulta los líderes de cada estadística.",
            modifier = Modifier.padding(20.dp)
        )
    }
}
