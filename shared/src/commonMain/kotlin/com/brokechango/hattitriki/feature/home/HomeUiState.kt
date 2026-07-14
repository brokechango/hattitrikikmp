package com.brokechango.hattitriki.feature.home

import com.brokechango.hattitriki.core.model.FriendlyMatch
import com.brokechango.hattitriki.core.model.PlayerRankingCategory

data class HomeUiState(
    val latestMatch: FriendlyMatch?,
    val totalMatches: Int,
    val totalGoals: Int,
    val featuredStats: List<HomeFeaturedStat>
)

data class HomeFeaturedStat(
    val category: PlayerRankingCategory,
    val playerName: String,
    val value: String,
)
