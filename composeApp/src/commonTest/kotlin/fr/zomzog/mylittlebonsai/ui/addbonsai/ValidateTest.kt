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
        addedOn: LocalDate? = LocalDate(2024, 3, 10),
    ) = AddBonsaiFormState(name = name, addedOn = addedOn)

    @Test
    fun validFormReturnsBonsai() {
        val result = validate(state())
        assertThat(result.bonsai).isNotNull()
        assertThat(result.bonsai!!.name).isEqualTo("Akira")
        assertThat(result.bonsai.addedOn).isEqualTo(LocalDate(2024, 3, 10))
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
        assertThat(result.updatedState.addedOnError).isNull()
    }

    @Test
    fun nullAddedOnSetsDateError() {
        val result = validate(state(addedOn = null))
        assertThat(result.bonsai).isNull()
        assertThat(result.updatedState.addedOnError).isEqualTo(ERROR_ADDED_ON_REQUIRED)
    }

    @Test
    fun multipleErrorsAreAllReported() {
        val result = validate(state(name = "", addedOn = null))
        assertThat(result.bonsai).isNull()
        assertThat(result.updatedState.nameError).isNotNull()
        assertThat(result.updatedState.addedOnError).isNotNull()
    }

    @Test
    fun validationClearsPreviousErrors() {
        val dirtyState = AddBonsaiFormState(
            name = "Akira",
            addedOn = LocalDate(2024, 3, 10),
            nameError = "stale error",
            addedOnError = "stale error",
        )
        val result = validate(dirtyState)
        assertThat(result.bonsai).isNotNull()
        assertThat(result.updatedState.nameError).isNull()
        assertThat(result.updatedState.addedOnError).isNull()
    }
}
