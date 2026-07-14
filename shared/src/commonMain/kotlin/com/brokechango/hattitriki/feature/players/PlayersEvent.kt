package com.brokechango.hattitriki.feature.players

import com.brokechango.hattitriki.core.model.PlayerRankingCategory

sealed interface PlayersEvent {
    data class SelectPlayer(val playerId: String) : PlayersEvent
    data class SelectCategory(val category: PlayerRankingCategory) : PlayersEvent
    data class SelectRankingView(val view: RankingView) : PlayersEvent
}
