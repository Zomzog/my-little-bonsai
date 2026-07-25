package fr.zomzog.mylittlebonsai.ui.addbonsai

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEqualTo
import assertk.assertions.isTrue
import kotlinx.datetime.LocalDate
import kotlin.test.Test

class ValidationResultTest {

    private fun validState() = AddBonsaiFormState(
        name = "Akira",
        kind = "Maple",
        purchaseDate = LocalDate(2024, 3, 10),
    )

    // validate() generates a random UUID for Bonsai.id, so equality tests use an
    // invalid state (bonsai = null) where UUID randomness is not a factor.
    private fun invalidState() = AddBonsaiFormState(name = "", kind = "Maple", purchaseDate = LocalDate(2024, 3, 10))

    @Test
    fun validationResultEqualityForSameInputs() {
        val r1 = validate(invalidState())
        val r2 = validate(invalidState())
        assertThat(r1).isEqualTo(r2)
    }

    @Test
    fun validationResultInequalityForDifferentInputs() {
        val r1 = validate(invalidState())
        val r2 = validate(invalidState().copy(kind = "Pine"))
        assertThat(r1).isNotEqualTo(r2)
    }

    @Test
    fun validationResultHashCodeConsistentWithEquals() {
        val r1 = validate(invalidState())
        val r2 = validate(invalidState())
        assertThat(r1.hashCode()).isEqualTo(r2.hashCode())
    }

    @Test
    fun validationResultToStringIsNonEmpty() {
        val result = validate(validState())
        assertThat(result.toString().isNotEmpty()).isTrue()
    }

    @Test
    fun validationResultCopyChangesUpdatedState() {
        val r1 = validate(validState())
        val r2 = r1.copy(bonsai = null)
        assertThat(r2.bonsai).isEqualTo(null)
        assertThat(r2.updatedState).isEqualTo(r1.updatedState)
    }

    @Test
    fun addBonsaiFormStateToStringIsNonEmpty() {
        val state = validState()
        assertThat(state.toString().isNotEmpty()).isTrue()
    }
}
