package com.brokechango.hattitriki.feature.history

sealed interface HistoryEvent {
    data class OpenMatch(val matchId: String) : HistoryEvent
}
