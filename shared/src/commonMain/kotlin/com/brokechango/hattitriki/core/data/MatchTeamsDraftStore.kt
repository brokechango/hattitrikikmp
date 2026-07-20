package com.brokechango.hattitriki.core.data

import com.russhwolf.settings.Settings

data class MatchTeamsDraft(
    val teamAPlayerIds: List<String>,
    val teamBPlayerIds: List<String>
) {
    init {
        require(teamAPlayerIds.isNotEmpty() && teamBPlayerIds.isNotEmpty()) {
            "El borrador necesita jugadores en ambos equipos."
        }
    }
}

interface MatchTeamsDraftStore {
    fun load(): MatchTeamsDraft?

    fun save(draft: MatchTeamsDraft)

    fun clear()
}

class MultiplatformSettingsMatchTeamsDraftStore(
    private val settings: Settings
) : MatchTeamsDraftStore {
    override fun load(): MatchTeamsDraft? {
        val teamAPlayerIds = settings.getStringOrNull(TEAM_A_KEY).decodePlayerIds()
        val teamBPlayerIds = settings.getStringOrNull(TEAM_B_KEY).decodePlayerIds()
        if (teamAPlayerIds.isEmpty() || teamBPlayerIds.isEmpty()) return null

        return MatchTeamsDraft(
            teamAPlayerIds = teamAPlayerIds,
            teamBPlayerIds = teamBPlayerIds
        )
    }

    override fun save(draft: MatchTeamsDraft) {
        settings.putString(TEAM_A_KEY, draft.teamAPlayerIds.encodePlayerIds())
        settings.putString(TEAM_B_KEY, draft.teamBPlayerIds.encodePlayerIds())
    }

    override fun clear() {
        settings.putString(TEAM_A_KEY, "")
        settings.putString(TEAM_B_KEY, "")
    }

    private fun List<String>.encodePlayerIds(): String {
        require(none { '\n' in it || '\r' in it }) {
            "Los identificadores de jugador no pueden contener saltos de línea."
        }
        return joinToString("\n")
    }

    private fun String?.decodePlayerIds(): List<String> = this
        .orEmpty()
        .lineSequence()
        .map(String::trim)
        .filter(String::isNotEmpty)
        .distinct()
        .toList()

    private companion object {
        const val TEAM_A_KEY = "match_teams_draft_team_a"
        const val TEAM_B_KEY = "match_teams_draft_team_b"
    }
}

object NoOpMatchTeamsDraftStore : MatchTeamsDraftStore {
    override fun load(): MatchTeamsDraft? = null

    override fun save(draft: MatchTeamsDraft) = Unit

    override fun clear() = Unit
}
