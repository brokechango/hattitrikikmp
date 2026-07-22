package com.brokechango.hattitriki.feature.invitation

import com.brokechango.hattitriki.core.data.AdminPlayer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LeagueInvitationUiStateTest {
    @Test
    fun `accepts a valid email for an active admin`() {
        val state = LeagueInvitationUiState(
            isCheckingAccess = false,
            isAdmin = true,
            players = listOf(AdminPlayer("player-1", "Jugador", true, false)),
            selectedPlayerId = "player-1",
            email = "  Jugador@Ejemplo.com "
        )

        assertTrue(state.canSubmit)
        assertEquals("jugador@ejemplo.com", state.normalizedEmail)
        assertNull(state.emailError)
    }

    @Test
    fun `rejects malformed email addresses`() {
        val state = LeagueInvitationUiState(
            isCheckingAccess = false,
            isAdmin = true,
            email = "jugador.example.com"
        )

        assertFalse(state.canSubmit)
        assertEquals("Indica un correo electrónico válido.", state.emailError)
    }

    @Test
    fun `does not allow members to send invitations`() {
        val state = LeagueInvitationUiState(
            isCheckingAccess = false,
            isAdmin = false,
            email = "jugador@ejemplo.com"
        )

        assertFalse(state.canSubmit)
    }
}
