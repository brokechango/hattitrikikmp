package com.brokechango.hattitriki.feature.invitation

import com.brokechango.hattitriki.core.data.AdminPlayer

data class LeagueInvitationUiState(
    val isCheckingAccess: Boolean = true,
    val isAdmin: Boolean = false,
    val isLoadingPlayers: Boolean = false,
    val playersErrorMessage: String? = null,
    val players: List<AdminPlayer> = emptyList(),
    val selectedPlayerId: String? = null,
    val email: String = "",
    val isSending: Boolean = false,
    val sentEmail: String? = null,
    val errorMessage: String? = null
) {
    val normalizedEmail: String
        get() = email.trim().lowercase()

    val emailError: String?
        get() = when {
            email.isBlank() -> null
            !isLeagueInvitationEmailValid(normalizedEmail) -> "Indica un correo electrónico válido."
            else -> null
        }

    val selectedPlayer: AdminPlayer?
        get() = players.firstOrNull { it.id == selectedPlayerId }

    val canSubmit: Boolean
        get() = isAdmin && !isLoadingPlayers && !isSending && selectedPlayer != null &&
            isLeagueInvitationEmailValid(normalizedEmail)
}

internal fun isLeagueInvitationEmailValid(email: String): Boolean =
    email.matches(Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))
