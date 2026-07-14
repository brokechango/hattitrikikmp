package com.brokechango.hattitriki.feature.home

import com.brokechango.hattitriki.core.model.FriendlyMatch

data class HomeUiState(
    val latestMatch: FriendlyMatch?,
    val totalMatches: Int,
    val totalGoals: Int,
    val featuredStats: List<HomeFeaturedStat>
)

data class HomeFeaturedStat(
    val title: String,
    val icon: String,
    val playerName: String,
    val value: String,
    val detail: String
)
