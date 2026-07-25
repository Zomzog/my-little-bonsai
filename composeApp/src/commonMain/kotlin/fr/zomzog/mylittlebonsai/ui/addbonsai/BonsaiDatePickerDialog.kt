package fr.zomzog.mylittlebonsai.ui.addbonsai

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn

// DatePickerState stores dates as UTC-midnight epoch milliseconds.
// millis / 86_400_000 is always an exact integer (no timezone conversion needed).
private fun LocalDate.toPickerMillis(): Long =
    LocalDate(1970, 1, 1).daysUntil(this).toLong() * 86_400_000L

private fun Long.toLocalDate(): LocalDate =
    LocalDate(1970, 1, 1).plus((this / 86_400_000L).toInt(), DateTimeUnit.DAY)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BonsaiDatePickerDialog(
    initialDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = (initialDate ?: today).toPickerMillis(),
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let { millis ->
                    onDateSelected(millis.toLocalDate())
                }
                onDismiss()
            }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    ) {
        DatePicker(state = datePickerState)
    }
}
