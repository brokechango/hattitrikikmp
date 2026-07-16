package com.brokechango.hattitriki.feature.teamrandomizer

import kotlin.random.Random

data class RandomTeam(
    val name: String,
    val players: List<TeamParticipant>
) {
    val statsScore: Double
        get() = players.sumOf(TeamParticipant::statsScore)

    val cardioPlayers: Int
        get() = players.count(TeamParticipant::hasCardio)
}

data class TeamParticipant(
    val id: String,
    val name: String,
    val hasCardio: Boolean = false,
    val statsScore: Double = 0.0
)

object TeamRandomizer {
    internal fun statsScore(
        goals: Int,
        wins: Int,
        matchesPlayed: Int,
        goalkeeperMatches: Int,
        goalsConcededPerMatch: Double
    ): Double = goals * 3.0 + wins * 2.0 + matchesPlayed + goalkeeperMatches - goalsConcededPerMatch

    fun participantsFrom(input: String): List<String> = input
        .lineSequence()
        .map(String::trim)
        .filter(String::isNotEmpty)
        .toList()

    fun createTeams(
        participants: List<TeamParticipant>,
        teamCount: Int,
        balanceStats: Boolean,
        random: Random = Random.Default
    ): List<RandomTeam> {
        require(participants.size >= 2) { "Añade al menos dos participantes." }
        require(teamCount in 2..participants.size) {
            "El número de equipos debe estar entre 2 y el número de participantes."
        }

        val extraPlayerTeams = participants.size % teamCount
        val buckets = List(teamCount) { index ->
            TeamBucket(capacity = participants.size / teamCount + if (index < extraPlayerTeams) 1 else 0)
        }

        val orderedParticipants = participants
            .shuffled(random)
            .sortedWith(
                compareByDescending<TeamParticipant> { it.hasCardio }
                    .thenByDescending { if (balanceStats) it.statsScore else 0 }
            )
        orderedParticipants.forEach { participant ->
            val availableTeams = buckets.filter { it.players.size < it.capacity }
            val cardioBalancedTeams = if (participant.hasCardio) {
                val minimumCardioPlayers = availableTeams.minOf(TeamBucket::cardioPlayers)
                availableTeams.filter { it.cardioPlayers == minimumCardioPlayers }
            } else {
                availableTeams
            }
            val statsBalancedTeams = if (balanceStats) {
                val minimumScore = cardioBalancedTeams.minOf(TeamBucket::statsScore)
                cardioBalancedTeams.filter { it.statsScore == minimumScore }
            } else {
                cardioBalancedTeams
            }
            val leastFilledTeams = statsBalancedTeams.let { candidates ->
                val minimumSize = candidates.minOf { it.players.size }
                candidates.filter { it.players.size == minimumSize }
            }
            leastFilledTeams.random(random).add(participant)
        }

        return buckets.mapIndexed { index, players ->
            RandomTeam(name = "Equipo ${index + 1}", players = players.players)
        }
    }

    private class TeamBucket(val capacity: Int) {
        val players = mutableListOf<TeamParticipant>()
        var statsScore = 0.0
            private set
        var cardioPlayers = 0
            private set

        fun add(participant: TeamParticipant) {
            players += participant
            statsScore += participant.statsScore
            if (participant.hasCardio) cardioPlayers++
        }
    }
}
