package com.brokechango.hattitriki.feature.newmatch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brokechango.hattitriki.core.data.ActaGoal
import com.brokechango.hattitriki.core.data.ActaParticipant
import com.brokechango.hattitriki.core.data.ActaTeam
import com.brokechango.hattitriki.core.data.AdminMatchRepository
import com.brokechango.hattitriki.core.data.CreateMatchResult
import com.brokechango.hattitriki.core.data.EditMatchResult
import com.brokechango.hattitriki.core.data.LoadMatchResult
import com.brokechango.hattitriki.core.data.LoadPlayersResult
import com.brokechango.hattitriki.core.data.MatchReportDraft
import com.brokechango.hattitriki.core.data.MatchTeamsDraftStore
import com.brokechango.hattitriki.core.data.NoOpMatchTeamsDraftStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NewMatchViewModel(
    private val repository: AdminMatchRepository?,
    private val matchId: String? = null,
    private val matchTeamsDraftStore: MatchTeamsDraftStore = NoOpMatchTeamsDraftStore
) : ViewModel() {
    private val _uiState = MutableStateFlow(NewMatchUiState(editingMatchId = matchId))
    val uiState: StateFlow<NewMatchUiState> = _uiState.asStateFlow()

    init {
        checkAccessAndLoadPlayers()
    }

    fun onEvent(event: NewMatchEvent) {
        when (event) {
            is NewMatchEvent.DateChanged -> updateForm(date = event.value)
            is NewMatchEvent.TeamAScoreChanged -> updateForm(teamAScore = event.value)
            is NewMatchEvent.TeamBScoreChanged -> updateForm(teamBScore = event.value)
            NewMatchEvent.PenaltyShootoutToggled -> togglePenaltyShootout()
            is NewMatchEvent.TeamAPenaltyScoreChanged -> updatePenaltyScores(teamAScore = event.value)
            is NewMatchEvent.TeamBPenaltyScoreChanged -> updatePenaltyScores(teamBScore = event.value)
            is NewMatchEvent.TeamToggled -> togglePlayerTeam(event.playerId, event.team)
            is NewMatchEvent.PlayerSetOutside -> setPlayerOutside(event.playerId)
            is NewMatchEvent.GoalkeeperToggled -> toggleGoalkeeper(event.team, event.playerId)
            is NewMatchEvent.GoalAdded -> addGoal(event.goal)
            is NewMatchEvent.GoalRemoved -> removeGoal(event.goal)
            NewMatchEvent.DiscardTeamsDraft -> discardTeamsDraft()
            NewMatchEvent.Submit -> save()
        }
    }

    private fun checkAccessAndLoadPlayers() {
        val matchRepository = repository
        if (matchRepository == null) {
            _uiState.value = _uiState.value.copy(
                isCheckingAccess = false,
                errorMessage = "Falta la configuración local de Supabase en este dispositivo."
            )
            return
        }

        viewModelScope.launch {
            if (!matchRepository.hasActiveAdminSession()) {
                _uiState.value = _uiState.value.copy(isCheckingAccess = false)
                return@launch
            }

            _uiState.value = _uiState.value.copy(
                isCheckingAccess = false,
                isLoadingPlayers = true,
                isAdmin = true
            )
            when (val result = matchRepository.loadActivePlayers()) {
                is LoadPlayersResult.Success -> {
                    val loadedState = _uiState.value.copy(
                        players = result.players,
                        isLoadingPlayers = false
                    )
                    _uiState.value = if (matchId == null) {
                        applySavedTeamsDraft(loadedState)
                    } else {
                        loadedState.also { loadExistingMatch(matchId) }
                    }
                }
                LoadPlayersResult.Unauthorized -> _uiState.value = _uiState.value.copy(
                    isLoadingPlayers = false,
                    isAdmin = false,
                    errorMessage = "Tu sesión ya no tiene permisos de administrador."
                )
                is LoadPlayersResult.Failure -> _uiState.value = _uiState.value.copy(
                    isLoadingPlayers = false,
                    errorMessage = result.message
                )
            }
        }
    }

    private fun applySavedTeamsDraft(state: NewMatchUiState): NewMatchUiState {
        val draft = runCatching { matchTeamsDraftStore.load() }.getOrNull() ?: return state
        val activePlayerIds = state.players.map { it.id }.toSet()
        val teamAPlayerIds = draft.teamAPlayerIds.filter(activePlayerIds::contains).distinct()
        val teamBPlayerIds = draft.teamBPlayerIds.filter(activePlayerIds::contains).distinct()
        val missingPlayers = draft.teamAPlayerIds.size + draft.teamBPlayerIds.size -
            teamAPlayerIds.size - teamBPlayerIds.size

        return state.copy(
            teamAPlayerIds = teamAPlayerIds,
            teamBPlayerIds = teamBPlayerIds,
            teamsDraftMessage = if (missingPlayers == 0) {
                "Se han cargado los equipos guardados en el generador."
            } else {
                "Se ha cargado el borrador, pero $missingPlayers jugador" +
                    if (missingPlayers == 1) {
                        " ya no está activo. Revisa los equipos."
                    } else {
                        "es ya no están activos. Revisa los equipos."
                    }
            }
        )
    }

    private fun loadExistingMatch(id: String) {
        val matchRepository = repository ?: return
        viewModelScope.launch {
            when (val result = matchRepository.loadMatch(id)) {
                is LoadMatchResult.Success -> {
                    val participantsA = result.report.participants.filter { it.team == ActaTeam.A.name }
                    val participantsB = result.report.participants.filter { it.team == ActaTeam.B.name }
                    _uiState.value = _uiState.value.copy(
                        date = result.report.matchDate,
                        teamAScore = result.report.teamAScore.toString(),
                        teamBScore = result.report.teamBScore.toString(),
                        isPenaltyShootout = result.report.teamAPenaltyScore != null && result.report.teamBPenaltyScore != null,
                        teamAPenaltyScore = result.report.teamAPenaltyScore?.toString().orEmpty(),
                        teamBPenaltyScore = result.report.teamBPenaltyScore?.toString().orEmpty(),
                        teamAPlayerIds = participantsA.map { it.playerId },
                        teamBPlayerIds = participantsB.map { it.playerId },
                        goalkeeperAIds = participantsA.filter { it.wasGoalkeeper }.map { it.playerId },
                        goalkeeperBIds = participantsB.filter { it.wasGoalkeeper }.map { it.playerId },
                        goalEntries = result.report.goals.map { goal ->
                            GoalDraft(
                                scorerPlayerId = goal.playerId,
                                team = ActaTeam.valueOf(goal.team),
                                count = goal.count,
                                goalkeeperPlayerId = goal.goalkeeperId,
                                isOwnGoal = goal.isOwnGoal
                            )
                        }
                    )
                }
                LoadMatchResult.NotFound -> _uiState.value = _uiState.value.copy(errorMessage = "No se ha encontrado el partido.")
                LoadMatchResult.Unauthorized -> _uiState.value = _uiState.value.copy(
                    isAdmin = false,
                    errorMessage = "Tu sesión ya no tiene permisos de administrador."
                )
                is LoadMatchResult.Failure -> _uiState.value = _uiState.value.copy(errorMessage = result.message)
            }
        }
    }

    private fun updateForm(
        date: String = _uiState.value.date,
        teamAScore: String = _uiState.value.teamAScore,
        teamBScore: String = _uiState.value.teamBScore
    ) {
        val isRegularScoreDraw = teamAScore.toIntOrNull()?.takeIf { it >= 0 } ==
            teamBScore.toIntOrNull()?.takeIf { it >= 0 } &&
            teamAScore.toIntOrNull()?.takeIf { it >= 0 } != null
        _uiState.value = _uiState.value.copy(
            date = date,
            teamAScore = teamAScore,
            teamBScore = teamBScore,
            isPenaltyShootout = _uiState.value.isPenaltyShootout && isRegularScoreDraw,
            teamAPenaltyScore = if (isRegularScoreDraw) _uiState.value.teamAPenaltyScore else "",
            teamBPenaltyScore = if (isRegularScoreDraw) _uiState.value.teamBPenaltyScore else "",
            errorMessage = null
        )
    }

    private fun togglePenaltyShootout() {
        val state = _uiState.value
        if (!state.isRegularScoreDraw) return
        _uiState.value = if (state.isPenaltyShootout) {
            state.copy(
                isPenaltyShootout = false,
                teamAPenaltyScore = "",
                teamBPenaltyScore = "",
                errorMessage = null
            )
        } else {
            state.copy(isPenaltyShootout = true, errorMessage = null)
        }
    }

    private fun updatePenaltyScores(
        teamAScore: String = _uiState.value.teamAPenaltyScore,
        teamBScore: String = _uiState.value.teamBPenaltyScore
    ) {
        if (!_uiState.value.isPenaltyShootout) return
        _uiState.value = _uiState.value.copy(
            teamAPenaltyScore = teamAScore,
            teamBPenaltyScore = teamBScore,
            errorMessage = null
        )
    }

    private fun togglePlayerTeam(playerId: String, team: ActaTeam) {
        val state = _uiState.value
        val teamAPlayerIds = when (team) {
            ActaTeam.A -> state.teamAPlayerIds.toggle(playerId)
            ActaTeam.B -> state.teamAPlayerIds
        }
        val teamBPlayerIds = when (team) {
            ActaTeam.A -> state.teamBPlayerIds
            ActaTeam.B -> state.teamBPlayerIds.toggle(playerId)
        }
        applyPlayerAssignments(state, teamAPlayerIds, teamBPlayerIds)
    }

    private fun setPlayerOutside(playerId: String) {
        val state = _uiState.value
        applyPlayerAssignments(
            state,
            state.teamAPlayerIds - playerId,
            state.teamBPlayerIds - playerId
        )
    }

    private fun applyPlayerAssignments(
        state: NewMatchUiState,
        teamAPlayerIds: List<String>,
        teamBPlayerIds: List<String>
    ) {
        val assignedState = state.copy(
            teamAPlayerIds = teamAPlayerIds,
            teamBPlayerIds = teamBPlayerIds,
            goalkeeperAIds = state.goalkeeperAIds.filter { it in teamAPlayerIds },
            goalkeeperBIds = state.goalkeeperBIds.filter { it in teamBPlayerIds }
        )
        _uiState.value = assignedState.copy(
            goalEntries = assignedState.goalEntries.filter { goal ->
                val scorerTeam = if (goal.isOwnGoal) assignedState.oppositeOf(goal.team) else goal.team
                val goalkeeperTeam = if (goal.isOwnGoal) scorerTeam else assignedState.oppositeOf(goal.team)
                assignedState.isOnTeam(goal.scorerPlayerId, scorerTeam) &&
                    goal.goalkeeperPlayerId in assignedState.goalkeeperIdsFor(goalkeeperTeam)
            },
            errorMessage = null
        )
    }

    private fun toggleGoalkeeper(team: ActaTeam, playerId: String) {
        val state = _uiState.value
        if (!state.isOnTeam(playerId, team)) return
        val currentGoalkeeperIds = state.goalkeeperIdsFor(team)
        val updatedGoalkeeperIds = if (playerId in currentGoalkeeperIds) {
            currentGoalkeeperIds - playerId
        } else {
            currentGoalkeeperIds + playerId
        }
        val invalidTargetIds = currentGoalkeeperIds.filterNot { it in updatedGoalkeeperIds }.toSet()
        _uiState.value = when (team) {
            ActaTeam.A -> state.copy(
                goalkeeperAIds = updatedGoalkeeperIds,
                goalEntries = state.goalEntries.filter { it.goalkeeperPlayerId !in invalidTargetIds },
                errorMessage = null
            )
            ActaTeam.B -> state.copy(
                goalkeeperBIds = updatedGoalkeeperIds,
                goalEntries = state.goalEntries.filter { it.goalkeeperPlayerId !in invalidTargetIds },
                errorMessage = null
            )
        }
    }

    private fun addGoal(goal: GoalDraft) {
        val state = _uiState.value
        val scorerTeam = if (goal.isOwnGoal) state.oppositeOf(goal.team) else goal.team
        val goalkeeperTeam = if (goal.isOwnGoal) scorerTeam else state.oppositeOf(goal.team)
        if (goal.count <= 0 || !state.isOnTeam(goal.scorerPlayerId, scorerTeam) ||
            goal.goalkeeperPlayerId !in state.goalkeeperIdsFor(goalkeeperTeam)
        ) return
        _uiState.value = _uiState.value.copy(
            goalEntries = _uiState.value.goalEntries + goal,
            errorMessage = null
        )
    }

    private fun removeGoal(goal: GoalDraft) {
        _uiState.value = _uiState.value.copy(goalEntries = _uiState.value.goalEntries - goal)
    }

    private fun discardTeamsDraft() {
        val state = _uiState.value
        runCatching { matchTeamsDraftStore.clear() }
            .onSuccess {
                _uiState.value = state.copy(
                    teamAPlayerIds = emptyList(),
                    teamBPlayerIds = emptyList(),
                    goalkeeperAIds = emptyList(),
                    goalkeeperBIds = emptyList(),
                    goalEntries = emptyList(),
                    teamsDraftMessage = null,
                    errorMessage = null
                )
            }
            .onFailure { error ->
                _uiState.value = state.copy(
                    errorMessage = error.message ?: "No se ha podido descartar el borrador."
                )
            }
    }

    private fun save() {
        val state = _uiState.value
        val matchRepository = repository ?: return
        if (!state.canSubmit) {
            _uiState.value = state.copy(
                errorMessage = "Completa fecha, marcador, equipos, porteros y goles. Los goles deben coincidir con el resultado."
            )
            return
        }

        val teamAIds = state.teamAPlayerIds
        val teamBIds = state.teamBPlayerIds
        val draft = MatchReportDraft(
            matchDate = state.date,
            teamAScore = state.teamAScore.toInt(),
            teamBScore = state.teamBScore.toInt(),
            teamAPenaltyScore = state.teamAPenaltyScore.toIntOrNull().takeIf { state.isPenaltyShootout },
            teamBPenaltyScore = state.teamBPenaltyScore.toIntOrNull().takeIf { state.isPenaltyShootout },
            participants = teamAIds.map { playerId ->
                ActaParticipant(playerId, ActaTeam.A.name, playerId in state.goalkeeperAIds)
            } + teamBIds.map { playerId ->
                ActaParticipant(playerId, ActaTeam.B.name, playerId in state.goalkeeperBIds)
            },
            goals = state.goalEntries.map { goal ->
                ActaGoal(goal.scorerPlayerId, goal.team.name, goal.count, goal.goalkeeperPlayerId, goal.isOwnGoal)
            }
        )

        _uiState.value = state.copy(isSaving = true, errorMessage = null)
        viewModelScope.launch {
            val result = state.editingMatchId?.let { matchRepository.updateMatch(it, draft) }
                ?: when (val createResult = matchRepository.createMatch(draft)) {
                    CreateMatchResult.Success -> EditMatchResult.Success
                    CreateMatchResult.Unauthorized -> EditMatchResult.Unauthorized
                    is CreateMatchResult.Failure -> EditMatchResult.Failure(createResult.message)
            }
            when (result) {
                EditMatchResult.Success -> {
                    if (state.editingMatchId == null) {
                        runCatching { matchTeamsDraftStore.clear() }
                    }
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        isSaved = true
                    )
                }
                EditMatchResult.Unauthorized -> _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    isAdmin = false,
                    errorMessage = "Tu sesión ya no tiene permisos de administrador."
                )
                is EditMatchResult.Failure -> _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = result.message
                )
            }
        }
    }

}

private fun List<String>.toggle(playerId: String): List<String> =
    if (playerId in this) this - playerId else this + playerId
