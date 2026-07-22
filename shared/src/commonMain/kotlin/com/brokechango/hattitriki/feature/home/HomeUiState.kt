package com.brokechango.hattitriki.feature.home

import com.brokechango.hattitriki.core.model.FriendlyMatch
import com.brokechango.hattitriki.core.model.PlayerRankingCategory

data class HomeUiState(
    val latestMatch: FriendlyMatch? = null,
    val totalMatches: Int = 0,
    val totalGoals: Int = 0,
    val featuredStats: List<HomeFeaturedStat> = emptyList(),
    val avatarUrlsByPlayerId: Map<String, String> = emptyMap(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
)

data class HomeFeaturedStat(
    val category: PlayerRankingCategory,
    val playerId: String,
    val playerName: String,
    val value: String,
)
