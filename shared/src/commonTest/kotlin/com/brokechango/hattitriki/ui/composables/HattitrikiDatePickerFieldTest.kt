package com.brokechango.hattitriki.ui.composables

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class HattitrikiDatePickerFieldTest {
    @Test
    fun `keeps dates in the persisted ISO format`() {
        assertEquals(
            "2026-07-16",
            utcMillisToIsoDate(requireNotNull(isoDateToUtcMillis("2026-07-16")))
        )
    }

    @Test
    fun `accepts leap day and rejects invalid dates`() {
        assertEquals(
            "2024-02-29",
            utcMillisToIsoDate(requireNotNull(isoDateToUtcMillis("2024-02-29")))
        )
        assertNull(isoDateToUtcMillis("2025-02-29"))
        assertNull(isoDateToUtcMillis("fecha"))
    }
}
