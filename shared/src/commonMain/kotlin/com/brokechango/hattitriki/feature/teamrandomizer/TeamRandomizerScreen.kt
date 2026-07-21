package com.brokechango.hattitriki.feature.teamrandomizer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brokechango.hattitriki.core.design.CrestGold
import com.brokechango.hattitriki.ui.composables.FootballCard
import com.brokechango.hattitriki.ui.composables.ScreenTitle
import com.brokechango.hattitriki.ui.composables.SupabaseLoadingState

@Composable
fun TeamRandomizerScreen(
    viewModel: TeamRandomizerViewModel,
    onResultReady: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        ScreenTitle(
            title = "Generador de equipos",
            subtitle = "Selecciona quién juega hoy y prepara un reparto equilibrado."
        )

        uiState.savedDraft?.let { draft ->
            SavedDraftBanner(
                teamASize = draft.teamAPlayerIds.size,
                teamBSize = draft.teamBPlayerIds.size,
                onDiscard = { viewModel.onEvent(TeamRandomizerEvent.ClearDraft) }
            )
        }

        RosterSection(
            uiState = uiState,
            onEvent = viewModel::onEvent
        )

        SetupSection(
            uiState = uiState,
            onEvent = viewModel::onEvent,
            onResultReady = onResultReady
        )
    }
}

@Composable
private fun SavedDraftBanner(
    teamASize: Int,
    teamBSize: Int,
    onDiscard: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, end = 8.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = "Borrador listo para el próximo partido",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "$teamASize jugadores en A · $teamBSize jugadores en B",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(onClick = onDiscard) {
                Text("Descartar")
            }
        }
    }
}

@Composable
private fun RosterSection(
    uiState: TeamRandomizerUiState,
    onEvent: (TeamRandomizerEvent) -> Unit
) {
    FootballCard(
        modifier = Modifier.fillMaxWidth(),
        highlight = true
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = "Convocatoria",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Solo jugadores activos de la plantilla",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                CountBadge("${uiState.registeredPlayers.size} activos")
            }

            if (uiState.isLoadingRoster) {
                SupabaseLoadingState(
                    message = "Cargando la plantilla del club…",
                    compact = true
                )
            } else if (uiState.registeredPlayers.isEmpty()) {
                EmptyRosterState(
                    message = uiState.rosterMessage,
                    onReload = { onEvent(TeamRandomizerEvent.ReloadRoster) }
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${uiState.participants.size} seleccionados",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    TextButton(
                        onClick = { onEvent(TeamRandomizerEvent.SelectAllPlayers) },
                        enabled = !uiState.allPlayersSelected
                    ) {
                        Text("Todos")
                    }
                    TextButton(
                        onClick = { onEvent(TeamRandomizerEvent.ClearSelection) },
                        enabled = uiState.selectedPlayerIds.isNotEmpty()
                    ) {
                        Text("Ninguno")
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    uiState.registeredPlayers.chunked(2).forEach { rowPlayers ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowPlayers.forEach { player ->
                                PlayerToggle(
                                    player = player,
                                    selected = player.id in uiState.selectedPlayerIds,
                                    onClick = {
                                        onEvent(TeamRandomizerEvent.TogglePlayer(player.id))
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (rowPlayers.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                TextButton(
                    onClick = { onEvent(TeamRandomizerEvent.ReloadRoster) },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Actualizar plantilla")
                }
            }

            if (!uiState.isLoadingRoster && uiState.registeredPlayers.isNotEmpty()) {
                uiState.rosterMessage?.let { message ->
                    SupportingMessage(message)
                }
            }
        }
    }
}

@Composable
private fun PlayerToggle(
    player: TeamParticipant,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(10.dp)
    Row(
        modifier = modifier
            .defaultMinSize(minHeight = 52.dp)
            .clip(shape)
            .background(
                if (selected) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.13f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = selected,
            onCheckedChange = null
        )
        Column(
            modifier = Modifier.padding(start = 8.dp).weight(1f),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Text(
                text = player.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                maxLines = 1
            )
            if (player.hasCardio) {
                Text(
                    text = "Cardio",
                    style = MaterialTheme.typography.labelSmall,
                    color = CrestGold
                )
            }
        }
    }
}

@Composable
private fun EmptyRosterState(
    message: String?,
    onReload: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "No hay jugadores disponibles",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = message ?: "Añade jugadores activos antes de generar los equipos.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedButton(onClick = onReload) {
                Text("Volver a intentar")
            }
        }
    }
}

@Composable
private fun SetupSection(
    uiState: TeamRandomizerUiState,
    onEvent: (TeamRandomizerEvent) -> Unit,
    onResultReady: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionHeading(
            eyebrow = "Paso 2",
            title = "Configura el reparto"
        )

        FootballCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = "Número de equipos",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (uiState.participants.size >= 2) {
                                "Elige entre 2 y ${uiState.participants.size}"
                            } else {
                                "Selecciona al menos dos jugadores"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    TeamCountStepper(
                        value = uiState.teamCount,
                        canDecrease = uiState.teamCount > 2,
                        canIncrease = uiState.teamCount < uiState.participants.size,
                        onDecrease = {
                            onEvent(TeamRandomizerEvent.TeamCountChanged(uiState.teamCount - 1))
                        },
                        onIncrease = {
                            onEvent(TeamRandomizerEvent.TeamCountChanged(uiState.teamCount + 1))
                        }
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(
                            enabled = uiState.statsAvailable,
                            onClick = { onEvent(TeamRandomizerEvent.ToggleStatsBalance) }
                        )
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = "Equilibrar por rendimiento",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (uiState.statsAvailable) {
                                "Compensa goles, victorias, partidos y portería."
                            } else {
                                "Las estadísticas no están disponibles ahora."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = uiState.balanceStats,
                        enabled = uiState.statsAvailable,
                        onCheckedChange = {
                            onEvent(TeamRandomizerEvent.ToggleStatsBalance)
                        }
                    )
                }

                SetupSummary(uiState)

                Button(
                    onClick = {
                        onEvent(TeamRandomizerEvent.Generate)
                        onResultReady()
                    },
                    enabled = uiState.canGenerate,
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 52.dp)
                ) {
                    Text(
                        if (uiState.teams.isEmpty()) {
                            "Generar equipos"
                        } else {
                            "Volver a generar"
                        }
                    )
                }

                uiState.errorMessage?.let { message ->
                    ErrorMessage(message)
                }
            }
        }
    }
}

@Composable
private fun TeamCountStepper(
    value: Int,
    canDecrease: Boolean,
    canIncrease: Boolean,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(
            onClick = onDecrease,
            enabled = canDecrease,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
            modifier = Modifier.size(42.dp)
        ) {
            Text("−", style = MaterialTheme.typography.titleLarge)
        }
        Box(
            modifier = Modifier.size(34.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
        }
        OutlinedButton(
            onClick = onIncrease,
            enabled = canIncrease,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
            modifier = Modifier.size(42.dp)
        ) {
            Text("+", style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
private fun SetupSummary(uiState: TeamRandomizerUiState) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.48f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SummaryValue(
                value = uiState.participants.size.toString(),
                label = "jugadores",
                modifier = Modifier.weight(1f)
            )
            SummaryValue(
                value = uiState.estimatedTeamSize,
                label = "por equipo",
                modifier = Modifier.weight(1f)
            )
            SummaryValue(
                value = uiState.selectedCardioPlayers.toString(),
                label = "con cardio",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SummaryValue(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SectionHeading(
    eyebrow: String,
    title: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = eyebrow,
            style = MaterialTheme.typography.labelMedium,
            color = CrestGold,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun CountBadge(text: String) {
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
private fun SupportingMessage(message: String) {
    Text(
        text = message,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun ErrorMessage(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error
        )
    }
}
