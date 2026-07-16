package com.brokechango.hattitriki.feature.players

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brokechango.hattitriki.core.data.FootballSnapshot
import com.brokechango.hattitriki.core.data.FootballSnapshotResult
import com.brokechango.hattitriki.core.data.FriendlyFootballRepository
import com.brokechango.hattitriki.core.data.playerStats
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

    /** Reloads when the user actually opens Clasificaciones. */
    fun refresh() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
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
            }
            is FootballSnapshotResult.Failure -> _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = result.message
            )
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
    val goalsAgainstByPlayerId = matches
        .asSequence()
        .flatMap { it.goals.asSequence() }
        .groupingBy { it.goalkeeperId }
        .fold(0) { total, goal -> total + goal.count }
    val recentFormByPlayerId = recentFormByPlayerId()
    val goalkeeperRankings = stats
        .filter { it.goalkeeperMatches > 0 }
        .map { it to (goalsAgainstByPlayerId[it.player.id] ?: 0) }

    fun rankingEntry(stats: PlayerStats, value: String) = PlayerRankingEntry(
        stats = stats,
        value = value,
        recentForm = recentFormByPlayerId[stats.player.id].orEmpty(),
        goalsAgainst = goalsAgainstByPlayerId[stats.player.id]
    )

    fun playerOnFormTotal(stats: PlayerStats): Int {
        val goalkeeperAdjustment = goalsAgainstByPlayerId[stats.player.id]?.let { goalsAgainst ->
            (stats.goalkeeperMatches * 2 - goalsAgainst).coerceAtLeast(0)
        } ?: 0

        return stats.matchesPlayed + stats.goals + stats.wins + goalkeeperAdjustment
    }

    return PreparedRankings(
        rankingsByCategory = mapOf(
            PlayerRankingCategory.TOP_SCORER to stats
                .sortedWith(compareByDescending<PlayerStats> { it.goals }.thenByDescending { it.wins })
                .map { rankingEntry(it, "${it.goals} G") },
            PlayerRankingCategory.GOALS_PER_MATCH to stats
                .filter { it.matchesPlayed > 0 }
                .sortedWith(
                    compareByDescending<PlayerStats> { it.goals.toDouble() / it.matchesPlayed }
                        .thenByDescending { it.goals }
                )
                .map { rankingEntry(it, "${formatPerMatch(it.goals, it.matchesPlayed)} G/P") },
            PlayerRankingCategory.ZAMORA to goalkeeperRankings
                .sortedWith(
                    compareBy<Pair<PlayerStats, Int>> { it.second }
                        .thenByDescending { it.first.goalkeeperMatches }
                        .thenByDescending { it.first.wins }
                )
                .map { (stats, goalsAgainst) -> rankingEntry(stats, "$goalsAgainst GC") },
            PlayerRankingCategory.GOALS_CONCEDED_PER_MATCH to goalkeeperRankings
                .sortedWith(
                    compareBy<Pair<PlayerStats, Int>> {
                        it.second.toDouble() / it.first.goalkeeperMatches
                    }.thenByDescending { it.first.goalkeeperMatches }
                        .thenByDescending { it.first.wins }
                )
                .map { (stats, goalsAgainst) ->
                    rankingEntry(stats, "${formatPerMatch(goalsAgainst, stats.goalkeeperMatches)} GC/P")
                },
            PlayerRankingCategory.MOST_PLAYED to stats
                .sortedWith(compareByDescending<PlayerStats> { it.matchesPlayed }.thenByDescending { it.wins })
                .map { rankingEntry(it, "${it.matchesPlayed} PJ") },
            PlayerRankingCategory.MOST_WINS to stats
                .sortedWith(compareByDescending<PlayerStats> { it.wins }.thenByDescending { it.goals })
                .map { rankingEntry(it, "${it.wins} V") },
            PlayerRankingCategory.PLAYER_ON_FORM to stats
                .map { it to playerOnFormTotal(it) }
                .sortedWith(
                    compareByDescending<Pair<PlayerStats, Int>> { it.second }
                        .thenByDescending { it.first.goals }
                        .thenByDescending { it.first.wins }
                        .thenBy { goalsAgainstByPlayerId[it.first.player.id] ?: Int.MAX_VALUE }
                )
                .map { (stats, total) -> rankingEntry(stats, total.toString()) }
        )
    )
}

private fun FootballSnapshot.recentFormByPlayerId(): Map<String, List<PlayerMatchResult>> {
    val forms = mutableMapOf<String, MutableList<PlayerMatchResult>>()
    matches.forEach { match ->
        val playersAlreadyProcessed = mutableSetOf<String>()
        match.players.forEach { participant ->
            if (!playersAlreadyProcessed.add(participant.playerId)) return@forEach
            val playerForm = forms.getOrPut(participant.playerId) { mutableListOf() }
            if (playerForm.size < 5) {
                playerForm += when (match.winner) {
                    participant.team -> PlayerMatchResult.WIN
                    null -> PlayerMatchResult.DRAW
                    else -> PlayerMatchResult.LOSS
                }
            }
        }
    }
    return forms
}

private fun formatPerMatch(total: Int, matches: Int): String {
    if (matches == 0) return "0.0"
    return (kotlin.math.round(total.toDouble() / matches * 10) / 10).toString()
}
