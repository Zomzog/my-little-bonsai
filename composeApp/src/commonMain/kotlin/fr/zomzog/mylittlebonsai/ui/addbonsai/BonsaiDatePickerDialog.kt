package fr.zomzog.mylittlebonsai.ui.addbonsai

import androidx.compose.runtime.Composable
import kotlinx.datetime.LocalDate

@Composable
expect fun BonsaiDatePickerDialog(
    initialDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
)
