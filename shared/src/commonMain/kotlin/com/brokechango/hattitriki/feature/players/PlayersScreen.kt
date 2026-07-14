package com.brokechango.hattitriki.feature.players

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
import com.brokechango.hattitriki.core.design.FootballCard
import com.brokechango.hattitriki.core.design.ScreenTitle
import com.brokechango.hattitriki.core.design.StatRow

@Composable
fun PlayersScreen(
    viewModel: PlayersViewModel,
    onEvent: (PlayersEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ScreenTitle(
            title = "Plantilla",
            subtitle = "Racha, goles y partidos de cada jugador."
        )
        uiState.players.forEach { stats ->
            FootballCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stats.player.name,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text("${stats.goals} goles", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatRow("PJ", stats.matchesPlayed.toString(), Modifier.weight(1f))
                        StatRow("G", stats.wins.toString(), Modifier.weight(1f))
                        StatRow("E", stats.draws.toString(), Modifier.weight(1f))
                        StatRow("P", stats.losses.toString(), Modifier.weight(1f))
                    }
                    StatRow("Portero", "${stats.goalkeeperMatches} partidos")
                }
            }
        }
    }
}
