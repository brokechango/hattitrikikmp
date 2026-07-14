package com.brokechango.hattitriki.feature.home

import androidx.lifecycle.ViewModel
import com.brokechango.hattitriki.core.data.FriendlyFootballRepository
import com.brokechango.hattitriki.core.data.InMemoryFriendlyFootballRepository
import com.brokechango.hattitriki.core.model.FriendlyMatch
import com.brokechango.hattitriki.core.model.PlayerStats
import com.brokechango.hattitriki.core.model.PlayerRankingCategory
import com.brokechango.hattitriki.core.model.TeamSide
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.round

class HomeViewModel(
    private val repository: FriendlyFootballRepository = InMemoryFriendlyFootballRepository
) : ViewModel() {
    private val matches = repository.getMatches()
    private val stats = repository.getStats()

    private val _uiState = MutableStateFlow(
        HomeUiState(
            latestMatch = matches.firstOrNull(),
            totalMatches = matches.size,
            totalGoals = matches.sumOf { it.teamAScore + it.teamBScore },
            featuredStats = buildFeaturedStats(matches, stats)
        )
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private fun buildFeaturedStats(
        matches: List<FriendlyMatch>,
        stats: List<PlayerStats>
    ): List<HomeFeaturedStat> {
        val topScorer = stats.maxWithOrNull(compareBy<PlayerStats> { it.goals }.thenBy { it.wins })
        val topGoalsPerMatch = stats
            .filter { it.matchesPlayed > 0 }
            .maxByOrNull { it.goals.toDouble() / it.matchesPlayed }
        val mostPlayed = stats.maxWithOrNull(compareBy<PlayerStats> { it.matchesPlayed }.thenBy { it.wins })
        val mostWins = stats.maxWithOrNull(compareBy<PlayerStats> { it.wins }.thenBy { it.goals })
        val goalkeeperStats = stats
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
        val bestGoalkeeper = goalkeeperStats
            .minWithOrNull(
                compareBy<Pair<PlayerStats, Int>> { it.second }
                    .thenByDescending { it.first.goalkeeperMatches }
                    .thenByDescending { it.first.wins }
            )
        val bestGoalkeeperPerMatch = goalkeeperStats
            .minByOrNull { (playerStats, goalsAgainst) ->
                goalsAgainst.toDouble() / playerStats.goalkeeperMatches
            }

        return listOfNotNull(
            topScorer?.let {
                HomeFeaturedStat(
                    category = PlayerRankingCategory.TOP_SCORER,
                    playerName = it.player.name,
                    value = it.goals.toString()
                )
            },
            topGoalsPerMatch?.let {
                HomeFeaturedStat(
                    category = PlayerRankingCategory.GOALS_PER_MATCH,
                    playerName = it.player.name,
                    value = formatPerMatch(it.goals, it.matchesPlayed)
                )
            },
            bestGoalkeeper?.let { (playerStats, goalsAgainst) ->
                HomeFeaturedStat(
                    category = PlayerRankingCategory.ZAMORA,
                    playerName = playerStats.player.name,
                    value = goalsAgainst.toString()
                )
            },
            bestGoalkeeperPerMatch?.let { (playerStats, goalsAgainst) ->
                HomeFeaturedStat(
                    category = PlayerRankingCategory.GOALS_CONCEDED_PER_MATCH,
                    playerName = playerStats.player.name,
                    value = formatPerMatch(goalsAgainst, playerStats.goalkeeperMatches)
                )
            },
            mostPlayed?.let {
                HomeFeaturedStat(
                    category = PlayerRankingCategory.MOST_PLAYED,
                    playerName = it.player.name,
                    value = it.matchesPlayed.toString()
                )
            },
            mostWins?.let {
                HomeFeaturedStat(
                    category = PlayerRankingCategory.MOST_WINS,
                    playerName = it.player.name,
                    value = it.wins.toString()
                )
            }
        )
    }

    private fun formatPerMatch(total: Int, matches: Int): String {
        if (matches == 0) return "0.0"
        return (round(total.toDouble() / matches * 10) / 10).toString()
    }
}
