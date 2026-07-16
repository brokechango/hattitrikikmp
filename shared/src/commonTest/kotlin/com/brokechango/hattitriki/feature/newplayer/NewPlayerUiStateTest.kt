package com.brokechango.hattitriki.feature.newplayer

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NewPlayerUiStateTest {
    @Test
    fun `recognises an existing player as an edit`() {
        assertTrue(NewPlayerUiState(editingPlayerId = "player-1").isEditing)
        assertFalse(NewPlayerUiState().isEditing)
    }

    @Test
    fun `allows an admin to save a player with a name`() {
        assertTrue(
            NewPlayerUiState(
                isCheckingAccess = false,
                isAdmin = true,
                name = "Alex"
            ).canSubmit
        )
    }

    @Test
    fun `rejects blank names and non admin users`() {
        assertFalse(
            NewPlayerUiState(
                isCheckingAccess = false,
                isAdmin = true,
                name = "   "
            ).canSubmit
        )
        assertFalse(
            NewPlayerUiState(
                isCheckingAccess = false,
                isAdmin = false,
                name = "Alex"
            ).canSubmit
        )
    }

    @Test
    fun `defaults cardio status to false`() {
        assertFalse(NewPlayerUiState().hasCardio)
    }
}
