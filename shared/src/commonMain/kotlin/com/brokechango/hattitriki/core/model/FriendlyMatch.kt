package com.brokechango.hattitriki.core.model

data class FriendlyMatch(
    val id: String,
    val dateLabel: String,
    val teamAScore: Int,
    val teamBScore: Int,
    val players: List<MatchPlayer>,
    val goals: List<GoalEntry>,
    val penaltyShootout: PenaltyShootout? = null
) {
    init {
        require(penaltyShootout == null || teamAScore == teamBScore) {
            "A penalty shootout can only decide a drawn match."
        }
    }

    val winner: TeamSide?
        get() = when {
            teamAScore > teamBScore -> TeamSide.A
            teamBScore > teamAScore -> TeamSide.B
            else -> penaltyShootout?.winner
        }

    val penaltyShootoutLabel: String?
        get() = penaltyShootout?.let { "${it.teamAScore} - ${it.teamBScore} en penaltis" }
}

data class PenaltyShootout(
    val teamAScore: Int,
    val teamBScore: Int
) {
    init {
        require(teamAScore >= 0 && teamBScore >= 0) { "Penalty scores cannot be negative." }
        require(teamAScore != teamBScore) { "A penalty shootout must have a winner." }
    }

    val winner: TeamSide
        get() = if (teamAScore > teamBScore) TeamSide.A else TeamSide.B
}
