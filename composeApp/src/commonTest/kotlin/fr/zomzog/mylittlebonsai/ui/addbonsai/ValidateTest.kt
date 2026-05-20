package fr.zomzog.mylittlebonsai.ui.addbonsai

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import kotlinx.datetime.LocalDate
import kotlin.test.Test

class ValidateTest {

    private fun state(
        name: String = "Akira",
        kind: String = "Maple",
        purchaseDate: String = "2024-03-10",
        lastMaintenance: String = "",
    ) = AddBonsaiFormState(
        name = name,
        kind = kind,
        purchaseDateText = purchaseDate,
        lastMaintenanceDateText = lastMaintenance,
    )

    @Test
    fun validFormReturnsBonsai() {
        val result = validate(state())
        assertThat(result.bonsai).isNotNull()
        assertThat(result.bonsai!!.name).isEqualTo("Akira")
        assertThat(result.bonsai.kind).isEqualTo("Maple")
        assertThat(result.bonsai.purchaseDate).isEqualTo(LocalDate(2024, 3, 10))
        assertThat(result.bonsai.lastMaintenanceDate).isNull()
    }

    @Test
    fun validFormWithMaintenanceDateReturnsBonsai() {
        val result = validate(state(lastMaintenance = "2025-01-20"))
        assertThat(result.bonsai).isNotNull()
        assertThat(result.bonsai!!.lastMaintenanceDate).isEqualTo(LocalDate(2025, 1, 20))
    }

    @Test
    fun blankNameSetsNameError() {
        val result = validate(state(name = ""))
        assertThat(result.bonsai).isNull()
        assertThat(result.updatedState.nameError).isEqualTo(ERROR_NAME_BLANK)
    }

    @Test
    fun blankNameDoesNotAffectOtherErrors() {
        val result = validate(state(name = ""))
        assertThat(result.updatedState.kindError).isNull()
        assertThat(result.updatedState.purchaseDateError).isNull()
    }

    @Test
    fun blankKindSetsKindError() {
        val result = validate(state(kind = ""))
        assertThat(result.bonsai).isNull()
        assertThat(result.updatedState.kindError).isEqualTo(ERROR_KIND_BLANK)
    }

    @Test
    fun invalidPurchaseDateSetsDateError() {
        val result = validate(state(purchaseDate = "not-a-date"))
        assertThat(result.bonsai).isNull()
        assertThat(result.updatedState.purchaseDateError).isEqualTo(ERROR_DATE_FORMAT)
    }

    @Test
    fun emptyPurchaseDateSetsDateError() {
        val result = validate(state(purchaseDate = ""))
        assertThat(result.bonsai).isNull()
        assertThat(result.updatedState.purchaseDateError).isEqualTo(ERROR_DATE_FORMAT)
    }

    @Test
    fun invalidMaintenanceDateSetsMaintenanceDateError() {
        val result = validate(state(lastMaintenance = "bad-date"))
        assertThat(result.bonsai).isNull()
        assertThat(result.updatedState.lastMaintenanceDateError).isEqualTo(ERROR_DATE_FORMAT)
    }

    @Test
    fun blankMaintenanceDateIsAccepted() {
        val result = validate(state(lastMaintenance = ""))
        assertThat(result.updatedState.lastMaintenanceDateError).isNull()
    }

    @Test
    fun multipleErrorsAreAllReported() {
        val result = validate(state(name = "", kind = "", purchaseDate = "bad"))
        assertThat(result.bonsai).isNull()
        assertThat(result.updatedState.nameError).isNotNull()
        assertThat(result.updatedState.kindError).isNotNull()
        assertThat(result.updatedState.purchaseDateError).isNotNull()
    }

    @Test
    fun validationClearsPreviousErrors() {
        val dirtyState = AddBonsaiFormState(
            name = "Akira",
            kind = "Maple",
            purchaseDateText = "2024-03-10",
            nameError = "stale error",
            kindError = "stale error",
        )
        val result = validate(dirtyState)
        assertThat(result.bonsai).isNotNull()
        assertThat(result.updatedState.nameError).isNull()
        assertThat(result.updatedState.kindError).isNull()
    }
}
