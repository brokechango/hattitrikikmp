package com.brokechango.hattitriki.feature.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brokechango.hattitriki.core.design.FootballCard
import com.brokechango.hattitriki.core.design.ScorePill
import com.brokechango.hattitriki.core.design.ScreenTitle

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onEvent: (HomeEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ScreenTitle(
            title = "Hattitriki FC",
            subtitle = "Partidos de domingo, goles y estadisticas del grupo."
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { onEvent(HomeEvent.OpenHistory) }) {
                Text("Historial")
            }
            OutlinedButton(onClick = { onEvent(HomeEvent.OpenPlayers) }) {
                Text("Jugadores")
            }
            OutlinedButton(onClick = { onEvent(HomeEvent.OpenAdmin) }) {
                Text("Admin")
            }
        }

        uiState.latestMatch?.let { match ->
            FootballCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onEvent(HomeEvent.OpenMatch(match.id)) },
                highlight = true
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Ultimo partido", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(match.dateLabel, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Equipo A", style = MaterialTheme.typography.titleMedium)
                        ScorePill("${match.teamAScore} - ${match.teamBScore}")
                        Text("Equipo B", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(label = "Partidos", value = uiState.totalMatches.toString(), modifier = Modifier.weight(1f))
            StatCard(label = "Goles", value = uiState.totalGoals.toString(), modifier = Modifier.weight(1f))
        }

        Text("Maximos goleadores", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        uiState.topScorers.forEachIndexed { index, stats ->
            Row(modifier = Modifier.fillMaxWidth()) {
                Text("${index + 1}.", modifier = Modifier.width(28.dp))
                Text(stats.player.name, modifier = Modifier.weight(1f))
                Text("${stats.goals} goles")
            }
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    FootballCard(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
