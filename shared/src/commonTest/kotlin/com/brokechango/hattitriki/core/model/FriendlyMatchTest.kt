package com.brokechango.hattitriki.core.model

import kotlin.test.Test
import kotlin.test.assertEquals

class FriendlyMatchTest {
    @Test
    fun `a penalty shootout decides a match that ended in a draw`() {
        val match = FriendlyMatch(
            id = "match-1",
            dateLabel = "20 julio",
            teamAScore = 2,
            teamBScore = 2,
            players = emptyList(),
            goals = emptyList(),
            penaltyShootout = PenaltyShootout(teamAScore = 4, teamBScore = 5)
        )

        assertEquals(TeamSide.B, match.winner)
        assertEquals("4 - 5 en penaltis", match.penaltyShootoutLabel)
    }
}
