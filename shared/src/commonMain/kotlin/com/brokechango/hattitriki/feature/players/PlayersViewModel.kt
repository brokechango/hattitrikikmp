package com.brokechango.hattitriki.feature.players

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brokechango.hattitriki.core.data.FootballSnapshot
import com.brokechango.hattitriki.core.data.FootballSnapshotResult
import com.brokechango.hattitriki.core.data.FriendlyFootballRepository
import com.brokechango.hattitriki.core.data.PlayerAvatarUrlsResult
import com.brokechango.hattitriki.core.data.PlayerProfileRepository
import com.brokechango.hattitriki.core.data.playerStats
import com.brokechango.hattitriki.core.data.playerRankingMetricsByPlayerId
import com.brokechango.hattitriki.core.model.PlayerRankingCategory
import com.brokechango.hattitriki.core.model.PlayerStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlayersViewModel(
    private val repository: FriendlyFootballRepository? = null,
    private val profileRepository: PlayerProfileRepository? = null,
    initialSnapshot: FootballSnapshot? = null
) : ViewModel() {
    private var rankingsByCategory: Map<PlayerRankingCategory, List<PlayerRankingEntry>> = emptyMap()

    private val _uiState = MutableStateFlow(PlayersUiState())
    val uiState: StateFlow<PlayersUiState> = _uiState.asStateFlow()

    init {
        if (initialSnapshot != null) {
            applyPreparedRankings(initialSnapshot.prepareRankings())
        }
    }

    /** Reloads when the user actually opens Rankings. */
    fun refresh() {
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            avatarUrlsByPlayerId = emptyMap(),
            errorMessage = null
        )
        loadLeague()
    }

    fun selectCategory(category: PlayerRankingCategory) {
        if (category != _uiState.value.selectedCategory) {
            _uiState.value = _uiState.value.copy(
                selectedCategory = category,
                rankings = rankingsByCategory[category].orEmpty()
            )
        }
    }

    private fun loadLeague() = viewModelScope.launch {
        val leagueRepository = repository
        if (leagueRepository == null) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "Falta la configuración local de Supabase en este dispositivo."
            )
            return@launch
        }

        when (val result = withContext(Dispatchers.Default) { leagueRepository.loadSnapshot() }) {
            is FootballSnapshotResult.Success -> {
                val preparedRankings = withContext(Dispatchers.Default) {
                    result.snapshot.prepareRankings()
                }
                applyPreparedRankings(preparedRankings)
                loadAvatarUrls()
            }
            is FootballSnapshotResult.Failure -> _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = result.message
            )
        }
    }

    private suspend fun loadAvatarUrls() {
        val repository = profileRepository ?: return
        when (val result = repository.loadLeagueAvatarUrls()) {
            is PlayerAvatarUrlsResult.Success -> _uiState.value = _uiState.value.copy(
                avatarUrlsByPlayerId = result.avatarUrlsByPlayerId
            )
            is PlayerAvatarUrlsResult.Failure -> Unit
        }
    }

    private fun applyPreparedRankings(preparedRankings: PreparedRankings) {
        rankingsByCategory = preparedRankings.rankingsByCategory
        _uiState.value = _uiState.value.copy(
            rankings = rankingsByCategory[_uiState.value.selectedCategory].orEmpty(),
            isLoading = false,
            errorMessage = null
        )
    }

    fun selectRankingView(view: RankingView) {
        if (view != _uiState.value.rankingView) {
            _uiState.value = _uiState.value.copy(rankingView = view)
        }
    }

}

private data class PreparedRankings(
    val rankingsByCategory: Map<PlayerRankingCategory, List<PlayerRankingEntry>>
)

/**
 * Builds every ranking once per downloaded snapshot. This runs on Dispatchers.Default in the
 * production refresh path so that selecting a filter only swaps an already prepared list.
 */
private fun FootballSnapshot.prepareRankings(): PreparedRankings {
    val stats = playerStats()
    val rankingMetricsByPlayerId = playerRankingMetricsByPlayerId(stats)
    val recentFormByPlayerId = recentFormByPlayerId()
    val goalkeeperRankings = stats
        .filter { it.goalkeeperMatches > 0 }
        .map { playerStats ->
            playerStats to checkNotNull(rankingMetricsByPlayerId.getValue(playerStats.player.id).goalsAgainst)
        }

    fun rankingEntry(stats: PlayerStats, value: String) = PlayerRankingEntry(
        stats = stats,
        value = value,
        recentForm = recentFormByPlayerId[stats.player.id].orEmpty(),
        goalsAgainst = rankingMetricsByPlayerId.getValue(stats.player.id).goalsAgainst
    )

    return PreparedRankings(
        rankingsByCategory = mapOf(
            PlayerRankingCategory.TOP_SCORER to stats
                .sortedWith(compareByDescending<PlayerStats> { it.goals }.thenByDescending { it.wins })
                .map { rankingEntry(it, it.goals.toString()) },
            PlayerRankingCategory.GOALS_PER_MATCH to stats
                .filter { it.matchesPlayed > 0 }
                .sortedWith(
                    compareByDescending<PlayerStats> { it.goals.toDouble() / it.matchesPlayed }
                        .thenByDescending { it.goals }
                )
                .map { rankingEntry(it, formatPerMatch(it.goals, it.matchesPlayed)) },
            PlayerRankingCategory.ZAMORA to goalkeeperRankings
                .sortedWith(
                    compareBy<Pair<PlayerStats, Double>> { it.second }
                        .thenByDescending { it.first.goalkeeperMatches }
                        .thenByDescending { it.first.wins }
                )
                .map { (stats, goalsAgainst) -> rankingEntry(stats, formatGoals(goalsAgainst)) },
            PlayerRankingCategory.GOALS_CONCEDED_PER_MATCH to goalkeeperRankings
                .sortedWith(
                    compareBy<Pair<PlayerStats, Double>> {
                        it.second / it.first.goalkeeperMatches
                    }.thenByDescending { it.first.goalkeeperMatches }
                        .thenByDescending { it.first.wins }
                )
                .map { (stats, goalsAgainst) ->
                    rankingEntry(stats, formatPerMatch(goalsAgainst, stats.goalkeeperMatches))
                },
            PlayerRankingCategory.MOST_PLAYED to stats
                .sortedWith(compareByDescending<PlayerStats> { it.matchesPlayed }.thenByDescending { it.wins })
                .map { rankingEntry(it, it.matchesPlayed.toString()) },
            PlayerRankingCategory.MOST_WINS to stats
                .sortedWith(compareByDescending<PlayerStats> { it.wins }.thenByDescending { it.goals })
                .map { rankingEntry(it, it.wins.toString()) },
            PlayerRankingCategory.PLAYER_ON_FORM to stats
                .map { playerStats ->
                    playerStats to rankingMetricsByPlayerId.getValue(playerStats.player.id).totalPerformance
                }
                .sortedWith(
                    compareByDescending<Pair<PlayerStats, Int>> { it.second }
                        .thenByDescending { it.first.goals }
                        .thenByDescending { it.first.wins }
                        .thenBy {
                            rankingMetricsByPlayerId.getValue(it.first.player.id).assignedGoalsAgainst
                                ?: Int.MAX_VALUE
                        }
                )
                .map { (stats, total) -> rankingEntry(stats, total.toString()) }
        )
    )
}

private fun FootballSnapshot.recentFormByPlayerId(): Map<String, List<PlayerMatchResult>> {
    val recentMatches = matches.take(5)
    return players.associate { player ->
        player.id to recentMatches.map { match ->
            val participant = match.players.firstOrNull { it.playerId == player.id }
                ?: return@map PlayerMatchResult.DID_NOT_PLAY

            when (match.winner) {
                participant.team -> PlayerMatchResult.WIN
                null -> PlayerMatchResult.DRAW
                else -> PlayerMatchResult.LOSS
            }
        }
    }
}

private fun formatPerMatch(total: Int, matches: Int): String {
    if (matches == 0) return "0.0"
    return (kotlin.math.round(total.toDouble() / matches * 10) / 10).toString()
}

private fun formatPerMatch(total: Double, matches: Int): String {
    if (matches == 0) return "0.0"
    return formatOneDecimal(total / matches)
}

private fun formatGoals(total: Double): String {
    val roundedTotal = kotlin.math.round(total * 10) / 10
    return if (roundedTotal % 1.0 == 0.0) {
        roundedTotal.toInt().toString()
    } else {
        roundedTotal.toString()
    }
}

private fun formatOneDecimal(value: Double): String =
    (kotlin.math.round(value * 10) / 10).toString()
