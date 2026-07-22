package com.brokechango.hattitriki.feature.history

import com.brokechango.hattitriki.core.model.FriendlyMatch

data class HistoryUiState(
    val matches: List<FriendlyMatch> = emptyList(),
    val dateFilter: HistoryDateFilter = HistoryDateFilter(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
)

enum class HistoryDateFilterMode {
    Month,
    Year,
    Custom
}

data class HistoryDateFilter(
    val mode: HistoryDateFilterMode? = null,
    val month: Int? = null,
    val year: Int? = null,
    val startDate: String = "",
    val endDate: String = ""
) {
    fun includes(match: FriendlyMatch): Boolean {
        val date = match.playedOn.toIsoDateOrNull() ?: return mode == null

        return when (mode) {
            null -> true
            HistoryDateFilterMode.Month -> date.year == year && date.month == month
            HistoryDateFilterMode.Year -> date.year == year
            HistoryDateFilterMode.Custom ->
                (startDate.isBlank() || date.iso >= startDate) &&
                    (endDate.isBlank() || date.iso <= endDate)
        }
    }
}

internal val HistoryUiState.filteredMatches: List<FriendlyMatch>
    get() = matches.filter(dateFilter::includes)

internal val HistoryUiState.availableFilterYears: List<Int>
    get() = matches
        .mapNotNull { it.playedOn.toIsoDateOrNull()?.year }
        .distinct()
        .sortedDescending()

internal fun FriendlyMatch.filterDate(): HistoryFilterDate? = playedOn.toIsoDateOrNull()

internal data class HistoryFilterDate(
    val iso: String,
    val year: Int,
    val month: Int
)

private fun String.toIsoDateOrNull(): HistoryFilterDate? {
    val parts = split('-')
    if (parts.size != 3) return null

    val year = parts[0].toIntOrNull() ?: return null
    val month = parts[1].toIntOrNull() ?: return null
    val day = parts[2].toIntOrNull() ?: return null
    if (year !in 1..9999 || month !in 1..12 || day !in 1..31) return null

    return HistoryFilterDate(
        iso = year.toString().padStart(4, '0') + "-" +
            month.toString().padStart(2, '0') + "-" +
            day.toString().padStart(2, '0'),
        year = year,
        month = month
    )
}
