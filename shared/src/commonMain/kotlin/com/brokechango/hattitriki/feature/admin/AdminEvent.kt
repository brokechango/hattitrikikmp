package com.brokechango.hattitriki.feature.admin

sealed interface AdminEvent {
    data class EmailChanged(val value: String) : AdminEvent
    data class PasswordChanged(val value: String) : AdminEvent
    data object SubmitLogin : AdminEvent
    data object LogoutClicked : AdminEvent
}
