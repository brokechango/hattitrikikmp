package com.brokechango.hattitriki.core.model

data class MatchPlayer(
    val playerId: String,
    val team: TeamSide,
    val wasGoalkeeper: Boolean = false
)
