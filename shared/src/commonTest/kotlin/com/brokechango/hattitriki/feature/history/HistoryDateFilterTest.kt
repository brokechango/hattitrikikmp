package com.brokechango.hattitriki.feature.history

import com.brokechango.hattitriki.core.model.FriendlyMatch
import kotlin.test.Test
import kotlin.test.assertEquals

class HistoryDateFilterTest {
    private val matches = listOf(
        match(id = "july-2026", playedOn = "2026-07-22"),
        match(id = "june-2026", playedOn = "2026-06-15"),
        match(id = "july-2025", playedOn = "2025-07-18")
    )

    @Test
    fun `filters matches by selected month and year`() {
        val state = HistoryUiState(
            matches = matches,
            dateFilter = HistoryDateFilter(
                mode = HistoryDateFilterMode.Month,
                month = 7,
                year = 2026
            )
        )

        assertEquals(listOf("july-2026"), state.filteredMatches.map(FriendlyMatch::id))
    }

    @Test
    fun `filters matches by selected year`() {
        val state = HistoryUiState(
            matches = matches,
            dateFilter = HistoryDateFilter(
                mode = HistoryDateFilterMode.Year,
                year = 2025
            )
        )

        assertEquals(listOf("july-2025"), state.filteredMatches.map(FriendlyMatch::id))
    }

    @Test
    fun `filters matches inclusively by a custom range`() {
        val state = HistoryUiState(
            matches = matches,
            dateFilter = HistoryDateFilter(
                mode = HistoryDateFilterMode.Custom,
                startDate = "2026-06-15",
                endDate = "2026-07-22"
            )
        )

        assertEquals(
            listOf("july-2026", "june-2026"),
            state.filteredMatches.map(FriendlyMatch::id)
        )
    }

    @Test
    fun `exposes the available years from newest to oldest`() {
        assertEquals(listOf(2026, 2025), HistoryUiState(matches = matches).availableFilterYears)
    }

    private fun match(id: String, playedOn: String) = FriendlyMatch(
        id = id,
        dateLabel = playedOn,
        teamAScore = 0,
        teamBScore = 0,
        players = emptyList(),
        goals = emptyList(),
        playedOn = playedOn
    )
}
