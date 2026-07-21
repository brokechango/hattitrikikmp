package com.brokechango.hattitriki.core.supabase

import kotlin.test.Test
import kotlin.test.assertEquals

class SupabaseErrorHandlingTest {
    @Test
    fun `classifies PostgREST schema cache errors as setup errors`() {
        assertEquals(
            SupabaseErrorKind.SETUP,
            classifySupabaseError(errorCode = "PGRST202")
        )
    }

    @Test
    fun `classifies authorization throttling and temporary API failures by status`() {
        assertEquals(SupabaseErrorKind.UNAUTHORIZED, classifySupabaseError(statusCode = 403))
        assertEquals(SupabaseErrorKind.RATE_LIMITED, classifySupabaseError(statusCode = 429))
        assertEquals(SupabaseErrorKind.UNAVAILABLE, classifySupabaseError(statusCode = 503))
    }

    @Test
    fun `turns a browser network failure into a recovery message`() {
        val message = IllegalStateException("Failed to fetch")
            .toSupabaseUserMessage(testMessages)

        assertEquals("Comprueba tu conexión e inténtalo de nuevo.", message)
    }

    @Test
    fun `does not expose unknown API details to the user`() {
        val message = IllegalStateException("internal database diagnostics")
            .toSupabaseUserMessage(testMessages)

        assertEquals("No se ha podido completar la operación. Inténtalo de nuevo.", message)
    }

    private companion object {
        val testMessages = SupabaseErrorMessages(
            setupMessage = "La configuración de Supabase no está lista.",
            permissionMessage = "No tienes permisos para realizar esta acción.",
            connectionMessage = "Comprueba tu conexión e inténtalo de nuevo.",
            fallbackMessage = "No se ha podido completar la operación. Inténtalo de nuevo."
        )
    }
}
