package com.brokechango.hattitriki.feature.players

sealed interface PlayersEvent {
    data class SelectPlayer(val playerId: String) : PlayersEvent
}
