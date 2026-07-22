package com.brokechango.hattitriki.core.data

import com.brokechango.hattitriki.core.model.FriendlyMatch
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

    private fun match(id: String, vararg participants: Pair<String, TeamSide>) = FriendlyMatch(
        id = id,
        dateLabel = id,
        teamAScore = 0,
        teamBScore = 0,
        players = participants.map { (playerId, team) -> MatchPlayer(playerId, team) },
        goals = emptyList()
    )
}
