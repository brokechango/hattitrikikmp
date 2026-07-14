package com.brokechango.hattitriki.core.data

import com.brokechango.hattitriki.core.model.FriendlyMatch
import com.brokechango.hattitriki.core.model.GoalEntry
import com.brokechango.hattitriki.core.model.MatchPlayer
import com.brokechango.hattitriki.core.model.Player
import com.brokechango.hattitriki.core.model.PlayerStats
import com.brokechango.hattitriki.core.model.TeamSide

interface FriendlyFootballRepository {
    fun getPlayers(): List<Player>
    fun getMatches(): List<FriendlyMatch>
    fun getMatch(matchId: String): FriendlyMatch?
    fun getStats(): List<PlayerStats>
}

object InMemoryFriendlyFootballRepository : FriendlyFootballRepository {
    private val players = listOf(
        Player(id = "alex", name = "Alex"),
        Player(id = "bruno", name = "Bruno"),
        Player(id = "carlos", name = "Carlos"),
        Player(id = "dani", name = "Dani"),
        Player(id = "ivan", name = "Ivan"),
        Player(id = "miguel", name = "Miguel"),
        Player(id = "pablo", name = "Pablo"),
        Player(id = "sergio", name = "Sergio")
    )

    private val matches = listOf(
        FriendlyMatch(
            id = "match-5",
            dateLabel = "Domingo 26 julio",
            teamAScore = 5,
            teamBScore = 3,
            players = listOf(
                MatchPlayer("alex", TeamSide.A, wasGoalkeeper = true),
                MatchPlayer("bruno", TeamSide.A),
                MatchPlayer("carlos", TeamSide.A),
                MatchPlayer("dani", TeamSide.A),
                MatchPlayer("ivan", TeamSide.B, wasGoalkeeper = true),
                MatchPlayer("miguel", TeamSide.B),
                MatchPlayer("pablo", TeamSide.B),
                MatchPlayer("sergio", TeamSide.B)
            ),
            goals = listOf(
                GoalEntry("bruno", TeamSide.A, 2, goalkeeperId = "ivan"),
                GoalEntry("carlos", TeamSide.A, 1, goalkeeperId = "ivan"),
                GoalEntry("dani", TeamSide.A, 2, goalkeeperId = "ivan"),
                GoalEntry("miguel", TeamSide.B, 1, goalkeeperId = "alex"),
                GoalEntry("pablo", TeamSide.B, 1, goalkeeperId = "alex"),
                GoalEntry("sergio", TeamSide.B, 1, goalkeeperId = "alex")
            )
        ),
        FriendlyMatch(
            id = "match-4",
            dateLabel = "Domingo 19 julio",
            teamAScore = 2,
            teamBScore = 5,
            players = listOf(
                MatchPlayer("alex", TeamSide.A),
                MatchPlayer("ivan", TeamSide.A, wasGoalkeeper = true),
                MatchPlayer("pablo", TeamSide.A),
                MatchPlayer("sergio", TeamSide.A),
                MatchPlayer("bruno", TeamSide.B),
                MatchPlayer("carlos", TeamSide.B, wasGoalkeeper = true),
                MatchPlayer("dani", TeamSide.B),
                MatchPlayer("miguel", TeamSide.B)
            ),
            goals = listOf(
                GoalEntry("alex", TeamSide.A, 1, goalkeeperId = "carlos"),
                GoalEntry("sergio", TeamSide.A, 1, goalkeeperId = "carlos"),
                GoalEntry("bruno", TeamSide.B, 2, goalkeeperId = "ivan"),
                GoalEntry("dani", TeamSide.B, 1, goalkeeperId = "ivan"),
                GoalEntry("miguel", TeamSide.B, 2, goalkeeperId = "ivan")
            )
        ),
        FriendlyMatch(
            id = "match-3",
            dateLabel = "Domingo 12 julio",
            teamAScore = 7,
            teamBScore = 5,
            players = listOf(
                MatchPlayer("alex", TeamSide.A, wasGoalkeeper = true),
                MatchPlayer("bruno", TeamSide.A),
                MatchPlayer("carlos", TeamSide.A),
                MatchPlayer("dani", TeamSide.A),
                MatchPlayer("ivan", TeamSide.B, wasGoalkeeper = true),
                MatchPlayer("miguel", TeamSide.B),
                MatchPlayer("pablo", TeamSide.B),
                MatchPlayer("sergio", TeamSide.B)
            ),
            goals = listOf(
                GoalEntry("bruno", TeamSide.A, 2, goalkeeperId = "ivan"),
                GoalEntry("bruno", TeamSide.A, 1, goalkeeperId = "sergio"),
                GoalEntry("carlos", TeamSide.A, 2, goalkeeperId = "ivan"),
                GoalEntry("dani", TeamSide.A, 2, goalkeeperId = "ivan"),
                GoalEntry("miguel", TeamSide.B, 2, goalkeeperId = "alex"),
                GoalEntry("pablo", TeamSide.B, 2, goalkeeperId = "alex"),
                GoalEntry("sergio", TeamSide.B, 1, goalkeeperId = "alex")
            )
        ),
        FriendlyMatch(
            id = "match-2",
            dateLabel = "Domingo 5 julio",
            teamAScore = 4,
            teamBScore = 4,
            players = listOf(
                MatchPlayer("alex", TeamSide.A),
                MatchPlayer("ivan", TeamSide.A, wasGoalkeeper = true),
                MatchPlayer("pablo", TeamSide.A),
                MatchPlayer("sergio", TeamSide.A),
                MatchPlayer("bruno", TeamSide.B),
                MatchPlayer("carlos", TeamSide.B, wasGoalkeeper = true),
                MatchPlayer("dani", TeamSide.B),
                MatchPlayer("miguel", TeamSide.B)
            ),
            goals = listOf(
                GoalEntry("alex", TeamSide.A, 1, goalkeeperId = "carlos"),
                GoalEntry("pablo", TeamSide.A, 2, goalkeeperId = "carlos"),
                GoalEntry("sergio", TeamSide.A, 1, goalkeeperId = "carlos"),
                GoalEntry("bruno", TeamSide.B, 1, goalkeeperId = "ivan"),
                GoalEntry("dani", TeamSide.B, 2, goalkeeperId = "ivan"),
                GoalEntry("miguel", TeamSide.B, 1, goalkeeperId = "ivan")
            )
        ),
        FriendlyMatch(
            id = "match-1",
            dateLabel = "Domingo 28 junio",
            teamAScore = 3,
            teamBScore = 6,
            players = listOf(
                MatchPlayer("alex", TeamSide.A),
                MatchPlayer("carlos", TeamSide.A, wasGoalkeeper = true),
                MatchPlayer("dani", TeamSide.A),
                MatchPlayer("miguel", TeamSide.A),
                MatchPlayer("bruno", TeamSide.B),
                MatchPlayer("ivan", TeamSide.B),
                MatchPlayer("pablo", TeamSide.B, wasGoalkeeper = true),
                MatchPlayer("sergio", TeamSide.B)
            ),
            goals = listOf(
                GoalEntry("alex", TeamSide.A, 1, goalkeeperId = "pablo"),
                GoalEntry("dani", TeamSide.A, 1, goalkeeperId = "pablo"),
                GoalEntry("miguel", TeamSide.A, 1, goalkeeperId = "pablo"),
                GoalEntry("bruno", TeamSide.B, 2, goalkeeperId = "carlos"),
                GoalEntry("ivan", TeamSide.B, 2, goalkeeperId = "carlos"),
                GoalEntry("sergio", TeamSide.B, 2, goalkeeperId = "carlos")
            )
        )
    )

    override fun getPlayers(): List<Player> = players

    override fun getMatches(): List<FriendlyMatch> = matches

    override fun getMatch(matchId: String): FriendlyMatch? = matches.firstOrNull { it.id == matchId }

    override fun getStats(): List<PlayerStats> {
        return players.map { player ->
            val playedMatches = matches.filter { match ->
                match.players.any { it.playerId == player.id }
            }
            val wins = playedMatches.count { match ->
                val side = match.players.first { it.playerId == player.id }.team
                match.winner == side
            }
            val draws = playedMatches.count { it.winner == null }
            val goals = matches.sumOf { match ->
                match.goals.filter { it.playerId == player.id }.sumOf { it.count }
            }
            val goalkeeperMatches = playedMatches.count { match ->
                match.players.any { it.playerId == player.id && it.wasGoalkeeper }
            }

            PlayerStats(
                player = player,
                matchesPlayed = playedMatches.size,
                wins = wins,
                draws = draws,
                losses = playedMatches.size - wins - draws,
                goals = goals,
                goalkeeperMatches = goalkeeperMatches
            )
        }.sortedWith(compareByDescending<PlayerStats> { it.goals }.thenByDescending { it.wins })
    }
}
