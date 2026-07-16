package com.brokechango.hattitriki.feature.teamrandomizer

data class TeamRandomizerUiState(
    val participantInput: String = "",
    val teamCountInput: String = "2",
    val registeredPlayers: List<TeamParticipant> = emptyList(),
    val isLoadingRoster: Boolean = false,
    val statsAvailable: Boolean = false,
    val balanceStats: Boolean = false,
    val teams: List<RandomTeam> = emptyList(),
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
}
