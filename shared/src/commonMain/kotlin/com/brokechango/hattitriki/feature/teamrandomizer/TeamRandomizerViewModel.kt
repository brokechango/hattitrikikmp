package com.brokechango.hattitriki.feature.teamrandomizer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brokechango.hattitriki.core.data.AdminPlayerRepository
import com.brokechango.hattitriki.core.data.AdminPlayersResult
import com.brokechango.hattitriki.core.data.FootballSnapshotResult
import com.brokechango.hattitriki.core.data.FriendlyFootballRepository
import com.brokechango.hattitriki.core.data.playerStats
import com.brokechango.hattitriki.core.model.TeamSide
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TeamRandomizerViewModel(
    private val adminPlayerRepository: AdminPlayerRepository?,
    private val footballRepository: FriendlyFootballRepository?
) : ViewModel() {
    private val _uiState = MutableStateFlow(TeamRandomizerUiState())
    val uiState: StateFlow<TeamRandomizerUiState> = _uiState.asStateFlow()

    init {
        reloadRoster()
    }

    fun onEvent(event: TeamRandomizerEvent) {
        when (event) {
            is TeamRandomizerEvent.ParticipantsChanged -> updateInput(participantInput = event.value)
            is TeamRandomizerEvent.TeamCountChanged -> updateInput(teamCountInput = event.value)
            TeamRandomizerEvent.Generate -> generateTeams()
            TeamRandomizerEvent.UseActiveRoster -> useActiveRoster()
            TeamRandomizerEvent.ToggleStatsBalance -> _uiState.value = _uiState.value.copy(
                balanceStats = !_uiState.value.balanceStats,
                teams = emptyList(),
                errorMessage = null
            )
            TeamRandomizerEvent.ReloadRoster -> reloadRoster()
            TeamRandomizerEvent.LoadExample -> {
                _uiState.value = _uiState.value.copy(
                    participantInput = "Alex\nBruno\nCarmen\nDani\nElena\nFran\nGabi\nHugo\nInés\nJavi",
                    teamCountInput = "2",
                    teams = emptyList(),
                    errorMessage = null
                )
            }
            TeamRandomizerEvent.Clear -> _uiState.value = _uiState.value.copy(
                participantInput = "",
                teams = emptyList(),
                errorMessage = null
            )
        }
    }

    private fun updateInput(participantInput: String? = null, teamCountInput: String? = null) {
        _uiState.value = _uiState.value.copy(
            participantInput = participantInput ?: _uiState.value.participantInput,
            teamCountInput = teamCountInput ?: _uiState.value.teamCountInput,
            teams = emptyList(),
            errorMessage = null
        )
    }

    private fun generateTeams() {
        val currentState = _uiState.value
        val teamCount = currentState.teamCount
        if (teamCount == null) {
            _uiState.value = currentState.copy(errorMessage = "Indica un número válido de equipos.")
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
            onSuccess = { teams -> currentState.copy(teams = teams, errorMessage = null) },
            onFailure = { error -> currentState.copy(teams = emptyList(), errorMessage = error.message) }
        )
    }

    private fun useActiveRoster() {
        val currentState = _uiState.value
        if (currentState.registeredPlayers.isEmpty()) {
            _uiState.value = currentState.copy(
                errorMessage = "No hay jugadores activos disponibles. Actualiza la plantilla o añade jugadores."
            )
            return
        }
        _uiState.value = currentState.copy(
            participantInput = currentState.registeredPlayers.joinToString("\n") { it.name },
            teams = emptyList(),
            errorMessage = null
        )
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
                    _uiState.value = _uiState.value.copy(
                        isLoadingRoster = false,
                        registeredPlayers = playersResult.players
                            .filter { it.isActive }
                            .sortedBy { it.name.lowercase() }
                            .map { player ->
                                TeamParticipant(
                                    id = player.id,
                                    name = player.name,
                                    hasCardio = player.hasCardio,
                                    statsScore = statsByPlayerId[player.id] ?: 0.0
                                )
                            },
                        statsAvailable = snapshotResult is FootballSnapshotResult.Success,
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
