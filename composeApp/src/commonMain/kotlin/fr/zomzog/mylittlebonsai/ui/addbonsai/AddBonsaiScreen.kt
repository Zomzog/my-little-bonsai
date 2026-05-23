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
const val LABEL_PURCHASE_DATE = "Purchase date (YYYY-MM-DD)"
const val LABEL_LAST_MAINTENANCE = "Last maintenance (YYYY-MM-DD, optional)"
const val BUTTON_ADD = "Add"
const val ERROR_NAME_BLANK = "Name is required"
const val ERROR_KIND_BLANK = "Kind is required"
const val ERROR_DATE_FORMAT = "Use format YYYY-MM-DD"

data class AddBonsaiFormState(
    val name: String = "",
    val kind: String = "",
    val purchaseDateText: String = "",
    val lastMaintenanceDateText: String = "",
    val nameError: String? = null,
    val kindError: String? = null,
    val purchaseDateError: String? = null,
    val lastMaintenanceDateError: String? = null,
)

data class ValidationResult(
    val updatedState: AddBonsaiFormState,
    val bonsai: Bonsai?,
)

fun validate(state: AddBonsaiFormState): ValidationResult {
    var updated = state.copy(
        nameError = null,
        kindError = null,
        purchaseDateError = null,
        lastMaintenanceDateError = null,
    )

    val nameError = if (state.name.isBlank()) ERROR_NAME_BLANK else null
    val kindError = if (state.kind.isBlank()) ERROR_KIND_BLANK else null
    val purchaseDate = runCatching { LocalDate.parse(state.purchaseDateText) }.getOrNull()
    val purchaseDateError = if (purchaseDate == null) ERROR_DATE_FORMAT else null
    val lastMaintenanceDate = if (state.lastMaintenanceDateText.isBlank()) {
        null
    } else {
        runCatching { LocalDate.parse(state.lastMaintenanceDateText) }.getOrNull()
    }
    val lastMaintenanceDateError = if (
        state.lastMaintenanceDateText.isNotBlank() && lastMaintenanceDate == null
    ) ERROR_DATE_FORMAT else null

    updated = updated.copy(
        nameError = nameError,
        kindError = kindError,
        purchaseDateError = purchaseDateError,
        lastMaintenanceDateError = lastMaintenanceDateError,
    )

    val bonsai = if (nameError == null && kindError == null && purchaseDateError == null && lastMaintenanceDateError == null) {
        Bonsai(
            id = Uuid.random().toString(),
            name = state.name,
            kind = state.kind,
            purchaseDate = purchaseDate!!,
            lastMaintenanceDate = lastMaintenanceDate,
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
            OutlinedTextField(
                value = formState.purchaseDateText,
                onValueChange = { formState = formState.copy(purchaseDateText = it, purchaseDateError = null) },
                label = { Text(LABEL_PURCHASE_DATE) },
                isError = formState.purchaseDateError != null,
                supportingText = formState.purchaseDateError?.let { error -> { Text(error) } },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = formState.lastMaintenanceDateText,
                onValueChange = { formState = formState.copy(lastMaintenanceDateText = it, lastMaintenanceDateError = null) },
                label = { Text(LABEL_LAST_MAINTENANCE) },
                isError = formState.lastMaintenanceDateError != null,
                supportingText = formState.lastMaintenanceDateError?.let { error -> { Text(error) } },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(16.dp))
            if (formState.nameError != null || formState.kindError != null ||
                formState.purchaseDateError != null || formState.lastMaintenanceDateError != null
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
}
