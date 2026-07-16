package com.brokechango.hattitriki.feature.newplayer

sealed interface NewPlayerEvent {
    data class NameChanged(val value: String) : NewPlayerEvent
    data class HasCardioChanged(val value: Boolean) : NewPlayerEvent
    data object Submit : NewPlayerEvent
}
