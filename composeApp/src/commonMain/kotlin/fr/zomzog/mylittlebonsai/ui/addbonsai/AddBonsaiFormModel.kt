@file:OptIn(ExperimentalUuidApi::class)

package fr.zomzog.mylittlebonsai.ui.addbonsai

import fr.zomzog.mylittlebonsai.domain.Bonsai
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
