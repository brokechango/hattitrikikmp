package com.brokechango.hattitriki.feature.newmatch

import com.brokechango.hattitriki.core.data.AdminPlayer
import com.brokechango.hattitriki.core.data.ActaTeam
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NewMatchUiStateTest {
    @Test
    fun `players are outside by default`() {
        val state = NewMatchUiState(players = listOf(AdminPlayer("alex", "Alex", true)))

        assertTrue(state.selectedPlayerIds.isEmpty())
        assertFalse(state.isOnTeam("alex", ActaTeam.A))
        assertFalse(state.isOnTeam("alex", ActaTeam.B))
    }

    @Test
    fun `recognises an existing match as an edit`() {
        assertTrue(NewMatchUiState(editingMatchId = "match-1").isEditing)
        assertFalse(NewMatchUiState().isEditing)
    }

    @Test
    fun `allows a complete match report`() {
        val state = NewMatchUiState(
            isCheckingAccess = false,
            isAdmin = true,
            players = listOf(AdminPlayer("a", "Alex", true), AdminPlayer("b", "Bruno", true)),
            date = "2026-07-20",
            teamAScore = "1",
            teamBScore = "0",
            teamAPlayerIds = listOf("a"),
            teamBPlayerIds = listOf("b"),
            goalkeeperAIds = listOf("a"),
            goalkeeperBIds = listOf("b"),
            goalEntries = listOf(GoalDraft("a", ActaTeam.A, 1, "b"))
        )

        assertTrue(state.canSubmit)
    }

    @Test
    fun `rejects an acta when its goals do not match the score`() {
        val state = NewMatchUiState(
            isCheckingAccess = false,
            isAdmin = true,
            date = "2026-07-20",
            teamAScore = "2",
            teamBScore = "0",
            teamAPlayerIds = listOf("a"),
            teamBPlayerIds = listOf("b"),
            goalkeeperAIds = listOf("a"),
            goalkeeperBIds = listOf("b"),
            goalEntries = listOf(GoalDraft("a", ActaTeam.A, 1, "b"))
        )

        assertFalse(state.canSubmit)
    }

    @Test
    fun `reports each creation step readiness independently`() {
        val matchReady = NewMatchUiState(
            isAdmin = true,
            date = "2026-07-20",
            teamAScore = "1",
            teamBScore = "0"
        )

        assertTrue(matchReady.hasValidMatchBasics)
        assertFalse(matchReady.hasValidTeams)
        assertFalse(matchReady.hasValidGoals)

        val teamsReady = matchReady.copy(
            teamAPlayerIds = listOf("alex"),
            teamBPlayerIds = listOf("bruno"),
            goalkeeperAIds = listOf("alex"),
            goalkeeperBIds = listOf("bruno")
        )

        assertTrue(teamsReady.hasValidMatchBasics)
        assertTrue(teamsReady.hasValidTeams)
        assertFalse(teamsReady.hasValidGoals)

        val goalsReady = teamsReady.copy(
            goalEntries = listOf(
                GoalDraft("alex", ActaTeam.A, 1, "bruno")
            )
        )

        assertTrue(goalsReady.hasValidGoals)
        assertTrue(goalsReady.canSubmit)
    }

    @Test
    fun `allows a scorer to register goals against different goalkeepers`() {
        val state = NewMatchUiState(
            isAdmin = true,
            date = "2026-07-20",
            teamAScore = "5",
            teamBScore = "0",
            teamAPlayerIds = listOf("arturo"),
            teamBPlayerIds = listOf("marc", "roberto"),
            goalkeeperAIds = listOf("arturo"),
            goalkeeperBIds = listOf("marc", "roberto"),
            goalEntries = listOf(
                GoalDraft("arturo", ActaTeam.A, 2, "marc"),
                GoalDraft("arturo", ActaTeam.A, 3, "roberto")
            )
        )

        assertTrue(state.canSubmit)
        assertTrue(state.goalsFor(ActaTeam.A) == 5)
    }

    @Test
    fun `allows an own goal by a player against their own goalkeeper`() {
        val state = NewMatchUiState(
            isAdmin = true,
            date = "2026-07-20",
            teamAScore = "0",
            teamBScore = "1",
            teamAPlayerIds = listOf("matteo", "arturo"),
            teamBPlayerIds = listOf("bruno"),
            goalkeeperAIds = listOf("arturo"),
            goalkeeperBIds = listOf("bruno"),
            goalEntries = listOf(
                GoalDraft("matteo", ActaTeam.B, 1, "arturo", isOwnGoal = true)
            )
        )

        assertTrue(state.canSubmit)
        assertEquals(0, state.goalsFor(ActaTeam.A))
        assertEquals(1, state.goalsFor(ActaTeam.B))
    }

    @Test
    fun `allows a player to participate and score for both teams`() {
        val state = NewMatchUiState(
            isAdmin = true,
            date = "2026-07-20",
            teamAScore = "1",
            teamBScore = "1",
            teamAPlayerIds = listOf("alex", "carlos"),
            teamBPlayerIds = listOf("bruno", "carlos"),
            goalkeeperAIds = listOf("alex"),
            goalkeeperBIds = listOf("bruno"),
            goalEntries = listOf(
                GoalDraft("carlos", ActaTeam.A, 1, "bruno"),
                GoalDraft("carlos", ActaTeam.B, 1, "alex")
            )
        )

        assertTrue(state.isOnTeam("carlos", ActaTeam.A))
        assertTrue(state.isOnTeam("carlos", ActaTeam.B))
        assertTrue(state.canSubmit)
    }

    @Test
    fun `allows a drawn match decided by a penalty shootout`() {
        val state = NewMatchUiState(
            isAdmin = true,
            date = "2026-07-20",
            teamAScore = "1",
            teamBScore = "1",
            isPenaltyShootout = true,
            teamAPenaltyScore = "5",
            teamBPenaltyScore = "4",
            teamAPlayerIds = listOf("alex"),
            teamBPlayerIds = listOf("bruno"),
            goalkeeperAIds = listOf("alex"),
            goalkeeperBIds = listOf("bruno"),
            goalEntries = listOf(
                GoalDraft("alex", ActaTeam.A, 1, "bruno"),
                GoalDraft("bruno", ActaTeam.B, 1, "alex")
            )
        )

        assertTrue(state.hasValidPenaltyShootout)
        assertTrue(state.canSubmit)
    }

    @Test
    fun `rejects a penalty shootout without a winner`() {
        val state = NewMatchUiState(
            teamAScore = "1",
            teamBScore = "1",
            isPenaltyShootout = true,
            teamAPenaltyScore = "4",
            teamBPenaltyScore = "4"
        )

        assertFalse(state.hasValidPenaltyShootout)
    }
}
