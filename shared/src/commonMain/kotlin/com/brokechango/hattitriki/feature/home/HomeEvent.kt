package com.brokechango.hattitriki.feature.home

sealed interface HomeEvent {
    data object OpenHistory : HomeEvent
    data object OpenPlayers : HomeEvent
    data object OpenAdmin : HomeEvent
    data class OpenMatch(val matchId: String) : HomeEvent
}
