package com.brokechango.hattitriki.feature.newmatch

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
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
import androidx.compose.runtime.setValue
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

@Composable
fun NewMatchScreen(
    viewModel: NewMatchViewModel,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onSaved()
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ScreenTitle(
            title = if (uiState.isEditing) "Editar acta" else "Nueva acta",
            subtitle = if (uiState.isEditing) "Modifica el resultado, alineaciones, porteros y goleadores." else "Resultado, alineaciones, porteros y goleadores."
        )

        when {
            uiState.isCheckingAccess -> Text("Comprobando permisos…")
            !uiState.isAdmin -> AccessDenied(uiState.errorMessage)
            uiState.isLoadingPlayers -> Text("Cargando plantilla…")
            else -> MatchReportForm(uiState, viewModel::onEvent)
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
private fun MatchReportForm(uiState: NewMatchUiState, onEvent: (NewMatchEvent) -> Unit) {
    MatchBasics(uiState, onEvent)
    TeamAssignment(uiState, onEvent)
    if (uiState.selectedPlayerIds.isNotEmpty()) {
        GoalkeeperSelection(uiState, onEvent)
        GoalSelection(uiState, onEvent)
    }

    FootballCard(modifier = Modifier.fillMaxWidth(), highlight = true) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("No hace falta alinear a toda la plantilla. El total de goles debe coincidir con el marcador.")
            uiState.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button(
                onClick = { onEvent(NewMatchEvent.Submit) },
                enabled = uiState.canSubmit,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    when {
                        uiState.isSaving && uiState.isEditing -> "Actualizando acta…"
                        uiState.isSaving -> "Guardando acta…"
                        uiState.isEditing -> "Guardar cambios"
                        else -> "Guardar acta del partido"
                    }
                )
            }
        }
    }
}

@Composable
private fun MatchBasics(uiState: NewMatchUiState, onEvent: (NewMatchEvent) -> Unit) {
    FootballCard(modifier = Modifier.fillMaxWidth(), highlight = true) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Datos del partido", style = MaterialTheme.typography.titleMedium, color = CrestGold)
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
                    label = { Text("Goles equipo A") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    enabled = !uiState.isSaving,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = uiState.teamBScore,
                    onValueChange = { onEvent(NewMatchEvent.TeamBScoreChanged(it)) },
                    label = { Text("Goles equipo B") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    enabled = !uiState.isSaving,
                    modifier = Modifier.weight(1f)
                )
            }
            PenaltyShootoutFields(uiState, onEvent)
        }
    }
}

@Composable
private fun PenaltyShootoutFields(uiState: NewMatchUiState, onEvent: (NewMatchEvent) -> Unit) {
    OutlinedButton(
        onClick = { onEvent(NewMatchEvent.PenaltyShootoutToggled) },
        enabled = !uiState.isSaving && uiState.isRegularScoreDraw,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(if (uiState.isPenaltyShootout) "✓ Decidido en penaltis" else "¿Se decidió en penaltis?")
    }
    if (!uiState.isRegularScoreDraw) {
        Text("La tanda solo está disponible cuando el partido termina empatado.", style = MaterialTheme.typography.bodySmall)
    }
    if (uiState.isPenaltyShootout) {
        Text("Resultado de la tanda", style = MaterialTheme.typography.labelLarge, color = CrestGold)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = uiState.teamAPenaltyScore,
                onValueChange = { onEvent(NewMatchEvent.TeamAPenaltyScoreChanged(it)) },
                label = { Text("Penaltis A") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                enabled = !uiState.isSaving,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = uiState.teamBPenaltyScore,
                onValueChange = { onEvent(NewMatchEvent.TeamBPenaltyScoreChanged(it)) },
                label = { Text("Penaltis B") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                enabled = !uiState.isSaving,
                modifier = Modifier.weight(1f)
            )
        }
        Text("La tanda debe tener un ganador y no cuenta como goles del partido.", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun TeamAssignment(uiState: NewMatchUiState, onEvent: (NewMatchEvent) -> Unit) {
    FootballCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Alineaciones", style = MaterialTheme.typography.titleMedium, color = CrestGold)
            Text("A y B son independientes: un jugador puede participar en los dos equipos. Por defecto, todos están fuera.")
            Text(
                "Equipo A: ${uiState.teamAPlayerIds.size} · Equipo B: ${uiState.teamBPlayerIds.size} · Fuera: ${uiState.players.size - uiState.selectedPlayerIds.size}",
                style = MaterialTheme.typography.labelLarge,
                color = CrestGold
            )
            if (uiState.players.isEmpty()) {
                Text("No hay jugadores en Supabase. Añádelos desde Zona míster.")
            } else {
                uiState.players.forEach { player ->
                    TeamAssignmentRow(
                        player = player,
                        isInTeamA = uiState.isOnTeam(player.id, ActaTeam.A),
                        isInTeamB = uiState.isOnTeam(player.id, ActaTeam.B),
                        isSaving = uiState.isSaving,
                        onTeamToggled = { team -> onEvent(NewMatchEvent.TeamToggled(player.id, team)) },
                        onSetOutside = { onEvent(NewMatchEvent.PlayerSetOutside(player.id)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TeamAssignmentRow(
    player: AdminPlayer,
    isInTeamA: Boolean,
    isInTeamB: Boolean,
    isSaving: Boolean,
    onTeamToggled: (ActaTeam) -> Unit,
    onSetOutside: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Text(player.name, modifier = Modifier.weight(1f))
        TeamAssignmentButton("A", isInTeamA, isSaving) { onTeamToggled(ActaTeam.A) }
        TeamAssignmentButton("B", isInTeamB, isSaving) { onTeamToggled(ActaTeam.B) }
        TeamAssignmentButton("Fuera", !isInTeamA && !isInTeamB, isSaving, onSetOutside)
    }
}

@Composable
private fun TeamAssignmentButton(
    label: String,
    selected: Boolean,
    isSaving: Boolean,
    onClick: () -> Unit
) {
    if (selected) {
        Button(onClick = onClick, enabled = !isSaving) { Text(label) }
    } else {
        OutlinedButton(onClick = onClick, enabled = !isSaving) { Text(label) }
    }
}

@Composable
private fun GoalkeeperSelection(uiState: NewMatchUiState, onEvent: (NewMatchEvent) -> Unit) {
    FootballCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("¿Quién recibe los goles?", style = MaterialTheme.typography.titleMedium, color = CrestGold)
            Text("Elige uno o más porteros por equipo. Cada gol se atribuye al portero rival que lo recibió.")
            TeamRoster("Equipo A", uiState.teamAPlayerIds, uiState.players, uiState.goalkeeperAIds, ActaTeam.A, onEvent, uiState.isSaving)
            TeamRoster("Equipo B", uiState.teamBPlayerIds, uiState.players, uiState.goalkeeperBIds, ActaTeam.B, onEvent, uiState.isSaving)
        }
    }
}

@Composable
private fun TeamRoster(title: String, playerIds: List<String>, players: List<AdminPlayer>, goalkeeperIds: List<String>, team: ActaTeam, onEvent: (NewMatchEvent) -> Unit, isSaving: Boolean) {
    Text(title, style = MaterialTheme.typography.labelLarge, color = CrestGold)
    playerIds.mapNotNull { id -> players.firstOrNull { it.id == id } }.forEach { player ->
        OutlinedButton(
            onClick = { onEvent(NewMatchEvent.GoalkeeperToggled(team, player.id)) },
            enabled = !isSaving,
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (player.id in goalkeeperIds) "🧤 ${player.name}" else player.name) }
    }
}

@Composable
private fun GoalSelection(uiState: NewMatchUiState, onEvent: (NewMatchEvent) -> Unit) {
    FootballCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("¿Quién marca y cuántos?", style = MaterialTheme.typography.titleMedium, color = CrestGold)
            GoalFields(
                team = ActaTeam.A,
                playerIds = uiState.teamAPlayerIds,
                uiState = uiState,
                opposingGoalkeeperIds = uiState.goalkeeperBIds,
                onEvent = onEvent
            )
            GoalFields(
                team = ActaTeam.B,
                playerIds = uiState.teamBPlayerIds,
                uiState = uiState,
                opposingGoalkeeperIds = uiState.goalkeeperAIds,
                onEvent = onEvent
            )
        }
    }
}

@Composable
private fun GoalFields(
    team: ActaTeam,
    playerIds: List<String>,
    uiState: NewMatchUiState,
    opposingGoalkeeperIds: List<String>,
    onEvent: (NewMatchEvent) -> Unit
) {
    var selectedScorerId by remember(team) { mutableStateOf<String?>(null) }
    var selectedGoalkeeperId by remember(team) { mutableStateOf<String?>(null) }
    var goalCount by remember(team) { mutableStateOf(1) }
    var isOwnGoal by remember(team) { mutableStateOf(false) }
    val goalkeeperIds = if (isOwnGoal) uiState.goalkeeperIdsFor(team) else opposingGoalkeeperIds
    val scorers = playerIds.mapNotNull { id -> uiState.players.firstOrNull { it.id == id } }
    val goalkeepers = goalkeeperIds.mapNotNull { id -> uiState.players.firstOrNull { it.id == id } }
    // A goal is listed with the team that receives it. For example, an own
    // goal by a player from A is shown in B, even though it is selected from A.
    val goalEntries = uiState.goalEntries.filter { it.team == team }

    LaunchedEffect(isOwnGoal) {
        selectedScorerId = null
        selectedGoalkeeperId = null
    }

    Text("Equipo ${team.name} · ${uiState.goalsFor(team)} goles", style = MaterialTheme.typography.labelLarge, color = CrestGold)
    Text("Puedes elegir a cualquier jugador alineado, incluidos los porteros.", style = MaterialTheme.typography.bodySmall)
    OutlinedButton(
        onClick = { isOwnGoal = !isOwnGoal },
        enabled = !uiState.isSaving,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(if (isOwnGoal) "✓ Autogol" else "Marcar como autogol")
    }
    if (isOwnGoal) {
        Text(
            "Elige un jugador y un portero de este equipo. El gol sumará al Equipo ${uiState.oppositeOf(team).name}.",
            style = MaterialTheme.typography.bodySmall
        )
    }
    if (goalkeepers.isEmpty()) {
        Text(
            if (isOwnGoal) "Primero elige un portero de este equipo." else "Primero elige un portero del equipo rival.",
            style = MaterialTheme.typography.bodySmall
        )
        return
    }

    Text(if (isOwnGoal) "¿Quién marca en propia?" else "¿Quién ha marcado?", style = MaterialTheme.typography.bodySmall)
    scorers.forEach { player ->
        OutlinedButton(
            onClick = { selectedScorerId = player.id },
            enabled = !uiState.isSaving,
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (player.id == selectedScorerId) "✓ ${player.name}" else player.name) }
    }
    Text(if (isOwnGoal) "¿A qué portero de este equipo?" else "¿A quién?", style = MaterialTheme.typography.bodySmall)
    goalkeepers.forEach { goalkeeper ->
        OutlinedButton(
            onClick = { selectedGoalkeeperId = goalkeeper.id },
            enabled = !uiState.isSaving,
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (goalkeeper.id == selectedGoalkeeperId) "✓ ${goalkeeper.name}" else goalkeeper.name) }
    }
    GoalCounter(
        goals = goalCount,
        enabled = !uiState.isSaving,
        onGoalsChanged = { goalCount = it }
    )
    Button(
        onClick = {
            val scorerId = selectedScorerId ?: return@Button
            val goalkeeperId = selectedGoalkeeperId ?: return@Button
            val scoringTeam = if (isOwnGoal) uiState.oppositeOf(team) else team
            onEvent(NewMatchEvent.GoalAdded(GoalDraft(scorerId, scoringTeam, goalCount, goalkeeperId, isOwnGoal)))
            goalCount = 1
        },
        enabled = !uiState.isSaving && selectedScorerId != null && selectedGoalkeeperId != null,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            if (isOwnGoal) "Añadir autogol para Equipo ${uiState.oppositeOf(team).name}"
            else "Añadir gol"
        )
    }

    goalEntries.forEach { goal ->
        val scorer = uiState.players.firstOrNull { it.id == goal.scorerPlayerId }?.name ?: "Jugador"
        val goalkeeper = uiState.players.firstOrNull { it.id == goal.goalkeeperPlayerId }?.name ?: "Portero"
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text(
                "$scorer · ${goal.count} gol${if (goal.count == 1) "" else "es"} a $goalkeeper${if (goal.isOwnGoal) " (autogol)" else ""}",
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = { onEvent(NewMatchEvent.GoalRemoved(goal)) }, enabled = !uiState.isSaving) {
                Text("Borrar")
            }
        }
    }
}

@Composable
private fun GoalCounter(
    goals: Int,
    enabled: Boolean,
    onGoalsChanged: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Text("¿Cuántos?", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
        OutlinedButton(onClick = { onGoalsChanged(goals - 1) }, enabled = enabled && goals > 0) {
            Text("−")
        }
        Text("$goals", style = MaterialTheme.typography.titleMedium, color = CrestGold)
        Button(onClick = { onGoalsChanged(goals + 1) }, enabled = enabled) {
            Text("+")
        }
    }
}
