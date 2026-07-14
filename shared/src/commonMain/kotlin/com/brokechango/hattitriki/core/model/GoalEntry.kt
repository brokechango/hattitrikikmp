package com.brokechango.hattitriki.core.model

data class GoalEntry(
    val playerId: String,
    val team: TeamSide,
    val count: Int,
    val goalkeeperId: String
)
