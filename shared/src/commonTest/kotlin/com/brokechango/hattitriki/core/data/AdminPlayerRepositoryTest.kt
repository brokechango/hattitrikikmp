package com.brokechango.hattitriki.core.data

import kotlin.test.Test
import kotlin.test.assertEquals

class AdminPlayerRepositoryTest {
    @Test
    fun `translates Supabase setup errors into an actionable message`() {
        assertEquals(
            "La configuración de jugadores en Supabase no está lista. Aplica la última migración y vuelve a intentarlo.",
            playerSaveErrorMessage(IllegalStateException("PGRST205: Could not find the table public.players"))
        )
    }

    @Test
    fun `does not expose technical errors to the player form`() {
        assertEquals(
            "No se ha podido guardar el jugador. Inténtalo de nuevo.",
            playerSaveErrorMessage(IllegalStateException("unexpected database exception with internal details"))
        )
    }

    @Test
    fun `translates acta setup errors without exposing PostgREST details`() {
        assertEquals(
            "La configuración para guardar actas en Supabase no está lista. Aplica la última migración y vuelve a intentarlo.",
            matchSaveErrorMessage(IllegalStateException("PGRST202: Could not find the function"))
        )
    }
}
