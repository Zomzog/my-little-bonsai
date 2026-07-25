package fr.zomzog.mylittlebonsai.domain

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEqualTo
import assertk.assertions.isTrue
import kotlinx.datetime.LocalDate
import kotlin.test.Test

class BonsaiTest {

    private val date = LocalDate(2024, 3, 10)

    @Test
    fun bonsaiEqualityHoldsForIdenticalFields() {
        val a = Bonsai("1", "Akira", "Maple", date)
        val b = Bonsai("1", "Akira", "Maple", date)
        assertThat(a).isEqualTo(b)
    }

    @Test
    fun bonsaiInequalityOnDifferentId() {
        val a = Bonsai("1", "Akira", "Maple", date)
        val b = Bonsai("2", "Akira", "Maple", date)
        assertThat(a).isNotEqualTo(b)
    }

    @Test
    fun bonsaiHashCodeConsistentWithEquals() {
        val a = Bonsai("1", "Akira", "Maple", date)
        val b = Bonsai("1", "Akira", "Maple", date)
        assertThat(a.hashCode()).isEqualTo(b.hashCode())
    }

    @Test
    fun bonsaiToStringContainsName() {
        val bonsai = Bonsai("1", "Akira", "Maple", date)
        assertThat(bonsai.toString().contains("Akira")).isTrue()
    }

    @Test
    fun bonsaiCopyPreservesUnchangedFields() {
        val original = Bonsai("1", "Akira", "Maple", date)
        val copy = original.copy(name = "Bonsuke")
        assertThat(copy.id).isEqualTo("1")
        assertThat(copy.name).isEqualTo("Bonsuke")
        assertThat(copy.kind).isEqualTo("Maple")
        assertThat(copy.purchaseDate).isEqualTo(date)
    }

    @Test
    fun bonsaiCopyWithLastMaintenanceDate() {
        val maintenance = LocalDate(2025, 1, 5)
        val original = Bonsai("1", "Akira", "Maple", date)
        val copy = original.copy(lastMaintenanceDate = maintenance)
        assertThat(copy.lastMaintenanceDate).isEqualTo(maintenance)
    }
}
