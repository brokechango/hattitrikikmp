package com.brokechango.hattitriki.feature.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brokechango.hattitriki.core.model.FriendlyMatch
import com.brokechango.hattitriki.ui.composables.FootballCard
import com.brokechango.hattitriki.ui.composables.HattitrikiDatePickerField
import com.brokechango.hattitriki.ui.composables.HattitrikiPullToRefresh
import com.brokechango.hattitriki.ui.composables.PenaltyScore
import com.brokechango.hattitriki.ui.composables.ScorePill
import com.brokechango.hattitriki.ui.composables.ScreenTitle
import com.brokechango.hattitriki.ui.composables.SupabaseLoadingState
import com.brokechango.hattitriki.ui.preview.HattitrikiPreview
import com.brokechango.hattitriki.ui.preview.PreviewTargets
import hattitriki.shared.generated.resources.Res
import hattitriki.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onEvent: (HistoryEvent) -> Unit,
    scrollState: ScrollState,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HattitrikiPullToRefresh(
        isRefreshing = uiState.isRefreshing,
        onRefresh = viewModel::refresh,
        modifier = modifier
    ) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val columns = if (maxWidth >= 900.dp) 2 else 1
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
        ScreenTitle(
            title = stringResource(Res.string.history_title),
            subtitle = stringResource(Res.string.history_subtitle)
        )
        if (uiState.isLoading) {
            SupabaseLoadingState(message = stringResource(Res.string.history_loading))
            return@Column
        }
        uiState.errorMessage?.let { message ->
            Text(message, color = MaterialTheme.colorScheme.error)
            return@Column
        }
        HistoryDateFilterPanel(
            dateFilter = uiState.dateFilter,
            availableYears = uiState.availableFilterYears,
            onModeSelected = viewModel::selectDateFilter,
            onMonthSelected = viewModel::selectMonth,
            onYearSelected = viewModel::selectYear,
            onStartDateSelected = viewModel::selectCustomStartDate,
            onEndDateSelected = viewModel::selectCustomEndDate,
            onClear = viewModel::clearDateFilter
        )
        Text(
            text = stringResource(Res.string.history_finished_matches),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Black
        )
        val matches = uiState.filteredMatches
        if (matches.isEmpty()) {
            Text(
                stringResource(
                    if (uiState.matches.isEmpty()) {
                        Res.string.history_no_matches
                    } else {
                        Res.string.history_no_filtered_matches
                    }
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        matches.chunked(columns).forEach { matchRow ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                matchRow.forEach { match ->
                    HistoryMatchCard(
                        match = match,
                        onClick = { onEvent(HistoryEvent.OpenMatch(match.id)) },
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(columns - matchRow.size) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
    }
    }
}

@Composable
private fun HistoryMatchCard(
    match: FriendlyMatch,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FootballCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    match.dateLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    stringResource(Res.string.history_final),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Black
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text(stringResource(Res.string.team_a), modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                ScorePill(score = "${match.teamAScore} - ${match.teamBScore}")
                Text(
                    stringResource(Res.string.team_b),
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.Bold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                )
            }
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "",
                    style = MaterialTheme.typography.labelMedium,
                    minLines = 1
                )
                match.penaltyShootout?.let {
                    PenaltyScore(
                        score = "${it.teamAScore} - ${it.teamBScore}",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryDateFilterPanel(
    dateFilter: HistoryDateFilter,
    availableYears: List<Int>,
    onModeSelected: (HistoryDateFilterMode) -> Unit,
    onMonthSelected: (Int) -> Unit,
    onYearSelected: (Int) -> Unit,
    onStartDateSelected: (String) -> Unit,
    onEndDateSelected: (String) -> Unit,
    onClear: () -> Unit
) {
    FootballCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.history_filter_date),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Black
                )
                if (dateFilter.mode != null) {
                    TextButton(onClick = onClear) {
                        Text(stringResource(Res.string.history_clear_filter))
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HistoryDateFilterMode.entries.forEach { mode ->
                    FilterChip(
                        selected = dateFilter.mode == mode,
                        onClick = { onModeSelected(mode) },
                        label = {
                            Text(
                                when (mode) {
                                    HistoryDateFilterMode.Month -> stringResource(Res.string.history_filter_month)
                                    HistoryDateFilterMode.Year -> stringResource(Res.string.history_filter_year)
                                    HistoryDateFilterMode.Custom -> stringResource(Res.string.history_filter_custom)
                                }
                            )
                        }
                    )
                }
            }
            when (dateFilter.mode) {
                HistoryDateFilterMode.Month -> HistoryMonthFilter(
                    selectedMonth = dateFilter.month,
                    selectedYear = dateFilter.year,
                    availableYears = availableYears,
                    onMonthSelected = onMonthSelected,
                    onYearSelected = onYearSelected
                )
                HistoryDateFilterMode.Year -> HistoryYearFilter(
                    selectedYear = dateFilter.year,
                    availableYears = availableYears,
                    onYearSelected = onYearSelected
                )
                HistoryDateFilterMode.Custom -> HistoryCustomDateFilter(
                    startDate = dateFilter.startDate,
                    endDate = dateFilter.endDate,
                    onStartDateSelected = onStartDateSelected,
                    onEndDateSelected = onEndDateSelected
                )
                null -> Unit
            }
        }
    }
}

@Composable
private fun HistoryMonthFilter(
    selectedMonth: Int?,
    selectedYear: Int?,
    availableYears: List<Int>,
    onMonthSelected: (Int) -> Unit,
    onYearSelected: (Int) -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        if (maxWidth >= 480.dp) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                HistoryDateFilterSelector(
                    label = stringResource(Res.string.history_filter_month),
                    selectedValue = selectedMonth?.let(::spanishMonthName) ?: "—",
                    options = (1..12).map { HistoryFilterOption(it.toString(), spanishMonthName(it)) },
                    onSelected = { onMonthSelected(it.toInt()) },
                    modifier = Modifier.weight(1f)
                )
                HistoryDateFilterSelector(
                    label = stringResource(Res.string.history_filter_year),
                    selectedValue = selectedYear?.toString() ?: "—",
                    options = availableYears.map { HistoryFilterOption(it.toString(), it.toString()) },
                    onSelected = { onYearSelected(it.toInt()) },
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                HistoryDateFilterSelector(
                    label = stringResource(Res.string.history_filter_month),
                    selectedValue = selectedMonth?.let(::spanishMonthName) ?: "—",
                    options = (1..12).map { HistoryFilterOption(it.toString(), spanishMonthName(it)) },
                    onSelected = { onMonthSelected(it.toInt()) }
                )
                HistoryDateFilterSelector(
                    label = stringResource(Res.string.history_filter_year),
                    selectedValue = selectedYear?.toString() ?: "—",
                    options = availableYears.map { HistoryFilterOption(it.toString(), it.toString()) },
                    onSelected = { onYearSelected(it.toInt()) }
                )
            }
        }
    }
}

@Composable
private fun HistoryYearFilter(
    selectedYear: Int?,
    availableYears: List<Int>,
    onYearSelected: (Int) -> Unit
) {
    HistoryDateFilterSelector(
        label = stringResource(Res.string.history_filter_year),
        selectedValue = selectedYear?.toString() ?: "—",
        options = availableYears.map { HistoryFilterOption(it.toString(), it.toString()) },
        onSelected = { onYearSelected(it.toInt()) }
    )
}

@Composable
private fun HistoryCustomDateFilter(
    startDate: String,
    endDate: String,
    onStartDateSelected: (String) -> Unit,
    onEndDateSelected: (String) -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        if (maxWidth >= 480.dp) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                HistoryCustomDateField(
                    label = stringResource(Res.string.history_filter_from),
                    value = startDate,
                    onDateSelected = onStartDateSelected,
                    modifier = Modifier.weight(1f)
                )
                HistoryCustomDateField(
                    label = stringResource(Res.string.history_filter_to),
                    value = endDate,
                    onDateSelected = onEndDateSelected,
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                HistoryCustomDateField(
                    label = stringResource(Res.string.history_filter_from),
                    value = startDate,
                    onDateSelected = onStartDateSelected
                )
                HistoryCustomDateField(
                    label = stringResource(Res.string.history_filter_to),
                    value = endDate,
                    onDateSelected = onEndDateSelected
                )
            }
        }
    }
}

@Composable
private fun HistoryCustomDateField(
    label: String,
    value: String,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        HattitrikiDatePickerField(
            value = value,
            onDateSelected = onDateSelected,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun HistoryDateFilterSelector(
    label: String,
    selectedValue: String,
    options: List<HistoryFilterOption>,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { expanded = true },
                enabled = options.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(selectedValue)
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.label) },
                        onClick = {
                            onSelected(option.value)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

private data class HistoryFilterOption(
    val value: String,
    val label: String
)

private fun spanishMonthName(month: Int): String = when (month) {
    1 -> "Enero"
    2 -> "Febrero"
    3 -> "Marzo"
    4 -> "Abril"
    5 -> "Mayo"
    6 -> "Junio"
    7 -> "Julio"
    8 -> "Agosto"
    9 -> "Septiembre"
    10 -> "Octubre"
    11 -> "Noviembre"
    12 -> "Diciembre"
    else -> "—"
}

@PreviewTargets
@Composable
private fun HistoryScreenPreview() {
    val matches = listOf(
        FriendlyMatch("1", "22 jul 2026", 4, 3, emptyList(), emptyList()),
        FriendlyMatch("2", "15 jul 2026", 2, 2, emptyList(), emptyList())
    )
    HattitrikiPreview {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val columns = if (maxWidth >= 900.dp) 2 else 1
            Column(
                modifier = Modifier.fillMaxSize().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ScreenTitle(
                    title = "Historial",
                    subtitle = "Consulta todos los partidos jugados."
                )
                Text(
                    text = "PARTIDOS FINALIZADOS",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Black
                )
                matches.chunked(columns).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        row.forEach { match ->
                            HistoryMatchCard(
                                match = match,
                                onClick = {},
                                modifier = Modifier.weight(1f)
                            )
                        }
                        repeat(columns - row.size) { Box(modifier = Modifier.weight(1f)) }
                    }
                }
            }
        }
    }
}
