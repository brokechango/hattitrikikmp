package com.brokechango.hattitriki.feature.matchdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.brokechango.hattitriki.core.model.TeamSide

@Composable
fun MatchDetailScreen(
    viewModel: MatchDetailViewModel,
    onEvent: (MatchDetailEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val match = uiState.match

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedButton(onClick = { onEvent(MatchDetailEvent.Back) }) {
            Text("Volver")
        }

        if (match == null) {
            Text("No se encontro el partido.")
            return@Column
        }

        ScreenTitle(
            title = match.dateLabel,
            subtitle = "Acta del partido y alineaciones."
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Equipo A", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            ScorePill("${match.teamAScore} - ${match.teamBScore}")
            Text("Equipo B", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TeamCard(
                title = "Equipo A",
                team = TeamSide.A,
                uiState = uiState,
                modifier = Modifier.weight(1f)
            )
            TeamCard(
                title = "Equipo B",
                team = TeamSide.B,
                uiState = uiState,
                modifier = Modifier.weight(1f)
            )
        }

        Text("Goles", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        match.goals.forEach { goal ->
            val playerName = uiState.playersById[goal.playerId]?.name.orEmpty()
            Text("$playerName: ${goal.count}")
        }
    }
}

@Composable
private fun TeamCard(
    title: String,
    team: TeamSide,
    uiState: MatchDetailUiState,
    modifier: Modifier = Modifier
) {
    val match = uiState.match ?: return

    FootballCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            match.players.filter { it.team == team }.forEach { matchPlayer ->
                val name = uiState.playersById[matchPlayer.playerId]?.name.orEmpty()
                val suffix = if (matchPlayer.wasGoalkeeper) " (portero)" else ""
                Text("$name$suffix")
            }
        }
    }
}
