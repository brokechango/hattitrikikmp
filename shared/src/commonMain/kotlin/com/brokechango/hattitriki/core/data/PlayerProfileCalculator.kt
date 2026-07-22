package com.brokechango.hattitriki.core.data

import com.brokechango.hattitriki.core.model.Player
import com.brokechango.hattitriki.core.model.PlayerConnection
import com.brokechango.hattitriki.core.model.PlayerProfileSummary

/**
 * Builds a player profile from the same league snapshot used by the rankings.
 * A pair can contribute only once per match to each relationship, even if one
 * of the players appeared for both teams.
 */
fun FootballSnapshot.playerProfileSummary(playerId: String): PlayerProfileSummary? {
    val leagueStats = playerStats()
    val stats = leagueStats.firstOrNull { it.player.id == playerId } ?: return null
    val rankingMetrics = playerRankingMetricsByPlayerId(leagueStats).getValue(playerId)
    val teammates = mutableMapOf<String, Int>()
    val rivals = mutableMapOf<String, Int>()

    matches.forEach { match ->
        val playerTeams = match.players
            .asSequence()
            .filter { it.playerId == playerId }
            .map { it.team }
            .toSet()
        if (playerTeams.isEmpty()) return@forEach

        val teamsByOtherPlayer = match.players
            .asSequence()
            .filter { it.playerId != playerId }
            .groupBy({ it.playerId }, { it.team })
            .mapValues { (_, teams) -> teams.toSet() }

        teamsByOtherPlayer.forEach { (otherPlayerId, otherTeams) ->
            if (playerTeams.intersect(otherTeams).isNotEmpty()) {
                teammates[otherPlayerId] = (teammates[otherPlayerId] ?: 0) + 1
            }
            if (playerTeams.any { ownTeam -> otherTeams.any { it != ownTeam } }) {
                rivals[otherPlayerId] = (rivals[otherPlayerId] ?: 0) + 1
            }
        }
    }

    val playersById = players.associateBy(Player::id)
    return PlayerProfileSummary(
        stats = stats,
        rankingMetrics = rankingMetrics,
        maximumRival = bestConnection(rivals, playersById),
        inseparableTeammate = bestConnection(teammates, playersById)
    )
}

private fun bestConnection(
    counts: Map<String, Int>,
    playersById: Map<String, Player>
): PlayerConnection? {
    val maximum = counts.values.maxOrNull() ?: return null
    val leaders = counts
        .asSequence()
        .filter { it.value == maximum }
        .mapNotNull { (playerId, count) -> playersById[playerId]?.let { it to count } }
        .sortedWith(compareBy<Pair<Player, Int>> { it.first.name.lowercase() }.thenBy { it.first.id })
        .toList()
    val leader = leaders.firstOrNull() ?: return null
    return PlayerConnection(
        player = leader.first,
        matches = leader.second,
        isTied = leaders.size > 1
    )
}
