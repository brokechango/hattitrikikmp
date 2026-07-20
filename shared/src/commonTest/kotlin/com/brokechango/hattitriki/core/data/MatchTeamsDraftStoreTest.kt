package com.brokechango.hattitriki.core.data

import com.russhwolf.settings.MapSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MatchTeamsDraftStoreTest {
    @Test
    fun `persists restores and clears a match teams draft`() {
        val store = MultiplatformSettingsMatchTeamsDraftStore(MapSettings())
        val draft = MatchTeamsDraft(
            teamAPlayerIds = listOf("alex", "carmen"),
            teamBPlayerIds = listOf("bruno", "dani")
        )

        store.save(draft)

        assertEquals(draft, store.load())

        store.clear()

        assertNull(store.load())
    }
}
