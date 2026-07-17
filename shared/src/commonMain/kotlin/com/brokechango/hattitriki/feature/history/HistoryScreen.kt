package com.brokechango.hattitriki.feature.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brokechango.hattitriki.ui.composables.FootballCard
import com.brokechango.hattitriki.ui.composables.HattitrikiPullToRefresh
import com.brokechango.hattitriki.ui.composables.PenaltyScore
import com.brokechango.hattitriki.ui.composables.ScorePill
import com.brokechango.hattitriki.ui.composables.ScreenTitle

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onEvent: (HistoryEvent) -> Unit,
    scrollState: ScrollState,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HattitrikiPullToRefresh(
        isRefreshing = uiState.isRefreshing,
        onRefresh = viewModel::refresh,
        modifier = modifier
    ) {
    Column(
        modifier = Modifier.verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ScreenTitle(
            title = "Resultados",
            subtitle = "Todos los partidos guardados de la liga del grupo."
        )
        if (uiState.isLoading) {
            Text("Cargando partidos…")
            return@Column
        }
        uiState.errorMessage?.let { message ->
            Text(message, color = MaterialTheme.colorScheme.error)
            return@Column
        }
        Text(
            text = "PARTIDOS FINALIZADOS",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Black
        )
        if (uiState.matches.isEmpty()) {
            Text(
                "Todavía no hay partidos guardados.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        uiState.matches.forEach { match ->
            FootballCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onEvent(HistoryEvent.OpenMatch(match.id)) }
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            match.dateLabel,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "FINAL",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Text("Equipo A", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                        ScorePill(score = "${match.teamAScore} - ${match.teamBScore}")
                        Text(
                            "Equipo B",
                            modifier = Modifier.weight(1f),
                            fontWeight = FontWeight.Bold,
                            textAlign = androidx.compose.ui.text.style.TextAlign.End
                        )
                    }
                    match.penaltyShootout?.let {
                        PenaltyScore(
                            score = "${it.teamAScore} - ${it.teamBScore}",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
    }
}
