package com.brokechango.hattitriki.feature.newmatch

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brokechango.hattitriki.core.data.ActaTeam
import com.brokechango.hattitriki.core.data.AdminPlayer
import com.brokechango.hattitriki.core.design.CrestGold
import com.brokechango.hattitriki.ui.composables.FootballCard
import com.brokechango.hattitriki.ui.composables.HattitrikiDatePickerField
import com.brokechango.hattitriki.ui.composables.ScreenTitle
import com.brokechango.hattitriki.ui.composables.SupabaseLoadingState
import com.brokechango.hattitriki.ui.preview.HattitrikiPreview
import com.brokechango.hattitriki.ui.preview.PreviewTargets

private enum class MatchCreationStep(val label: String) {
    MATCH("Partido"),
    TEAMS("Equipos"),
    GOALS("Goles")
}

@Composable
fun NewMatchScreen(
    viewModel: NewMatchViewModel,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var currentStepName by rememberSaveable { mutableStateOf(MatchCreationStep.MATCH.name) }
    val currentStep = MatchCreationStep.valueOf(currentStepName)

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onSaved()
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ScreenTitle(
            title = if (uiState.isEditing) "Editar acta" else "Nueva acta",
            subtitle = if (uiState.isEditing) {
                "Revisa el partido paso a paso y guarda los cambios."
            } else {
                "Completa partido, equipos y goles en tres pasos."
            }
        )

        when {
            uiState.isCheckingAccess -> SupabaseLoadingState(
                message = "Comprobando permisos…",
                compact = true
            )
            !uiState.isAdmin -> AccessDenied(uiState.errorMessage)
            uiState.isLoadingPlayers -> SupabaseLoadingState(
                message = "Cargando la plantilla…",
                compact = true
            )
            else -> MatchReportFlow(
                uiState = uiState,
                currentStep = currentStep,
                onStepSelected = { currentStepName = it.name },
                onEvent = viewModel::onEvent
            )
        }
    }
}

@Composable
private fun AccessDenied(errorMessage: String?) {
    FootballCard(modifier = Modifier.fillMaxWidth(), highlight = true) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Acceso restringido", style = MaterialTheme.typography.titleMedium)
            Text(errorMessage ?: "Inicia sesión como administrador desde la Zona míster.")
        }
    }
}

@Composable
private fun MatchReportFlow(
    uiState: NewMatchUiState,
    currentStep: MatchCreationStep,
    onStepSelected: (MatchCreationStep) -> Unit,
    onEvent: (NewMatchEvent) -> Unit
) {
    var selectedLineupTeamName by rememberSaveable { mutableStateOf(ActaTeam.A.name) }
    var selectedGoalsTeamName by rememberSaveable { mutableStateOf(ActaTeam.A.name) }

    uiState.teamsDraftMessage?.let { message ->
        FootballCard(modifier = Modifier.fillMaxWidth(), highlight = true) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Borrador de equipos",
                    style = MaterialTheme.typography.titleMedium,
                    color = CrestGold
                )
                Text(message, style = MaterialTheme.typography.bodySmall)
                OutlinedButton(
                    onClick = { onEvent(NewMatchEvent.DiscardTeamsDraft) },
                    enabled = !uiState.isSaving,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Descartar borrador y vaciar equipos")
                }
            }
        }
    }

    MatchStepBreadcrumb(
        currentStep = currentStep,
        uiState = uiState,
        onStepSelected = onStepSelected
    )

    AnimatedContent(
        targetState = currentStep,
        transitionSpec = {
            val direction = if (targetState.ordinal > initialState.ordinal) 1 else -1
            slideInHorizontally(
                animationSpec = tween(220),
                initialOffsetX = { direction * it / 3 }
            ) togetherWith slideOutHorizontally(
                animationSpec = tween(220),
                targetOffsetX = { -direction * it / 3 }
            )
        },
        modifier = Modifier.fillMaxWidth(),
        label = "Paso de creación del partido"
    ) { step ->
        when (step) {
            MatchCreationStep.MATCH -> MatchBasics(uiState, onEvent)
            MatchCreationStep.TEAMS -> TeamAssignment(
                uiState = uiState,
                selectedTeam = ActaTeam.valueOf(selectedLineupTeamName),
                onTeamSelected = { selectedLineupTeamName = it.name },
                onEvent = onEvent
            )
            MatchCreationStep.GOALS -> GoalSelection(
                uiState = uiState,
                selectedTeam = ActaTeam.valueOf(selectedGoalsTeamName),
                onTeamSelected = { selectedGoalsTeamName = it.name },
                onEvent = onEvent
            )
        }
    }

    StepActions(
        uiState = uiState,
        currentStep = currentStep,
        onBack = {
            MatchCreationStep.entries.getOrNull(currentStep.ordinal - 1)?.let(onStepSelected)
        },
        onContinue = {
            MatchCreationStep.entries.getOrNull(currentStep.ordinal + 1)?.let(onStepSelected)
        },
        onSubmit = { onEvent(NewMatchEvent.Submit) }
    )
}

@Composable
private fun MatchStepBreadcrumb(
    currentStep: MatchCreationStep,
    uiState: NewMatchUiState,
    onStepSelected: (MatchCreationStep) -> Unit
) {
    FootballCard(modifier = Modifier.fillMaxWidth(), highlight = true) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                "Paso ${currentStep.ordinal + 1} de ${MatchCreationStep.entries.size}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MatchCreationStep.entries.forEachIndexed { index, step ->
                    TextButton(
                        onClick = { onStepSelected(step) },
                        enabled = step == currentStep || uiState.canOpen(step),
                        modifier = Modifier.weight(1f)
                    ) {
                        val prefix = when {
                            step == currentStep -> "${index + 1}. "
                            uiState.isComplete(step) -> "✓ "
                            else -> ""
                        }
                        Text(prefix + step.label, maxLines = 1)
                    }
                    if (index < MatchCreationStep.entries.lastIndex) {
                        Text("›", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun StepActions(
    uiState: NewMatchUiState,
    currentStep: MatchCreationStep,
    onBack: () -> Unit,
    onContinue: () -> Unit,
    onSubmit: () -> Unit
) {
    val requirement = uiState.requirementFor(currentStep)

    FootballCard(modifier = Modifier.fillMaxWidth(), highlight = true) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            requirement?.let {
                Text(
                    "Para continuar: $it",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            uiState.errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (currentStep != MatchCreationStep.MATCH) {
                    OutlinedButton(
                        onClick = onBack,
                        enabled = !uiState.isSaving,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Atrás")
                    }
                }
                Button(
                    onClick = if (currentStep == MatchCreationStep.GOALS) onSubmit else onContinue,
                    enabled = requirement == null && !uiState.isSaving,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        when {
                            uiState.isSaving && uiState.isEditing -> "Actualizando…"
                            uiState.isSaving -> "Guardando…"
                            currentStep != MatchCreationStep.GOALS -> "Continuar"
                            uiState.isEditing -> "Guardar cambios"
                            else -> "Guardar acta"
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MatchBasics(uiState: NewMatchUiState, onEvent: (NewMatchEvent) -> Unit) {
    FootballCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Partido", style = MaterialTheme.typography.titleMedium, color = CrestGold)
            Text(
                "Indica la fecha y el marcador final.",
                style = MaterialTheme.typography.bodySmall
            )
            HattitrikiDatePickerField(
                value = uiState.date,
                onDateSelected = { onEvent(NewMatchEvent.DateChanged(it)) },
                enabled = !uiState.isSaving,
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = uiState.teamAScore,
                    onValueChange = { onEvent(NewMatchEvent.TeamAScoreChanged(it)) },
                    label = { Text("Equipo A") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    enabled = !uiState.isSaving,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = uiState.teamBScore,
                    onValueChange = { onEvent(NewMatchEvent.TeamBScoreChanged(it)) },
                    label = { Text("Equipo B") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    enabled = !uiState.isSaving,
                    modifier = Modifier.weight(1f)
                )
            }
            if (uiState.isRegularScoreDraw) {
                PenaltyShootoutFields(uiState, onEvent)
            }
        }
    }
}

@Composable
private fun PenaltyShootoutFields(uiState: NewMatchUiState, onEvent: (NewMatchEvent) -> Unit) {
    OutlinedButton(
        onClick = { onEvent(NewMatchEvent.PenaltyShootoutToggled) },
        enabled = !uiState.isSaving,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(if (uiState.isPenaltyShootout) "✓ Decidido en penaltis" else "¿Se decidió en penaltis?")
    }
    if (uiState.isPenaltyShootout) {
        Text("Resultado de la tanda", style = MaterialTheme.typography.labelLarge, color = CrestGold)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = uiState.teamAPenaltyScore,
                onValueChange = { onEvent(NewMatchEvent.TeamAPenaltyScoreChanged(it)) },
                label = { Text("Equipo A") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                enabled = !uiState.isSaving,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = uiState.teamBPenaltyScore,
                onValueChange = { onEvent(NewMatchEvent.TeamBPenaltyScoreChanged(it)) },
                label = { Text("Equipo B") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                enabled = !uiState.isSaving,
                modifier = Modifier.weight(1f)
            )
        }
        Text(
            "La tanda debe tener un ganador y no suma goles al marcador.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun TeamAssignment(
    uiState: NewMatchUiState,
    selectedTeam: ActaTeam,
    onTeamSelected: (ActaTeam) -> Unit,
    onEvent: (NewMatchEvent) -> Unit
) {
    FootballCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Equipos y porteros", style = MaterialTheme.typography.titleMedium, color = CrestGold)
            Text(
                "Selecciona los jugadores de cada equipo. Un jugador puede participar en ambos.",
                style = MaterialTheme.typography.bodySmall
            )
            TeamTabs(
                selectedTeam = selectedTeam,
                labelFor = { team ->
                    val playerCount = uiState.playerIdsFor(team).size
                    "Equipo ${team.name} · $playerCount"
                },
                onTeamSelected = onTeamSelected,
                enabled = !uiState.isSaving
            )
            Text(
                "Marca como portero a uno o más jugadores del Equipo ${selectedTeam.name}.",
                style = MaterialTheme.typography.labelLarge,
                color = CrestGold
            )
            if (uiState.players.isEmpty()) {
                Text("No hay jugadores en Supabase. Añádelos desde Zona míster.")
            } else {
                uiState.players.forEach { player ->
                    TeamPlayerRow(
                        player = player,
                        isSelected = uiState.isOnTeam(player.id, selectedTeam),
                        isGoalkeeper = player.id in uiState.goalkeeperIdsFor(selectedTeam),
                        isSaving = uiState.isSaving,
                        onSelectedChanged = {
                            onEvent(NewMatchEvent.TeamToggled(player.id, selectedTeam))
                        },
                        onGoalkeeperToggled = {
                            onEvent(NewMatchEvent.GoalkeeperToggled(selectedTeam, player.id))
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TeamPlayerRow(
    player: AdminPlayer,
    isSelected: Boolean,
    isGoalkeeper: Boolean,
    isSaving: Boolean,
    onSelectedChanged: () -> Unit,
    onGoalkeeperToggled: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isSelected,
            onCheckedChange = { onSelectedChanged() },
            enabled = !isSaving
        )
        Text(player.name, modifier = Modifier.weight(1f))
        if (isSelected) {
            TextButton(
                onClick = onGoalkeeperToggled,
                enabled = !isSaving
            ) {
                Text(if (isGoalkeeper) "🧤 Portero" else "Hacer portero")
            }
        }
    }
}

@Composable
private fun TeamTabs(
    selectedTeam: ActaTeam,
    labelFor: (ActaTeam) -> String,
    onTeamSelected: (ActaTeam) -> Unit,
    enabled: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ActaTeam.entries.forEach { team ->
            if (team == selectedTeam) {
                Button(
                    onClick = { onTeamSelected(team) },
                    enabled = enabled,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(labelFor(team), maxLines = 1)
                }
            } else {
                OutlinedButton(
                    onClick = { onTeamSelected(team) },
                    enabled = enabled,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(labelFor(team), maxLines = 1)
                }
            }
        }
    }
}

@Composable
private fun GoalSelection(
    uiState: NewMatchUiState,
    selectedTeam: ActaTeam,
    onTeamSelected: (ActaTeam) -> Unit,
    onEvent: (NewMatchEvent) -> Unit
) {
    FootballCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Goles", style = MaterialTheme.typography.titleMedium, color = CrestGold)
            Text(
                "Asigna el marcador entre los goleadores. El portero se elige automáticamente cuando solo hay uno.",
                style = MaterialTheme.typography.bodySmall
            )
            TeamTabs(
                selectedTeam = selectedTeam,
                labelFor = { team ->
                    "Equipo ${team.name} · ${uiState.goalsFor(team)}/${uiState.scoreFor(team) ?: 0}"
                },
                onTeamSelected = onTeamSelected,
                enabled = !uiState.isSaving
            )
            GoalFields(
                team = selectedTeam,
                uiState = uiState,
                onEvent = onEvent
            )
        }
    }
}

@Composable
private fun GoalFields(
    team: ActaTeam,
    uiState: NewMatchUiState,
    onEvent: (NewMatchEvent) -> Unit
) {
    var selectedScorerId by remember(team) { mutableStateOf<String?>(null) }
    var selectedGoalkeeperId by remember(team) { mutableStateOf<String?>(null) }
    var goalCount by remember(team) { mutableStateOf(1) }
    var isOwnGoal by remember(team) { mutableStateOf(false) }

    val scorerTeam = if (isOwnGoal) uiState.oppositeOf(team) else team
    val goalkeeperTeam = uiState.oppositeOf(team)
    val scorers = uiState.playerIdsFor(scorerTeam)
        .mapNotNull { id -> uiState.players.firstOrNull { it.id == id } }
    val goalkeepers = uiState.goalkeeperIdsFor(goalkeeperTeam)
        .mapNotNull { id -> uiState.players.firstOrNull { it.id == id } }
    val goalEntries = uiState.goalEntries.filter { it.team == team }
    val targetGoals = uiState.scoreFor(team) ?: 0
    val assignedGoals = uiState.goalsFor(team)
    val remainingGoals = (targetGoals - assignedGoals).coerceAtLeast(0)
    val effectiveGoalkeeperId = goalkeepers.singleOrNull()?.id
        ?: selectedGoalkeeperId?.takeIf { selectedId ->
            goalkeepers.any { it.id == selectedId }
        }

    LaunchedEffect(isOwnGoal) {
        selectedScorerId = null
        selectedGoalkeeperId = null
    }
    LaunchedEffect(remainingGoals) {
        goalCount = if (remainingGoals > 0) {
            goalCount.coerceIn(1, remainingGoals)
        } else {
            1
        }
    }

    Text(
        "Equipo ${team.name} · $assignedGoals de $targetGoals goles asignados",
        style = MaterialTheme.typography.labelLarge,
        color = CrestGold
    )

    if (remainingGoals > 0) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = isOwnGoal,
                onCheckedChange = { isOwnGoal = it },
                enabled = !uiState.isSaving
            )
            Text("Es un autogol")
        }
        if (isOwnGoal) {
            Text(
                "Elige quién marcó en propia del Equipo ${scorerTeam.name}; el gol suma al Equipo ${team.name}.",
                style = MaterialTheme.typography.bodySmall
            )
        }
        PlayerDropdownField(
            label = if (isOwnGoal) "Jugador que marca en propia" else "Goleador",
            players = scorers,
            selectedPlayerId = selectedScorerId,
            enabled = !uiState.isSaving,
            onPlayerSelected = { selectedScorerId = it }
        )
        when {
            goalkeepers.isEmpty() -> Text(
                "Vuelve a Equipos y selecciona un portero para el Equipo ${goalkeeperTeam.name}.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
            goalkeepers.size == 1 -> Text(
                "Portero: ${goalkeepers.single().name} · selección automática",
                style = MaterialTheme.typography.bodyMedium
            )
            else -> PlayerDropdownField(
                label = "Portero del Equipo ${goalkeeperTeam.name}",
                players = goalkeepers,
                selectedPlayerId = selectedGoalkeeperId,
                enabled = !uiState.isSaving,
                onPlayerSelected = { selectedGoalkeeperId = it }
            )
        }
        GoalCounter(
            goals = goalCount,
            maximumGoals = remainingGoals,
            enabled = !uiState.isSaving,
            onGoalsChanged = { goalCount = it }
        )
        Button(
            onClick = {
                val scorerId = selectedScorerId ?: return@Button
                val goalkeeperId = effectiveGoalkeeperId ?: return@Button
                onEvent(
                    NewMatchEvent.GoalAdded(
                        GoalDraft(
                            scorerPlayerId = scorerId,
                            team = team,
                            count = goalCount,
                            goalkeeperPlayerId = goalkeeperId,
                            isOwnGoal = isOwnGoal
                        )
                    )
                )
                selectedScorerId = null
                goalCount = 1
            },
            enabled = !uiState.isSaving &&
                selectedScorerId != null &&
                effectiveGoalkeeperId != null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isOwnGoal) "Añadir autogol" else "Añadir gol")
        }
    } else if (assignedGoals == targetGoals) {
        Text(
            if (targetGoals == 0) "Este equipo no marcó goles."
            else "Todos los goles del Equipo ${team.name} están asignados.",
            style = MaterialTheme.typography.bodyMedium
        )
    }

    if (goalEntries.isNotEmpty()) {
        HorizontalDivider()
        goalEntries.forEach { goal ->
            val scorer = uiState.players.firstOrNull { it.id == goal.scorerPlayerId }?.name
                ?: "Jugador"
            val goalkeeper = uiState.players.firstOrNull { it.id == goal.goalkeeperPlayerId }?.name
                ?: "Portero"
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "$scorer · ${goal.count} gol${if (goal.count == 1) "" else "es"} a $goalkeeper" +
                        if (goal.isOwnGoal) " · autogol" else "",
                    modifier = Modifier.weight(1f)
                )
                TextButton(
                    onClick = { onEvent(NewMatchEvent.GoalRemoved(goal)) },
                    enabled = !uiState.isSaving
                ) {
                    Text("Borrar")
                }
            }
        }
    }
}

@Composable
private fun PlayerDropdownField(
    label: String,
    players: List<AdminPlayer>,
    selectedPlayerId: String?,
    enabled: Boolean,
    onPlayerSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = players.firstOrNull { it.id == selectedPlayerId }?.name

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { expanded = true },
            enabled = enabled && players.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                selectedName ?: if (players.isEmpty()) "No hay opciones para $label" else label,
                modifier = Modifier.weight(1f)
            )
            Text("▾")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            players.forEach { player ->
                DropdownMenuItem(
                    text = { Text(player.name) },
                    onClick = {
                        onPlayerSelected(player.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun GoalCounter(
    goals: Int,
    maximumGoals: Int,
    enabled: Boolean,
    onGoalsChanged: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Cantidad", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
        OutlinedButton(
            onClick = { onGoalsChanged(goals - 1) },
            enabled = enabled && goals > 1
        ) {
            Text("−")
        }
        Text("$goals", style = MaterialTheme.typography.titleMedium, color = CrestGold)
        Button(
            onClick = { onGoalsChanged(goals + 1) },
            enabled = enabled && goals < maximumGoals
        ) {
            Text("+")
        }
    }
}

private fun NewMatchUiState.canOpen(step: MatchCreationStep): Boolean = when (step) {
    MatchCreationStep.MATCH -> true
    MatchCreationStep.TEAMS -> hasValidMatchBasics
    MatchCreationStep.GOALS -> hasValidMatchBasics && hasValidTeams
}

private fun NewMatchUiState.isComplete(step: MatchCreationStep): Boolean = when (step) {
    MatchCreationStep.MATCH -> hasValidMatchBasics
    MatchCreationStep.TEAMS -> hasValidMatchBasics && hasValidTeams
    MatchCreationStep.GOALS -> hasValidMatchBasics && hasValidTeams && hasValidGoals
}

private fun NewMatchUiState.requirementFor(step: MatchCreationStep): String? = when (step) {
    MatchCreationStep.MATCH -> when {
        !NewMatchUiState.isValidDate(date) -> "selecciona la fecha del partido."
        scoreFor(ActaTeam.A) == null || scoreFor(ActaTeam.B) == null ->
            "introduce un marcador válido para ambos equipos."
        !hasValidPenaltyShootout ->
            "la tanda de penaltis necesita un ganador."
        else -> null
    }
    MatchCreationStep.TEAMS -> when {
        teamAPlayerIds.isEmpty() -> "añade al menos un jugador al Equipo A."
        teamBPlayerIds.isEmpty() -> "añade al menos un jugador al Equipo B."
        goalkeeperAIds.none { it in teamAPlayerIds } ->
            "selecciona un portero para el Equipo A."
        goalkeeperBIds.none { it in teamBPlayerIds } ->
            "selecciona un portero para el Equipo B."
        else -> null
    }
    MatchCreationStep.GOALS -> {
        val teamATarget = scoreFor(ActaTeam.A) ?: 0
        val teamBTarget = scoreFor(ActaTeam.B) ?: 0
        when {
            goalsFor(ActaTeam.A) < teamATarget ->
                "faltan ${teamATarget - goalsFor(ActaTeam.A)} goles por asignar al Equipo A."
            goalsFor(ActaTeam.A) > teamATarget ->
                "hay más goles asignados que los indicados para el Equipo A."
            goalsFor(ActaTeam.B) < teamBTarget ->
                "faltan ${teamBTarget - goalsFor(ActaTeam.B)} goles por asignar al Equipo B."
            goalsFor(ActaTeam.B) > teamBTarget ->
                "hay más goles asignados que los indicados para el Equipo B."
            !hasValidGoals -> "revisa los goleadores y porteros seleccionados."
            else -> null
        }
    }
}

@PreviewTargets
@Composable
private fun NewMatchScreenPreview() {
    val players = listOf(
        AdminPlayer("1", "Arturo", isActive = true),
        AdminPlayer("2", "Marta", isActive = true),
        AdminPlayer("3", "Nico", isActive = true),
        AdminPlayer("4", "Laura", isActive = true)
    )
    val state = NewMatchUiState(
        isCheckingAccess = false,
        isAdmin = true,
        players = players,
        date = "2026-07-22",
        teamAScore = "2",
        teamBScore = "1",
        teamAPlayerIds = listOf("1", "2"),
        teamBPlayerIds = listOf("3", "4"),
        goalkeeperAIds = listOf("1"),
        goalkeeperBIds = listOf("3")
    )

    HattitrikiPreview {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ScreenTitle(
                title = "Nueva acta",
                subtitle = "Completa partido, equipos y goles en tres pasos."
            )
            MatchReportFlow(
                uiState = state,
                currentStep = MatchCreationStep.MATCH,
                onStepSelected = {},
                onEvent = {}
            )
        }
    }
}
