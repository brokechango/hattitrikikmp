package com.brokechango.hattitriki.feature.teamrandomizer

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class TeamRandomizerTest {
    @Test
    fun `creates balanced teams and includes every participant once`() {
        val participants = listOf("Ana", "Bruno", "Carmen", "Dani", "Elena", "Fran", "Gabi")
            .mapIndexed { index, name -> TeamParticipant(id = index.toString(), name = name) }

        val teams = TeamRandomizer.createTeams(
            participants,
            teamCount = 3,
            balanceStats = false,
            random = Random(8)
        )

        assertEquals(participants.map(TeamParticipant::name).sorted(), teams.flatMap(RandomTeam::players).map(TeamParticipant::name).sorted())
        assertTrue(teams.maxOf { it.players.size } - teams.minOf { it.players.size } <= 1)
        assertEquals(listOf("Equipo 1", "Equipo 2", "Equipo 3"), teams.map(RandomTeam::name))
    }

    @Test
    fun `rejects an impossible number of teams`() {
        assertFailsWith<IllegalArgumentException> {
            TeamRandomizer.createTeams(
                listOf(TeamParticipant("ana", "Ana"), TeamParticipant("bruno", "Bruno")),
                teamCount = 3,
                balanceStats = false
            )
        }
    }

    @Test
    fun `spreads cardio players across teams when possible`() {
        val participants = listOf(
            TeamParticipant("ana", "Ana", hasCardio = true),
            TeamParticipant("bruno", "Bruno", hasCardio = true),
            TeamParticipant("carmen", "Carmen"),
            TeamParticipant("dani", "Dani")
        )

        val teams = TeamRandomizer.createTeams(participants, teamCount = 2, balanceStats = false, random = Random(4))

        assertEquals(listOf(1, 1), teams.map(RandomTeam::cardioPlayers).sorted())
    }

    @Test
    fun `balances registered player statistics when selected`() {
        val participants = listOf(
            TeamParticipant("ana", "Ana", statsScore = 10.0),
            TeamParticipant("bruno", "Bruno", statsScore = 8.0),
            TeamParticipant("carmen", "Carmen", statsScore = 2.0),
            TeamParticipant("dani", "Dani", statsScore = 0.0)
        )

        val teams = TeamRandomizer.createTeams(participants, teamCount = 2, balanceStats = true, random = Random(2))

        assertEquals(0.0, teams[0].statsScore - teams[1].statsScore)
    }

    @Test
    fun `subtracts the team goals conceded per match from a player score`() {
        val score = TeamRandomizer.statsScore(
            goals = 2,
            wins = 1,
            matchesPlayed = 1,
            goalkeeperMatches = 0,
            goalsConcededPerMatch = 3.0
        )

        assertEquals(6.0, score)
    }

    @Test
    fun `allows saving two generated teams made from active players`() {
        val registeredPlayers = listOf(
            TeamParticipant("alex", "Alex"),
            TeamParticipant("bruno", "Bruno")
        )
        val state = TeamRandomizerUiState(
            registeredPlayers = registeredPlayers,
            selectedPlayerIds = registeredPlayers.map(TeamParticipant::id).toSet(),
            teams = listOf(
                RandomTeam("Equipo 1", listOf(registeredPlayers[0])),
                RandomTeam("Equipo 2", listOf(registeredPlayers[1]))
            )
        )

        assertTrue(state.canSaveDraft)
    }

    @Test
    fun `only selected active players take part in the draw`() {
        val registeredPlayers = listOf(
            TeamParticipant("alex", "Alex"),
            TeamParticipant("bruno", "Bruno"),
            TeamParticipant("carmen", "Carmen")
        )
        val state = TeamRandomizerUiState(
            registeredPlayers = registeredPlayers,
            selectedPlayerIds = setOf("alex", "carmen")
        )

        assertEquals(listOf("Alex", "Carmen"), state.participants.map(TeamParticipant::name))
    }

    @Test
    fun `does not save players outside the active roster as a match draft`() {
        val state = TeamRandomizerUiState(
            teams = listOf(
                RandomTeam("Equipo 1", listOf(TeamParticipant("external-ana", "Ana"))),
                RandomTeam("Equipo 2", listOf(TeamParticipant("external-bruno", "Bruno")))
            )
        )

        assertTrue(state.saveDraftRequirement?.contains("plantilla activa") == true)
    }
}
