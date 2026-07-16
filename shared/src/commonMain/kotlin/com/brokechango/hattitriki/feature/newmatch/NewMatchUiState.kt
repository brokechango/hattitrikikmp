package com.brokechango.hattitriki.feature.newmatch

import com.brokechango.hattitriki.core.data.ActaTeam
import com.brokechango.hattitriki.core.data.AdminPlayer

data class GoalDraft(
    val scorerPlayerId: String,
    val team: ActaTeam,
    val count: Int,
    val goalkeeperPlayerId: String,
    val isOwnGoal: Boolean = false
)

data class NewMatchUiState(
    val editingMatchId: String? = null,
    val isCheckingAccess: Boolean = true,
    val isLoadingPlayers: Boolean = false,
    val isAdmin: Boolean = false,
    val players: List<AdminPlayer> = emptyList(),
    val date: String = "",
    val teamAScore: String = "",
    val teamBScore: String = "",
    val isPenaltyShootout: Boolean = false,
    val teamAPenaltyScore: String = "",
    val teamBPenaltyScore: String = "",
    val teamAPlayerIds: List<String> = emptyList(),
    val teamBPlayerIds: List<String> = emptyList(),
    val goalkeeperAIds: List<String> = emptyList(),
    val goalkeeperBIds: List<String> = emptyList(),
    val goalEntries: List<GoalDraft> = emptyList(),
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
) {
    val isEditing: Boolean
        get() = editingMatchId != null

    val selectedPlayerIds: List<String>
        get() = (teamAPlayerIds + teamBPlayerIds).distinct()

    fun isOnTeam(playerId: String, team: ActaTeam): Boolean = when (team) {
        ActaTeam.A -> playerId in teamAPlayerIds
        ActaTeam.B -> playerId in teamBPlayerIds
    }

    fun goalkeeperIdsFor(team: ActaTeam): List<String> = when (team) {
        ActaTeam.A -> goalkeeperAIds
        ActaTeam.B -> goalkeeperBIds
    }

    val isRegularScoreDraw: Boolean
        get() = teamAScore.toNonNegativeIntOrNull() == teamBScore.toNonNegativeIntOrNull() &&
            teamAScore.toNonNegativeIntOrNull() != null

    val hasValidPenaltyShootout: Boolean
        get() {
            if (!isPenaltyShootout) return true
            val teamAPenalties = teamAPenaltyScore.toNonNegativeIntOrNull()
            val teamBPenalties = teamBPenaltyScore.toNonNegativeIntOrNull()
            return isRegularScoreDraw && teamAPenalties != null && teamBPenalties != null &&
                teamAPenalties != teamBPenalties
        }

    val canSubmit: Boolean
        get() = isAdmin && !isSaving && isValidDate(date) &&
            teamAScore.toNonNegativeIntOrNull() != null &&
            teamBScore.toNonNegativeIntOrNull() != null &&
            hasValidPenaltyShootout &&
            teamAPlayerIds.isNotEmpty() && teamBPlayerIds.isNotEmpty() &&
            goalkeeperAIds.any { it in teamAPlayerIds } &&
            goalkeeperBIds.any { it in teamBPlayerIds } &&
            goalEntries.all(::isValidGoal) &&
            goalsFor(ActaTeam.A) == teamAScore.toInt() &&
            goalsFor(ActaTeam.B) == teamBScore.toInt()

    fun goalsFor(team: ActaTeam): Int = goalEntries
        .filter { it.team == team }
        .sumOf { it.count }

    private fun isValidGoal(goal: GoalDraft): Boolean {
        val scorerTeam = if (goal.isOwnGoal) oppositeOf(goal.team) else goal.team
        val goalkeeperTeam = if (goal.isOwnGoal) scorerTeam else oppositeOf(goal.team)
        return goal.count > 0 && isOnTeam(goal.scorerPlayerId, scorerTeam) &&
            goal.goalkeeperPlayerId in goalkeeperIdsFor(goalkeeperTeam)
    }

    fun oppositeOf(team: ActaTeam): ActaTeam = if (team == ActaTeam.A) ActaTeam.B else ActaTeam.A

    companion object {
        fun isValidDate(value: String): Boolean = Regex("\\d{4}-\\d{2}-\\d{2}").matches(value)
    }
}

private fun String.toNonNegativeIntOrNull(): Int? = toIntOrNull()?.takeIf { it >= 0 }
