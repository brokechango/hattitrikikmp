package com.brokechango.hattitriki.core.logging

import kotlin.test.Test
import kotlin.test.assertEquals

class SupabaseLoggerTest {
    @Test
    fun `keeps database diagnostics but removes request metadata`() {
        val detail = safeSupabaseErrorDetail(
            IllegalStateException(
                "column type mismatch\nCode: 42804\nHeaders: {Authorization=secret}\nHttp Method: POST"
            )
        )

        assertEquals("column type mismatch Code: 42804", detail)
    }
}
