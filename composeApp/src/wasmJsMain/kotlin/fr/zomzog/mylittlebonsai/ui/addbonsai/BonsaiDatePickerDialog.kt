package fr.zomzog.mylittlebonsai.ui.addbonsai

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate

@Composable
actual fun BonsaiDatePickerDialog(
    initialDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    val init = initialDate ?: LocalDate(2024, 1, 1)
    var year by remember { mutableStateOf(init.year.toString()) }
    var month by remember { mutableStateOf(init.monthNumber.toString()) }
    var day by remember { mutableStateOf(init.dayOfMonth.toString()) }
    var dateError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Date") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (dateError) {
                    Text(
                        text = "Invalid date",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = year,
                        onValueChange = { year = it; dateError = false },
                        label = { Text("Year") },
                        modifier = Modifier.weight(2f),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = month,
                        onValueChange = { month = it; dateError = false },
                        label = { Text("Month") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = day,
                        onValueChange = { day = it; dateError = false },
                        label = { Text("Day") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val date = runCatching {
                    LocalDate(year.toInt(), month.toInt(), day.toInt())
                }.getOrNull()
                if (date != null) {
                    onDateSelected(date)
                    onDismiss()
                } else {
                    dateError = true
                }
            }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
