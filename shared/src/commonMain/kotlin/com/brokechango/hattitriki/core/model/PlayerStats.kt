package com.brokechango.hattitriki.core.model

data class PlayerStats(
    val player: Player,
    val matchesPlayed: Int,
    val wins: Int,
    val draws: Int,
    val losses: Int,
    val goals: Int,
    val goalkeeperMatches: Int
)
