package com.brokechango.hattitriki.feature.auth

import com.brokechango.hattitriki.core.auth.LeagueRole
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AuthUiStateTest {
    @Test
    fun loginRequiresConfiguredCompleteForm() {
        assertFalse(AuthUiState(gateState = AuthGateState.SignedOut).canSubmitLogin)
        assertFalse(
            AuthUiState(
                gateState = AuthGateState.SignedOut,
                email = "member@example.com",
                password = "secret",
                isAuthConfigured = false
            ).canSubmitLogin
        )
        assertTrue(
            AuthUiState(
                gateState = AuthGateState.SignedOut,
                email = "member@example.com",
                password = "secret"
            ).canSubmitLogin
        )
    }

    @Test
    fun invitationRequiresSecureMatchingPasswords() {
        val invitationState = AuthUiState(gateState = AuthGateState.InvitationSetup)

        assertFalse(invitationState.canSubmitInvitation)
        assertFalse(
            invitationState.copy(
                newPassword = "corta",
                confirmPassword = "corta"
            ).canSubmitInvitation
        )
        assertFalse(
            invitationState.copy(
                newPassword = "segura123",
                confirmPassword = "distinta123"
            ).canSubmitInvitation
        )
        assertTrue(
            invitationState.copy(
                newPassword = "segura123",
                confirmPassword = "segura123"
            ).canSubmitInvitation
        )
    }

    @Test
    fun invitationValidationExplainsInvalidFields() {
        val state = AuthUiState(
            gateState = AuthGateState.InvitationSetup,
            newPassword = "corta",
            confirmPassword = "distinta"
        )

        assertEquals("Usa al menos 8 caracteres.", state.newPasswordError)
        assertEquals("Las contraseñas no coinciden.", state.confirmPasswordError)
    }

    @Test
    fun databaseRolesAreStrictlyMapped() {
        assertEquals(LeagueRole.MEMBER, LeagueRole.fromDatabaseValue("member"))
        assertEquals(LeagueRole.ADMIN, LeagueRole.fromDatabaseValue("ADMIN"))
        assertNull(LeagueRole.fromDatabaseValue("owner"))
        assertNull(LeagueRole.fromDatabaseValue(null))
    }
}
