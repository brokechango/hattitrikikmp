package com.brokechango.hattitriki.feature.home

import com.brokechango.hattitriki.core.model.FriendlyMatch
import com.brokechango.hattitriki.core.model.PlayerStats

data class HomeUiState(
    val latestMatch: FriendlyMatch?,
    val topScorers: List<PlayerStats>,
    val totalMatches: Int,
    val totalGoals: Int
)
