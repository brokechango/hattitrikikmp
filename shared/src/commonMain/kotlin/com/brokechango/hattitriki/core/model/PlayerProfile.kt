package com.brokechango.hattitriki.core.model

data class PlayerConnection(
    val player: Player,
    val matches: Int,
    val isTied: Boolean
)

data class PlayerProfileSummary(
    val stats: PlayerStats,
    val maximumRival: PlayerConnection?,
    val inseparableTeammate: PlayerConnection?
)
