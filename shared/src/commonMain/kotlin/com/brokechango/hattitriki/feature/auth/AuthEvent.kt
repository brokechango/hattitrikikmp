package com.brokechango.hattitriki.feature.auth

sealed interface AuthEvent {
    data class EmailChanged(val value: String) : AuthEvent
    data class PasswordChanged(val value: String) : AuthEvent
    data class InvitationPasswordChanged(val value: String) : AuthEvent
    data class InvitationPasswordConfirmationChanged(val value: String) : AuthEvent
    data object SubmitLogin : AuthEvent
    data object SubmitInvitation : AuthEvent
    data object CancelInvitation : AuthEvent
    data object RetryAccess : AuthEvent
    data object Logout : AuthEvent
}
