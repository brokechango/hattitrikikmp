package com.brokechango.hattitriki.feature.teamrandomizer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brokechango.hattitriki.core.design.CrestGold
import com.brokechango.hattitriki.ui.composables.FootballCard
import com.brokechango.hattitriki.ui.composables.ScreenTitle
import kotlin.math.roundToInt

@Composable
fun TeamRandomizerResultScreen(
    viewModel: TeamRandomizerViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        if (uiState.teams.isEmpty()) {
            MissingResult(onBack = onBack)
        } else {
            ScreenTitle(
                title = "Equipos listos",
                subtitle = "Revisa el reparto antes de preparar el próximo partido."
            )

            ResultSummary(uiState)

            ResultActions(
                uiState = uiState,
                onReroll = { viewModel.onEvent(TeamRandomizerEvent.Generate) },
                onSave = { viewModel.onEvent(TeamRandomizerEvent.SaveDraft) }
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                uiState.teams.forEachIndexed { index, team ->
                    TeamResultCard(
                        team = team,
                        teamIndex = index,
                        showStats = uiState.balanceStats
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultSummary(uiState: TeamRandomizerUiState) {
    FootballCard(
        modifier = Modifier.fillMaxWidth(),
        highlight = true
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ResultMetric(
                value = uiState.teams.size.toString(),
                label = "equipos",
                modifier = Modifier.weight(1f)
            )
            ResultMetric(
                value = uiState.participants.size.toString(),
                label = "jugadores",
                modifier = Modifier.weight(1f)
            )
            ResultMetric(
                value = uiState.estimatedTeamSize,
                label = "por equipo",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ResultMetric(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ResultActions(
    uiState: TeamRandomizerUiState,
    onReroll: () -> Unit,
    onSave: () -> Unit
) {
    val requirement = uiState.saveDraftRequirement

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "¿Te encaja el reparto?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = when {
                    uiState.isCurrentResultSaved ->
                        "Estos equipos ya están guardados para el próximo partido."
                    requirement != null ->
                        requirement
                    else ->
                        "Puedes repetir el sorteo o guardar los equipos A y B."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedButton(
                onClick = onReroll,
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 48.dp)
            ) {
                Text("Volver a sortear")
            }

            Button(
                onClick = onSave,
                enabled = uiState.canSaveDraft && !uiState.isCurrentResultSaved,
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 50.dp)
            ) {
                Text(
                    if (uiState.isCurrentResultSaved) {
                        "Guardado para el próximo partido"
                    } else {
                        "Guardar para el próximo partido"
                    }
                )
            }

            uiState.draftMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = CrestGold
                )
            }

            uiState.errorMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun TeamResultCard(
    team: RandomTeam,
    teamIndex: Int,
    showStats: Boolean
) {
    FootballCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = CrestGold
                ) {
                    Box(
                        modifier = Modifier.size(38.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = teamLetter(teamIndex),
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Equipo ${teamLetter(teamIndex)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = buildString {
                            append("${team.players.size} jugadores")
                            if (team.cardioPlayers > 0) {
                                append(" · ${team.cardioPlayers} con cardio")
                            }
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (showStats) {
                    ScoreBadge("${team.statsScore.roundToInt()} pts")
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.28f))

            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                team.players.forEachIndexed { index, player ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = (index + 1).toString().padStart(2, '0'),
                            style = MaterialTheme.typography.labelMedium,
                            color = CrestGold,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = player.name,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        if (player.hasCardio) {
                            Text(
                                text = "Cardio",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScoreBadge(text: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelMedium,
            color = CrestGold,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun MissingResult(onBack: () -> Unit) {
    ScreenTitle(
        title = "No hay un sorteo activo",
        subtitle = "Vuelve al generador para elegir la convocatoria."
    )

    FootballCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "El resultado ya no está disponible",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "La plantilla sigue intacta. Solo necesitas generar los equipos de nuevo.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Volver al generador")
            }
        }
    }
}

private fun teamLetter(index: Int): String =
    if (index in 0..25) ('A'.code + index).toChar().toString() else (index + 1).toString()
