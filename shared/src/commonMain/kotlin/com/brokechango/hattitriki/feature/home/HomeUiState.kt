package com.brokechango.hattitriki.feature.home

import com.brokechango.hattitriki.core.model.FriendlyMatch
import com.brokechango.hattitriki.core.model.PlayerRankingCategory

data class HomeUiState(
    val latestMatch: FriendlyMatch? = null,
    val totalMatches: Int = 0,
    val totalGoals: Int = 0,
    val featuredStats: List<HomeFeaturedStat> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

data class HomeFeaturedStat(
    val category: PlayerRankingCategory,
    val playerName: String,
    val value: String,
)
