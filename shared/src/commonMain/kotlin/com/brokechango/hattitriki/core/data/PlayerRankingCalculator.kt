package com.brokechango.hattitriki.core.data

import com.brokechango.hattitriki.core.model.PlayerRankingMetrics
import com.brokechango.hattitriki.core.model.PlayerStats

/**
 * Calculates the raw values used by the Rankings filters for every player in a league snapshot.
 * Keeping this in the data layer lets profiles and rankings present the same values.
 */
fun FootballSnapshot.playerRankingMetricsByPlayerId(
    stats: List<PlayerStats> = playerStats()
): Map<String, PlayerRankingMetrics> {
    val assignedGoalsAgainstByPlayerId = matches
        .asSequence()
        .flatMap { it.goals.asSequence() }
        .groupingBy { it.goalkeeperId }
        .fold(0) { total, goal -> total + goal.count }
    val sharedGoalsAgainstByPlayerId = goalsAgainstShareByGoalkeeperId()

    return stats.associate { playerStats ->
        val goalsAgainst = if (playerStats.goalkeeperMatches > 0) {
            sharedGoalsAgainstByPlayerId[playerStats.player.id] ?: 0.0
        } else {
            null
        }
        val goalkeeperAdjustment = assignedGoalsAgainstByPlayerId[playerStats.player.id]?.let {
            goalsAgainstAssigned ->
            (playerStats.goalkeeperMatches * 2 - goalsAgainstAssigned).coerceAtLeast(0)
        } ?: 0

        playerStats.player.id to PlayerRankingMetrics(
            goalsPerMatch = playerStats.goals.toDouble() / playerStats.matchesPlayed.coerceAtLeast(1),
            goalsAgainst = goalsAgainst,
            goalsAgainstPerMatch = goalsAgainst?.div(playerStats.goalkeeperMatches),
            assignedGoalsAgainst = assignedGoalsAgainstByPlayerId[playerStats.player.id],
            totalPerformance = playerStats.matchesPlayed + playerStats.goals + playerStats.wins +
                goalkeeperAdjustment
        )
    }
}
