package com.brokechango.hattitriki.core.model

data class FriendlyMatch(
    val id: String,
    val dateLabel: String,
    val teamAScore: Int,
    val teamBScore: Int,
    val players: List<MatchPlayer>,
    val goals: List<GoalEntry>
) {
    val winner: TeamSide?
        get() = when {
            teamAScore > teamBScore -> TeamSide.A
            teamBScore > teamAScore -> TeamSide.B
            else -> null
        }
}
