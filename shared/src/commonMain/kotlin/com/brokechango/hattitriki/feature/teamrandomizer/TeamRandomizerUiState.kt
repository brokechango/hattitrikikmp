package com.brokechango.hattitriki.feature.teamrandomizer

import com.brokechango.hattitriki.core.data.MatchTeamsDraft

data class TeamRandomizerUiState(
    val participantInput: String = "",
    val teamCountInput: String = "2",
    val registeredPlayers: List<TeamParticipant> = emptyList(),
    val isLoadingRoster: Boolean = false,
    val statsAvailable: Boolean = false,
    val balanceStats: Boolean = false,
    val teams: List<RandomTeam> = emptyList(),
    val savedDraft: MatchTeamsDraft? = null,
    val draftMessage: String? = null,
    val errorMessage: String? = null,
    val rosterMessage: String? = null
) {
    val participants: List<TeamParticipant>
        get() = TeamRandomizer.participantsFrom(participantInput).mapIndexed { index, name ->
            registeredPlayers.firstOrNull { it.name.equals(name, ignoreCase = true) }
                ?: TeamParticipant(id = "manual-$index-$name", name = name)
        }

    val teamCount: Int?
        get() = teamCountInput.toIntOrNull()

    val canGenerate: Boolean
        get() = teamCount != null && participants.size >= 2 && teamCount in 2..participants.size

    val estimatedTeamSize: String
        get() = teamCount?.takeIf { it > 0 }?.let { count ->
            "${participants.size / count}-${(participants.size + count - 1) / count}"
        } ?: "—"

    val selectedCardioPlayers: Int
        get() = participants.count(TeamParticipant::hasCardio)

    val isCurrentResultSaved: Boolean
        get() = teams.size == 2 &&
            savedDraft?.teamAPlayerIds == teams[0].players.map(TeamParticipant::id) &&
            savedDraft.teamBPlayerIds == teams[1].players.map(TeamParticipant::id)

    val canSaveDraft: Boolean
        get() = saveDraftRequirement == null

    val saveDraftRequirement: String?
        get() {
            if (teams.isEmpty()) return "Genera los equipos antes de guardar el borrador."
            if (teams.size != 2) return "Un partido necesita exactamente 2 equipos."

            val playerIds = teams.flatMap { team -> team.players.map(TeamParticipant::id) }
            val registeredPlayerIds = registeredPlayers.map(TeamParticipant::id).toSet()
            if (playerIds.any { it !in registeredPlayerIds }) {
                return "Solo se pueden guardar jugadores de la plantilla activa."
            }
            if (playerIds.distinct().size != playerIds.size) {
                return "Cada jugador debe aparecer una sola vez en el borrador."
            }
            return null
        }
}
