package com.brokechango.hattitriki.feature.teamrandomizer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brokechango.hattitriki.core.data.AdminPlayerRepository
import com.brokechango.hattitriki.core.data.AdminPlayersResult
import com.brokechango.hattitriki.core.data.FootballSnapshotResult
import com.brokechango.hattitriki.core.data.FriendlyFootballRepository
import com.brokechango.hattitriki.core.data.MatchTeamsDraft
import com.brokechango.hattitriki.core.data.MatchTeamsDraftStore
import com.brokechango.hattitriki.core.data.NoOpMatchTeamsDraftStore
import com.brokechango.hattitriki.core.data.playerStats
import com.brokechango.hattitriki.core.model.TeamSide
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TeamRandomizerViewModel(
    private val adminPlayerRepository: AdminPlayerRepository?,
    private val footballRepository: FriendlyFootballRepository?,
    private val matchTeamsDraftStore: MatchTeamsDraftStore = NoOpMatchTeamsDraftStore
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        TeamRandomizerUiState(
            savedDraft = runCatching { matchTeamsDraftStore.load() }.getOrNull()
        )
    )
    val uiState: StateFlow<TeamRandomizerUiState> = _uiState.asStateFlow()

    init {
        reloadRoster()
    }

    fun onEvent(event: TeamRandomizerEvent) {
        when (event) {
            is TeamRandomizerEvent.TogglePlayer -> togglePlayer(event.playerId)
            is TeamRandomizerEvent.TeamCountChanged -> updateTeamCount(event.value)
            TeamRandomizerEvent.Generate -> generateTeams()
            TeamRandomizerEvent.SelectAllPlayers -> selectAllPlayers()
            TeamRandomizerEvent.ClearSelection -> clearSelection()
            TeamRandomizerEvent.SaveDraft -> saveDraft()
            TeamRandomizerEvent.ClearDraft -> clearDraft()
            TeamRandomizerEvent.ToggleStatsBalance -> _uiState.value = _uiState.value.copy(
                balanceStats = !_uiState.value.balanceStats,
                teams = emptyList(),
                draftMessage = null,
                errorMessage = null
            )
            TeamRandomizerEvent.ReloadRoster -> reloadRoster()
        }
    }

    private fun togglePlayer(playerId: String) {
        val state = _uiState.value
        if (state.registeredPlayers.none { it.id == playerId }) return
        val selectedPlayerIds = state.selectedPlayerIds.toMutableSet().apply {
            if (!add(playerId)) remove(playerId)
        }
        _uiState.value = state.copy(
            selectedPlayerIds = selectedPlayerIds,
            teamCount = state.teamCount.coerceAtMost(selectedPlayerIds.size.coerceAtLeast(2)),
            teams = emptyList(),
            draftMessage = null,
            errorMessage = null
        )
    }

    private fun updateTeamCount(value: Int) {
        val state = _uiState.value
        val upperBound = state.participants.size.coerceAtLeast(2)
        _uiState.value = state.copy(
            teamCount = value.coerceIn(2, upperBound),
            teams = emptyList(),
            draftMessage = null,
            errorMessage = null
        )
    }

    private fun selectAllPlayers() {
        val state = _uiState.value
        _uiState.value = state.copy(
            selectedPlayerIds = state.registeredPlayers.map(TeamParticipant::id).toSet(),
            teams = emptyList(),
            draftMessage = null,
            errorMessage = null
        )
    }

    private fun clearSelection() {
        _uiState.value = _uiState.value.copy(
            selectedPlayerIds = emptySet(),
            teamCount = 2,
            teams = emptyList(),
            draftMessage = null,
            errorMessage = null
        )
    }

    private fun generateTeams() {
        val currentState = _uiState.value
        val teamCount = currentState.teamCount
        if (!currentState.canGenerate) {
            _uiState.value = currentState.copy(
                errorMessage = "Selecciona al menos dos jugadores de la plantilla activa."
            )
            return
        }

        val result = runCatching {
            TeamRandomizer.createTeams(
                participants = currentState.participants,
                teamCount = teamCount,
                balanceStats = currentState.balanceStats
            )
        }
        _uiState.value = result.fold(
            onSuccess = { teams ->
                currentState.copy(teams = teams, draftMessage = null, errorMessage = null)
            },
            onFailure = { error ->
                currentState.copy(
                    teams = emptyList(),
                    draftMessage = null,
                    errorMessage = error.message
                )
            }
        )
    }

    private fun saveDraft() {
        val state = _uiState.value
        val requirement = state.saveDraftRequirement
        if (requirement != null) {
            _uiState.value = state.copy(errorMessage = requirement)
            return
        }

        val draft = MatchTeamsDraft(
            teamAPlayerIds = state.teams[0].players.map(TeamParticipant::id),
            teamBPlayerIds = state.teams[1].players.map(TeamParticipant::id)
        )
        runCatching { matchTeamsDraftStore.save(draft) }
            .onSuccess {
                _uiState.value = state.copy(
                    savedDraft = draft,
                    draftMessage = "Equipos guardados como borrador para el próximo partido.",
                    errorMessage = null
                )
            }
            .onFailure { error ->
                _uiState.value = state.copy(
                    draftMessage = null,
                    errorMessage = error.message ?: "No se ha podido guardar el borrador."
                )
            }
    }

    private fun clearDraft() {
        val state = _uiState.value
        runCatching { matchTeamsDraftStore.clear() }
            .onSuccess {
                _uiState.value = state.copy(
                    savedDraft = null,
                    draftMessage = "Borrador de equipos descartado.",
                    errorMessage = null
                )
            }
            .onFailure { error ->
                _uiState.value = state.copy(
                    errorMessage = error.message ?: "No se ha podido descartar el borrador."
                )
            }
    }

    private fun reloadRoster() {
        val playerRepository = adminPlayerRepository
        if (playerRepository == null) {
            _uiState.value = _uiState.value.copy(
                isLoadingRoster = false,
                rosterMessage = "Falta la configuración local de Supabase para cargar la plantilla."
            )
            return
        }

        _uiState.value = _uiState.value.copy(isLoadingRoster = true, rosterMessage = null)
        viewModelScope.launch {
            when (val playersResult = playerRepository.loadPlayers()) {
                is AdminPlayersResult.Success -> {
                    val snapshotResult = footballRepository?.loadSnapshot()
                    val statsByPlayerId = when (snapshotResult) {
                        is FootballSnapshotResult.Success -> {
                            val goalsConcededByPlayerId = mutableMapOf<String, Int>()
                            val matchesPlayedByPlayerId = mutableMapOf<String, Int>()
                            snapshotResult.snapshot.matches.forEach { match ->
                                match.players
                                    .groupBy { participant -> participant.playerId }
                                    .values
                                    .map { participants -> participants.first() }
                                    .forEach { participant ->
                                        val goalsConceded = when (participant.team) {
                                            TeamSide.A -> match.teamBScore
                                            TeamSide.B -> match.teamAScore
                                        }
                                        goalsConcededByPlayerId[participant.playerId] =
                                            (goalsConcededByPlayerId[participant.playerId] ?: 0) + goalsConceded
                                        matchesPlayedByPlayerId[participant.playerId] =
                                            (matchesPlayedByPlayerId[participant.playerId] ?: 0) + 1
                                    }
                            }
                            snapshotResult.snapshot.playerStats().associate { stats ->
                                val matchesPlayed = matchesPlayedByPlayerId[stats.player.id] ?: 0
                                val goalsConcededPerMatch = if (matchesPlayed == 0) {
                                    0.0
                                } else {
                                    (goalsConcededByPlayerId[stats.player.id] ?: 0).toDouble() / matchesPlayed
                                }
                                stats.player.id to TeamRandomizer.statsScore(
                                    goals = stats.goals,
                                    wins = stats.wins,
                                    matchesPlayed = stats.matchesPlayed,
                                    goalkeeperMatches = stats.goalkeeperMatches,
                                    goalsConcededPerMatch = goalsConcededPerMatch
                                    )
                                }
                        }
                        else -> emptyMap()
                    }
                    val currentState = _uiState.value
                    val activePlayers = playersResult.players
                        .filter { it.isActive }
                        .sortedBy { it.name.lowercase() }
                        .map { player ->
                            TeamParticipant(
                                id = player.id,
                                name = player.name,
                                hasCardio = player.hasCardio,
                                statsScore = statsByPlayerId[player.id] ?: 0.0
                            )
                        }
                    val activePlayerIds = activePlayers.map(TeamParticipant::id).toSet()
                    val selectedPlayerIds = if (currentState.registeredPlayers.isEmpty()) {
                        activePlayerIds
                    } else {
                        currentState.selectedPlayerIds.intersect(activePlayerIds)
                    }
                    _uiState.value = currentState.copy(
                        isLoadingRoster = false,
                        registeredPlayers = activePlayers,
                        selectedPlayerIds = selectedPlayerIds,
                        teamCount = currentState.teamCount.coerceAtMost(
                            selectedPlayerIds.size.coerceAtLeast(2)
                        ),
                        teams = emptyList(),
                        statsAvailable = snapshotResult is FootballSnapshotResult.Success,
                        balanceStats = currentState.balanceStats &&
                            snapshotResult is FootballSnapshotResult.Success,
                        rosterMessage = when (snapshotResult) {
                            null -> "La plantilla está lista, pero faltan las estadísticas de la liga."
                            is FootballSnapshotResult.Failure -> "La plantilla está lista, pero no se han podido cargar las estadísticas: ${snapshotResult.message}"
                            is FootballSnapshotResult.Success -> null
                        }
                    )
                }
                AdminPlayersResult.Unauthorized -> _uiState.value = _uiState.value.copy(
                    isLoadingRoster = false,
                    rosterMessage = "Tu sesión ya no tiene permisos de administrador."
                )
                is AdminPlayersResult.Failure -> _uiState.value = _uiState.value.copy(
                    isLoadingRoster = false,
                    rosterMessage = playersResult.message
                )
            }
        }
    }
}
