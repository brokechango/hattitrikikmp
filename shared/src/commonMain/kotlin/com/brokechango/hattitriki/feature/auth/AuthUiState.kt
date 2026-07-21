package com.brokechango.hattitriki.feature.auth

import com.brokechango.hattitriki.core.auth.LeagueAccess

sealed interface AuthGateState {
    data object Loading : AuthGateState
    data object SignedOut : AuthGateState
    data object InvitationSetup : AuthGateState
    data class Authenticated(val access: LeagueAccess) : AuthGateState
    data class AccessError(val message: String) : AuthGateState
}

data class AuthUiState(
    val gateState: AuthGateState = AuthGateState.Loading,
    val email: String = "",
    val password: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isSubmitting: Boolean = false,
    val isAuthConfigured: Boolean = true,
    val errorMessage: String? = null
) {
    val canSubmitLogin: Boolean
        get() = gateState == AuthGateState.SignedOut &&
            isAuthConfigured &&
            !isSubmitting &&
            email.isNotBlank() &&
            password.isNotBlank()

    val newPasswordError: String?
        get() = if (newPassword.isNotEmpty() && newPassword.length < MIN_PASSWORD_LENGTH) {
            "Usa al menos $MIN_PASSWORD_LENGTH caracteres."
        } else {
            null
        }

    val confirmPasswordError: String?
        get() = if (confirmPassword.isNotEmpty() && confirmPassword != newPassword) {
            "Las contraseñas no coinciden."
        } else {
            null
        }

    val canSubmitInvitation: Boolean
        get() = gateState == AuthGateState.InvitationSetup &&
            isAuthConfigured &&
            !isSubmitting &&
            newPassword.length >= MIN_PASSWORD_LENGTH &&
            confirmPassword == newPassword

    private companion object {
        const val MIN_PASSWORD_LENGTH = 8
    }
}
