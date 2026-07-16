package com.brokechango.hattitriki.ui.composables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.brokechango.hattitriki.core.design.CrestGold
import com.brokechango.hattitriki.core.design.CrestNavy
import com.brokechango.hattitriki.core.design.CrestWhite

/**
 * Campo de fecha con la estética de Hattitriki. [value] y [onDateSelected]
 * usan siempre el formato estable `AAAA-MM-DD` que persiste el acta.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HattitrikiDatePickerField(
    value: String,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var isPickerVisible by remember { mutableStateOf(false) }

    OutlinedButton(
        onClick = { isPickerVisible = true },
        enabled = enabled,
        modifier = modifier,
        border = BorderStroke(1.dp, CrestGold),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = CrestWhite)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 3.dp)
        ) {
            Text("Fecha", color = CrestGold)
            Text(if (value.isBlank()) "Seleccionar fecha" else value)
        }
    }

    if (isPickerVisible) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = isoDateToUtcMillis(value)
        )
        DatePickerDialog(
            onDismissRequest = { isPickerVisible = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { selectedDateMillis ->
                            onDateSelected(utcMillisToIsoDate(selectedDateMillis))
                        }
                        isPickerVisible = false
                    },
                    enabled = datePickerState.selectedDateMillis != null,
                    colors = ButtonDefaults.textButtonColors(contentColor = CrestGold)
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { isPickerVisible = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = CrestGold)
                ) {
                    Text("Cancelar")
                }
            },
            colors = androidx.compose.material3.DatePickerDefaults.colors(
                containerColor = CrestNavy
            )
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

internal fun isoDateToUtcMillis(value: String): Long? {
    val (year, month, day) = value.split('-').mapNotNull(String::toIntOrNull).takeIf { it.size == 3 }
        ?: return null
    if (month !in 1..12 || day !in 1..daysInMonth(year, month)) return null
    return daysFromCivil(year, month, day) * MILLIS_PER_DAY
}

internal fun utcMillisToIsoDate(millis: Long): String {
    val (year, month, day) = civilFromDays(floorDiv(millis, MILLIS_PER_DAY))
    return "%04d-%02d-%02d".format(year, month, day)
}

private const val MILLIS_PER_DAY = 86_400_000L

private fun daysInMonth(year: Int, month: Int): Int = when (month) {
    2 -> if (isLeapYear(year)) 29 else 28
    4, 6, 9, 11 -> 30
    else -> 31
}

private fun isLeapYear(year: Int): Boolean = year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)

private fun daysFromCivil(year: Int, month: Int, day: Int): Long {
    val adjustedYear = year - if (month <= 2) 1 else 0
    val era = floorDiv(adjustedYear.toLong(), 400L)
    val yearOfEra = adjustedYear - (era * 400L).toInt()
    val monthPrime = month + if (month > 2) -3 else 9
    val dayOfYear = (153 * monthPrime + 2) / 5 + day - 1
    val dayOfEra = yearOfEra * 365 + yearOfEra / 4 - yearOfEra / 100 + dayOfYear
    return era * 146_097L + dayOfEra - 719_468L
}

private fun civilFromDays(daysSinceEpoch: Long): Triple<Int, Int, Int> {
    val shiftedDays = daysSinceEpoch + 719_468L
    val era = floorDiv(shiftedDays, 146_097L)
    val dayOfEra = shiftedDays - era * 146_097L
    val yearOfEra = (dayOfEra - dayOfEra / 1_460L + dayOfEra / 36_524L - dayOfEra / 146_096L) / 365L
    var year = (yearOfEra + era * 400L).toInt()
    val dayOfYear = (dayOfEra - (365L * yearOfEra + yearOfEra / 4L - yearOfEra / 100L)).toInt()
    val monthPrime = (5 * dayOfYear + 2) / 153
    val day = dayOfYear - (153 * monthPrime + 2) / 5 + 1
    val month = monthPrime + if (monthPrime < 10) 3 else -9
    if (month <= 2) year += 1
    return Triple(year, month, day)
}

private fun floorDiv(value: Long, divisor: Long): Long =
    if (value >= 0) value / divisor else -((-value + divisor - 1) / divisor)
