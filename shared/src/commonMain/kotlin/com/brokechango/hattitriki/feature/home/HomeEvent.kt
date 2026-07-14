package com.brokechango.hattitriki.feature.home

import com.brokechango.hattitriki.core.model.PlayerRankingCategory

sealed interface HomeEvent {
    data object OpenHistory : HomeEvent
    data class OpenPlayers(val category: PlayerRankingCategory) : HomeEvent
    data object OpenAdmin : HomeEvent
    data class OpenMatch(val matchId: String) : HomeEvent
}
