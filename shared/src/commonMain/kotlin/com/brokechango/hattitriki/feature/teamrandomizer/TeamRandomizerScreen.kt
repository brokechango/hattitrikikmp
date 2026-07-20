package com.brokechango.hattitriki.feature.teamrandomizer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brokechango.hattitriki.ui.composables.FootballCard
import com.brokechango.hattitriki.ui.composables.ScreenTitle
import kotlin.math.roundToInt

@Composable
fun TeamRandomizerScreen(
    viewModel: TeamRandomizerViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ScreenTitle(
            title = "Aleatorizador de equipos",
            subtitle = "Añade los participantes, elige los equipos y crea un reparto equilibrado."
        )

        uiState.savedDraft?.let { draft ->
            FootballCard(modifier = Modifier.fillMaxWidth(), highlight = true) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Borrador de partido guardado",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${draft.teamAPlayerIds.size} jugadores en el Equipo A · " +
                            "${draft.teamBPlayerIds.size} en el Equipo B. " +
                            "Se cargarán automáticamente al crear un partido.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedButton(
                        onClick = { viewModel.onEvent(TeamRandomizerEvent.ClearDraft) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Descartar borrador")
                    }
                }
            }
        }

        FootballCard(modifier = Modifier.fillMaxWidth(), highlight = true) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = uiState.participantInput,
                    onValueChange = { viewModel.onEvent(TeamRandomizerEvent.ParticipantsChanged(it)) },
                    label = { Text("Participantes") },
                    placeholder = { Text("Un nombre por línea") },
                    minLines = 6,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.onEvent(TeamRandomizerEvent.UseActiveRoster) },
                        enabled = !uiState.isLoadingRoster,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Usar plantilla (${uiState.registeredPlayers.size})")
                    }
                    OutlinedButton(
                        onClick = { viewModel.onEvent(TeamRandomizerEvent.ReloadRoster) },
                        enabled = !uiState.isLoadingRoster,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (uiState.isLoadingRoster) "Cargando…" else "Actualizar")
                    }
                }

                OutlinedTextField(
                    value = uiState.teamCountInput,
                    onValueChange = { viewModel.onEvent(TeamRandomizerEvent.TeamCountChanged(it)) },
                    label = { Text("Número de equipos") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "${uiState.participants.size} participantes · ${uiState.teamCount ?: "—"} equipos · ${uiState.estimatedTeamSize} personas por equipo",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = if (uiState.selectedCardioPlayers == 0) {
                        "No hay participantes con cardio marcado."
                    } else {
                        "Se repartirán ${uiState.selectedCardioPlayers} jugadores con cardio para que no coincidan en el mismo equipo, siempre que sea posible."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedButton(
                    onClick = { viewModel.onEvent(TeamRandomizerEvent.ToggleStatsBalance) },
                    enabled = uiState.statsAvailable,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (uiState.balanceStats) {
                            "✓ Equilibrando por estadísticas"
                        } else {
                            "Equilibrar por estadísticas"
                        }
                    )
                }

                if (uiState.balanceStats) {
                    Text(
                        text = "Puntuación: 3×goles + 2×victorias + partidos jugados + partidos como portero − goles encajados por partido del equipo.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                uiState.rosterMessage?.let { message ->
                    Text(message, color = MaterialTheme.colorScheme.error)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.onEvent(TeamRandomizerEvent.LoadExample) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Datos de ejemplo")
                    }
                    OutlinedButton(
                        onClick = { viewModel.onEvent(TeamRandomizerEvent.Clear) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Borrar")
                    }
                }

                Button(
                    onClick = { viewModel.onEvent(TeamRandomizerEvent.Generate) },
                    enabled = uiState.canGenerate,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (uiState.teams.isEmpty()) "Generar equipos" else "Generar de nuevo")
                }

                uiState.errorMessage?.let { message ->
                    Text(message, color = MaterialTheme.colorScheme.error)
                }
            }
        }

        if (uiState.teams.isNotEmpty()) {
            Text(
                text = "Resultado",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            uiState.teams.forEach { team ->
                FootballCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = buildString {
                                append("${team.name} · ${team.players.size}")
                                if (uiState.balanceStats) append(" · ${team.statsScore.roundToInt()} pts")
                                if (team.cardioPlayers > 0) append(" · ${team.cardioPlayers} con cardio")
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        team.players.forEach { player ->
                            Text("• ${player.name}")
                        }
                    }
                }
            }

            FootballCard(modifier = Modifier.fillMaxWidth(), highlight = true) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val saveDraftRequirement = uiState.saveDraftRequirement
                    Text(
                        "Preparar el próximo partido",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        when {
                            uiState.isCurrentResultSaved ->
                                "✓ Estos equipos ya están guardados y se cargarán en Nuevo partido."
                            saveDraftRequirement != null ->
                                saveDraftRequirement
                            else ->
                                "Guarda el Equipo 1 como Equipo A y el Equipo 2 como Equipo B."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = { viewModel.onEvent(TeamRandomizerEvent.SaveDraft) },
                        enabled = uiState.canSaveDraft && !uiState.isCurrentResultSaved,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (uiState.isCurrentResultSaved) {
                                "Borrador guardado"
                            } else {
                                "Guardar equipos como borrador"
                            }
                        )
                    }
                    uiState.draftMessage?.let { message ->
                        Text(
                            message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
