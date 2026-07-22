package com.brokechango.hattitriki.core.model

/** Raw values shared by the Rankings filters and player profiles. */
data class PlayerRankingMetrics(
    val goalsPerMatch: Double,
    val goalsAgainst: Double?,
    val goalsAgainstPerMatch: Double?,
    val assignedGoalsAgainst: Int?,
    val totalPerformance: Int
)
