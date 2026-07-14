package com.brokechango.hattitriki.feature.admin

sealed interface AdminEvent {
    data object LoginClicked : AdminEvent
    data object NewMatchClicked : AdminEvent
    data object AddPlayerClicked : AdminEvent
}
