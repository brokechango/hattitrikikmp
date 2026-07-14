package com.brokechango.hattitriki.feature.players

import androidx.lifecycle.ViewModel
import com.brokechango.hattitriki.core.data.FriendlyFootballRepository
import com.brokechango.hattitriki.core.data.InMemoryFriendlyFootballRepository
import com.brokechango.hattitriki.core.model.PlayerRankingCategory
import com.brokechango.hattitriki.core.model.PlayerStats
import com.brokechango.hattitriki.core.model.TeamSide
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PlayersViewModel(
    repository: FriendlyFootballRepository = InMemoryFriendlyFootballRepository
) : ViewModel() {
    private val stats = repository.getStats()
    private val matches = repository.getMatches()
    private val goalsAgainstByPlayerId = goalkeeperRankings()
        .associate { (playerStats, goalsAgainst) -> playerStats.player.id to goalsAgainst }

    private val _uiState = MutableStateFlow(buildUiState(PlayerRankingCategory.TOP_SCORER))
    val uiState: StateFlow<PlayersUiState> = _uiState.asStateFlow()

    fun selectCategory(category: PlayerRankingCategory) {
        if (category != _uiState.value.selectedCategory) {
            _uiState.value = buildUiState(category, _uiState.value.rankingView)
        }
    }

    fun selectRankingView(view: RankingView) {
        if (view != _uiState.value.rankingView) {
            _uiState.value = _uiState.value.copy(rankingView = view)
        }
    }

    private fun buildUiState(
        category: PlayerRankingCategory,
        rankingView: RankingView = RankingView.DETAILED
    ): PlayersUiState = PlayersUiState(
        selectedCategory = category,
        rankingView = rankingView,
        rankings = when (category) {
            PlayerRankingCategory.TOP_SCORER -> stats
                .sortedWith(compareByDescending<PlayerStats> { it.goals }.thenByDescending { it.wins })
                .map { rankingEntry(it, "${it.goals} G") }

            PlayerRankingCategory.GOALS_PER_MATCH -> stats
                .filter { it.matchesPlayed > 0 }
                .sortedWith(
                    compareByDescending<PlayerStats> { it.goals.toDouble() / it.matchesPlayed }
                        .thenByDescending { it.goals }
                )
                .map { rankingEntry(it, "${formatPerMatch(it.goals, it.matchesPlayed)} G/P") }

            PlayerRankingCategory.ZAMORA -> goalkeeperRankings()
                .sortedWith(
                    compareBy<Pair<PlayerStats, Int>> { it.second }
                        .thenByDescending { it.first.goalkeeperMatches }
                        .thenByDescending { it.first.wins }
                )
                .map { (stats, goalsAgainst) -> rankingEntry(stats, "$goalsAgainst GC") }

            PlayerRankingCategory.GOALS_CONCEDED_PER_MATCH -> goalkeeperRankings()
                .sortedWith(
                    compareBy<Pair<PlayerStats, Int>> {
                        it.second.toDouble() / it.first.goalkeeperMatches
                    }.thenByDescending { it.first.goalkeeperMatches }
                        .thenByDescending { it.first.wins }
                )
                .map { (stats, goalsAgainst) ->
                    rankingEntry(stats, "${formatPerMatch(goalsAgainst, stats.goalkeeperMatches)} GC/P")
                }

            PlayerRankingCategory.MOST_PLAYED -> stats
                .sortedWith(compareByDescending<PlayerStats> { it.matchesPlayed }.thenByDescending { it.wins })
                .map { rankingEntry(it, "${it.matchesPlayed} PJ") }

            PlayerRankingCategory.MOST_WINS -> stats
                .sortedWith(compareByDescending<PlayerStats> { it.wins }.thenByDescending { it.goals })
                .map { rankingEntry(it, "${it.wins} V") }

            PlayerRankingCategory.PLAYER_ON_FORM -> stats
                .map { it to playerOnFormTotal(it) }
                .sortedWith(
                    compareByDescending<Pair<PlayerStats, Int>> { it.second }
                        .thenByDescending { it.first.goals }
                        .thenByDescending { it.first.wins }
                        .thenBy { goalsAgainstByPlayerId[it.first.player.id] ?: Int.MAX_VALUE }
                )
                .map { (stats, total) -> rankingEntry(stats, total.toString()) }
        }
    )

    private fun rankingEntry(stats: PlayerStats, value: String): PlayerRankingEntry = PlayerRankingEntry(
        stats = stats,
        value = value,
        recentForm = recentForm(stats.player.id),
        goalsAgainst = goalsAgainstByPlayerId[stats.player.id]
    )

    private fun playerOnFormTotal(stats: PlayerStats): Int {
        val goalkeeperAdjustment = goalsAgainstByPlayerId[stats.player.id]?.let { goalsAgainst ->
            (stats.goalkeeperMatches * 2 - goalsAgainst).coerceAtLeast(0)
        } ?: 0

        return stats.matchesPlayed + stats.goals + stats.wins + goalkeeperAdjustment
    }

    private fun recentForm(playerId: String): List<PlayerMatchResult> = matches
        .asSequence()
        .mapNotNull { match ->
            val team = match.players.firstOrNull { it.playerId == playerId }?.team ?: return@mapNotNull null
            when (match.winner) {
                team -> PlayerMatchResult.WIN
                null -> PlayerMatchResult.DRAW
                else -> PlayerMatchResult.LOSS
            }
        }
        .take(5)
        .toList()

    private fun goalkeeperRankings(): List<Pair<PlayerStats, Int>> = stats
        .filter { it.goalkeeperMatches > 0 }
        .map { playerStats ->
            val goalsAgainst = matches.sumOf { match ->
                val goalkeeper = match.players.firstOrNull {
                    it.playerId == playerStats.player.id && it.wasGoalkeeper
                }
                when (goalkeeper?.team) {
                    TeamSide.A -> match.teamBScore
                    TeamSide.B -> match.teamAScore
                    null -> 0
                }
            }
            playerStats to goalsAgainst
        }

    private fun formatPerMatch(total: Int, matches: Int): String {
        if (matches == 0) return "0.0"
        return (kotlin.math.round(total.toDouble() / matches * 10) / 10).toString()
    }
}
