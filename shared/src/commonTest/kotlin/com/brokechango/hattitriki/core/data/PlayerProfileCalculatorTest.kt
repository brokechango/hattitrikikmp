package com.brokechango.hattitriki.core.data

import com.brokechango.hattitriki.core.model.FriendlyMatch
import com.brokechango.hattitriki.core.model.GoalEntry
import com.brokechango.hattitriki.core.model.MatchPlayer
import com.brokechango.hattitriki.core.model.Player
import com.brokechango.hattitriki.core.model.TeamSide
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class PlayerProfileCalculatorTest {
    @Test
    fun `profile identifies the most frequent rival and teammate by distinct match`() {
        val snapshot = FootballSnapshot(
            players = listOf(
                Player("a", "Álex"),
                Player("b", "Bruno"),
                Player("c", "Carla"),
                Player("d", "Diego")
            ),
            matches = listOf(
                match("1", "a" to TeamSide.A, "c" to TeamSide.A, "b" to TeamSide.B),
                match("2", "a" to TeamSide.A, "c" to TeamSide.A, "b" to TeamSide.B),
                match("3", "a" to TeamSide.A, "d" to TeamSide.B),
                match(
                    "4",
                    "a" to TeamSide.A,
                    "a" to TeamSide.B,
                    "c" to TeamSide.A,
                    "b" to TeamSide.B
                )
            )
        )

        val summary = assertNotNull(snapshot.playerProfileSummary("a"))

        assertEquals("Bruno", summary.maximumRival?.player?.name)
        assertEquals(3, summary.maximumRival?.matches)
        assertEquals("Carla", summary.inseparableTeammate?.player?.name)
        assertEquals(3, summary.inseparableTeammate?.matches)
    }

    @Test
    fun `a player appearing for both teams does not duplicate one relationship in a match`() {
        val snapshot = FootballSnapshot(
            players = listOf(Player("a", "Álex"), Player("b", "Bruno")),
            matches = listOf(
                match("1", "a" to TeamSide.A, "a" to TeamSide.B, "b" to TeamSide.B)
            )
        )

        val summary = assertNotNull(snapshot.playerProfileSummary("a"))

        assertEquals(1, summary.maximumRival?.matches)
        assertEquals(1, summary.inseparableTeammate?.matches)
    }

    @Test
    fun `profile exposes the same metrics used by every ranking filter`() {
        val alex = Player("a", "Álex")
        val bruno = Player("b", "Bruno")
        val snapshot = FootballSnapshot(
            players = listOf(alex, bruno),
            matches = listOf(
                FriendlyMatch(
                    id = "1",
                    dateLabel = "1",
                    teamAScore = 2,
                    teamBScore = 1,
                    players = listOf(
                        MatchPlayer(alex.id, TeamSide.A, wasGoalkeeper = true),
                        MatchPlayer(bruno.id, TeamSide.B, wasGoalkeeper = true)
                    ),
                    goals = listOf(
                        GoalEntry(alex.id, TeamSide.A, count = 2, goalkeeperId = bruno.id),
                        GoalEntry(bruno.id, TeamSide.B, count = 1, goalkeeperId = alex.id)
                    )
                )
            )
        )

        val metrics = assertNotNull(snapshot.playerProfileSummary(alex.id)).rankingMetrics

        assertEquals(2.0, metrics.goalsPerMatch)
        assertEquals(1.0, metrics.goalsAgainst)
        assertEquals(1.0, metrics.goalsAgainstPerMatch)
        assertEquals(5, metrics.totalPerformance)
    }

    private fun match(id: String, vararg participants: Pair<String, TeamSide>) = FriendlyMatch(
        id = id,
        dateLabel = id,
        teamAScore = 0,
        teamBScore = 0,
        players = participants.map { (playerId, team) -> MatchPlayer(playerId, team) },
        goals = emptyList()
    )
}
