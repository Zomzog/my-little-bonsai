@file:OptIn(ExperimentalUuidApi::class)

package fr.zomzog.mylittlebonsai.ui.addbonsai

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.zomzog.mylittlebonsai.domain.Bonsai
import fr.zomzog.mylittlebonsai.domain.BonsaiRepository
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

const val ADD_BONSAI_TITLE = "Add Bonsai"
const val LABEL_NAME = "Name"
const val LABEL_KIND = "Kind"
const val LABEL_PURCHASE_DATE = "Purchase date"
const val LABEL_LAST_MAINTENANCE = "Last maintenance (optional)"
const val BUTTON_ADD = "Add"
const val ERROR_NAME_BLANK = "Name is required"
const val ERROR_KIND_BLANK = "Kind is required"
const val ERROR_PURCHASE_DATE_REQUIRED = "Purchase date is required"

data class AddBonsaiFormState(
    val name: String = "",
    val kind: String = "",
    val purchaseDate: LocalDate? = null,
    val lastMaintenanceDate: LocalDate? = null,
    val nameError: String? = null,
    val kindError: String? = null,
    val purchaseDateError: String? = null,
)

data class ValidationResult(
    val updatedState: AddBonsaiFormState,
    val bonsai: Bonsai?,
)

fun validate(state: AddBonsaiFormState): ValidationResult {
    val nameError = if (state.name.isBlank()) ERROR_NAME_BLANK else null
    val kindError = if (state.kind.isBlank()) ERROR_KIND_BLANK else null
    val purchaseDateError = if (state.purchaseDate == null) ERROR_PURCHASE_DATE_REQUIRED else null

    val updated = state.copy(
        nameError = nameError,
        kindError = kindError,
        purchaseDateError = purchaseDateError,
    )

    val bonsai = if (nameError == null && kindError == null && purchaseDateError == null) {
        Bonsai(
            id = Uuid.random().toString(),
            name = state.name,
            kind = state.kind,
            purchaseDate = state.purchaseDate!!,
            lastMaintenanceDate = state.lastMaintenanceDate,
        )
    } else {
        null
    }

    return ValidationResult(updatedState = updated, bonsai = bonsai)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBonsaiScreen(
    repository: BonsaiRepository,
    onBonsaiAdded: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var formState by remember { mutableStateOf(AddBonsaiFormState()) }
    val coroutineScope = rememberCoroutineScope()
    var showPurchaseDatePicker by remember { mutableStateOf(false) }
    var showMaintenanceDatePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(ADD_BONSAI_TITLE) })
        },
        modifier = modifier,
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp),
        ) {
            OutlinedTextField(
                value = formState.name,
                onValueChange = { formState = formState.copy(name = it, nameError = null) },
                label = { Text(LABEL_NAME) },
                isError = formState.nameError != null,
                supportingText = formState.nameError?.let { error -> { Text(error) } },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = formState.kind,
                onValueChange = { formState = formState.copy(kind = it, kindError = null) },
                label = { Text(LABEL_KIND) },
                isError = formState.kindError != null,
                supportingText = formState.kindError?.let { error -> { Text(error) } },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = { showPurchaseDatePicker = true },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(formState.purchaseDate?.toString() ?: LABEL_PURCHASE_DATE)
            }
            if (formState.purchaseDateError != null) {
                Text(
                    text = formState.purchaseDateError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = { showMaintenanceDatePicker = true },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(formState.lastMaintenanceDate?.toString() ?: LABEL_LAST_MAINTENANCE)
            }
            Spacer(Modifier.height(16.dp))
            if (formState.nameError != null || formState.kindError != null ||
                formState.purchaseDateError != null
            ) {
                Text(
                    text = "Please fix the errors above",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
                Spacer(Modifier.height(8.dp))
            }
            Button(
                onClick = {
                    val result = validate(formState)
                    if (result.bonsai != null) {
                        coroutineScope.launch {
                            repository.addBonsai(result.bonsai)
                            onBonsaiAdded()
                        }
                    } else {
                        formState = result.updatedState
                    }
                },
                modifier = Modifier.align(Alignment.End),
            ) {
                Text(BUTTON_ADD)
            }
        }
    }

    if (showPurchaseDatePicker) {
        BonsaiDatePickerDialog(
            initialDate = formState.purchaseDate,
            onDateSelected = { date ->
                formState = formState.copy(purchaseDate = date, purchaseDateError = null)
                showPurchaseDatePicker = false
            },
            onDismiss = { showPurchaseDatePicker = false },
        )
    }

    if (showMaintenanceDatePicker) {
        BonsaiDatePickerDialog(
            initialDate = formState.lastMaintenanceDate,
            onDateSelected = { date ->
                formState = formState.copy(lastMaintenanceDate = date)
                showMaintenanceDatePicker = false
            },
            onDismiss = { showMaintenanceDatePicker = false },
        )
    }
}
