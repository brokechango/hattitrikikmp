package com.brokechango.hattitriki.feature.matchdetail

sealed interface MatchDetailEvent {
    data object Back : MatchDetailEvent
}
