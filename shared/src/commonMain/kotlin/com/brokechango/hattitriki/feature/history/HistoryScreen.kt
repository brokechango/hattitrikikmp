package com.brokechango.hattitriki.feature.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brokechango.hattitriki.ui.composables.FootballCard
import com.brokechango.hattitriki.ui.composables.ScorePill
import com.brokechango.hattitriki.ui.composables.ScreenTitle

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onEvent: (HistoryEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ScreenTitle(
            title = "Historial",
            subtitle = "Todos los marcadores guardados de la liga del grupo."
        )
        uiState.matches.forEach { match ->
            FootballCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onEvent(HistoryEvent.OpenMatch(match.id)) }
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(match.dateLabel, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Equipo A", modifier = Modifier.weight(1f))
                        ScorePill("${match.teamAScore} - ${match.teamBScore}")
                        Text("Equipo B", modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
