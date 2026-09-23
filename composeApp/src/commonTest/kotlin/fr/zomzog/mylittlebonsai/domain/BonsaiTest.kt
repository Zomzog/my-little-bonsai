package fr.zomzog.mylittlebonsai.domain

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEqualTo
import assertk.assertions.isTrue
import kotlinx.datetime.LocalDate
import kotlin.test.Test

class BonsaiTest {

    private val date = LocalDate(2024, 3, 10)

    private fun bonsai(id: String = "1", name: String = "Akira") = Bonsai(id = id, name = name, addedOn = date)

    @Test
    fun bonsaiEqualityHoldsForIdenticalFields() {
        assertThat(bonsai()).isEqualTo(bonsai())
    }

    @Test
    fun bonsaiInequalityOnDifferentId() {
        assertThat(bonsai(id = "1")).isNotEqualTo(bonsai(id = "2"))
    }

    @Test
    fun bonsaiHashCodeConsistentWithEquals() {
        assertThat(bonsai().hashCode()).isEqualTo(bonsai().hashCode())
    }

    @Test
    fun bonsaiToStringContainsName() {
        assertThat(bonsai().toString().contains("Akira")).isTrue()
    }

    @Test
    fun bonsaiCopyPreservesUnchangedFields() {
        val original = bonsai()
        val copy = original.copy(name = "Bonsuke")
        assertThat(copy.id).isEqualTo("1")
        assertThat(copy.name).isEqualTo("Bonsuke")
        assertThat(copy.addedOn).isEqualTo(date)
    }

    @Test
    fun defaultStatusIsActiveWithNoOptionalFields() {
        val bonsai = bonsai()
        assertThat(bonsai.status).isEqualTo(BonsaiStatus.ACTIVE)
    }
}
