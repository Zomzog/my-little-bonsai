package fr.zomzog.mylittlebonsai.domain

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEqualTo
import assertk.assertions.isTrue
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlin.test.Test

/**
 * Sanity checks for the generated `equals`/`hashCode`/`toString`/`copy` of the small
 * vault domain types, matching the coverage the pre-existing [Bonsai] tests had —
 * [BonsaiVaultCodecTest][fr.zomzog.mylittlebonsai.data.vault.BonsaiVaultCodecTest] and
 * friends exercise `equals` through round trips, but not the other generated members.
 */
class DomainDataClassesTest {

    @Test
    fun archivedInfoEqualityToStringAndCopy() {
        val a = ArchivedInfo(ArchivedReason.DEAD, LocalDate(2026, 1, 1), note = "Root rot")
        val b = ArchivedInfo(ArchivedReason.DEAD, LocalDate(2026, 1, 1), note = "Root rot")
        assertThat(a).isEqualTo(b)
        assertThat(a.hashCode()).isEqualTo(b.hashCode())
        assertThat(a.toString().contains("DEAD")).isTrue()
        assertThat(a.copy(note = null).note).isEqualTo(null)
        assertThat(a).isNotEqualTo(a.copy(reason = ArchivedReason.SOLD))
    }

    @Test
    fun archivedReasonEntriesAreDistinct() {
        assertThat(ArchivedReason.entries.toSet().size).isEqualTo(ArchivedReason.entries.size)
    }

    @Test
    fun bonsaiAgeBirthdayEqualityToStringAndCopy() {
        val a = BonsaiAge.Birthday("2016-04")
        val b = BonsaiAge.Birthday("2016-04")
        assertThat(a).isEqualTo(b)
        assertThat(a.hashCode()).isEqualTo(b.hashCode())
        assertThat(a.toString().contains("2016-04")).isTrue()
        assertThat(a.copy(value = "2020-01")).isNotEqualTo(a)
    }

    @Test
    fun bonsaiAgeYearsEqualityToStringAndCopy() {
        val a = BonsaiAge.Years(5)
        val b = BonsaiAge.Years(5)
        assertThat(a).isEqualTo(b)
        assertThat(a.hashCode()).isEqualTo(b.hashCode())
        assertThat(a.toString().contains("5")).isTrue()
        assertThat(a.copy(years = 6)).isNotEqualTo(a)
    }

    @Test
    fun substrateComponentEqualityToStringAndCopy() {
        val a = SubstrateComponent("akadama", 60)
        val b = SubstrateComponent("akadama", 60)
        assertThat(a).isEqualTo(b)
        assertThat(a.hashCode()).isEqualTo(b.hashCode())
        assertThat(a.toString().contains("akadama")).isTrue()
        assertThat(a.copy(percent = 40)).isNotEqualTo(a)
    }

    @Test
    fun substrateNamedMixEqualityToStringAndCopy() {
        val a = Substrate.NamedMix("mix-1")
        val b = Substrate.NamedMix("mix-1")
        assertThat(a).isEqualTo(b)
        assertThat(a.hashCode()).isEqualTo(b.hashCode())
        assertThat(a.toString().contains("mix-1")).isTrue()
        assertThat(a.copy(mixId = "mix-2")).isNotEqualTo(a)
    }

    @Test
    fun substrateInlineMixEqualityToStringAndCopy() {
        val components = listOf(SubstrateComponent("akadama", 100))
        val a = Substrate.InlineMix(components)
        val b = Substrate.InlineMix(components)
        assertThat(a).isEqualTo(b)
        assertThat(a.hashCode()).isEqualTo(b.hashCode())
        assertThat(a.toString().contains("akadama")).isTrue()
        assertThat(a.copy(components = emptyList())).isNotEqualTo(a)
    }

    @Test
    fun sessionEqualityToStringAndCopy() {
        val createdAt = VaultTimestamp(LocalDateTime(2026, 4, 12, 10, 35, 12), 7200)
        val actions = listOf(Action.Other("pruning"))
        val a = Session("s1", LocalDate(2026, 4, 12), createdAt, actions, body = "notes")
        val b = Session("s1", LocalDate(2026, 4, 12), createdAt, actions, body = "notes")
        assertThat(a).isEqualTo(b)
        assertThat(a.hashCode()).isEqualTo(b.hashCode())
        assertThat(a.toString().contains("s1")).isTrue()
        assertThat(a.copy(body = "other")).isNotEqualTo(a)
    }

    @Test
    fun fertilizerDoseEqualityToStringAndCopy() {
        val a = FertilizerDose(5.0, DoseUnit.ML_PER_L)
        val b = FertilizerDose(5.0, DoseUnit.ML_PER_L)
        assertThat(a).isEqualTo(b)
        assertThat(a.hashCode()).isEqualTo(b.hashCode())
        assertThat(a.toString().contains("5.0")).isTrue()
        assertThat(a.copy(amount = 10.0)).isNotEqualTo(a)
    }

    @Test
    fun doseUnitEntriesAreDistinct() {
        assertThat(DoseUnit.entries.toSet().size).isEqualTo(DoseUnit.entries.size)
    }

    @Test
    fun measuringActionEqualityToStringAndCopy() {
        val a = Action.Measuring(height = 42.5, width = 38.0, nebari = 21.0, weight = 1850.0, note = "n")
        val b = Action.Measuring(height = 42.5, width = 38.0, nebari = 21.0, weight = 1850.0, note = "n")
        assertThat(a).isEqualTo(b)
        assertThat(a.hashCode()).isEqualTo(b.hashCode())
        assertThat(a.toString().contains("42.5")).isTrue()
        assertThat(a.copy(height = null)).isNotEqualTo(a)
    }

    @Test
    fun repottingActionEqualityToStringAndCopy() {
        val a = Action.Repotting(Substrate.NamedMix("mix-1"), pot = "pot-1", note = "n")
        val b = Action.Repotting(Substrate.NamedMix("mix-1"), pot = "pot-1", note = "n")
        assertThat(a).isEqualTo(b)
        assertThat(a.hashCode()).isEqualTo(b.hashCode())
        assertThat(a.toString().contains("pot-1")).isTrue()
        assertThat(a.copy(pot = "pot-2")).isNotEqualTo(a)
    }

    @Test
    fun fertilizingActionEqualityToStringAndCopy() {
        val dose = FertilizerDose(5.0, DoseUnit.G)
        val a = Action.Fertilizing("fish-emulsion", dose, note = "n")
        val b = Action.Fertilizing("fish-emulsion", dose, note = "n")
        assertThat(a).isEqualTo(b)
        assertThat(a.hashCode()).isEqualTo(b.hashCode())
        assertThat(a.toString().contains("fish-emulsion")).isTrue()
        assertThat(a.copy(fertilizer = "other")).isNotEqualTo(a)
    }

    @Test
    fun treatmentActionEqualityToStringAndCopy() {
        val a = Action.Treatment("fungicide", target = "aphids", note = "n")
        val b = Action.Treatment("fungicide", target = "aphids", note = "n")
        assertThat(a).isEqualTo(b)
        assertThat(a.hashCode()).isEqualTo(b.hashCode())
        assertThat(a.toString().contains("fungicide")).isTrue()
        assertThat(a.copy(target = null)).isNotEqualTo(a)
    }

    @Test
    fun otherActionEqualityToStringAndCopy() {
        val a = Action.Other("watering", note = "n")
        val b = Action.Other("watering", note = "n")
        assertThat(a).isEqualTo(b)
        assertThat(a.hashCode()).isEqualTo(b.hashCode())
        assertThat(a.toString().contains("watering")).isTrue()
        assertThat(a.copy(actionId = "other")).isNotEqualTo(a)
    }

    @Test
    fun bonsaiFullEqualityToStringAndCopy() {
        val a = Bonsai(
            id = "id-a",
            name = "Akira",
            addedOn = LocalDate(2024, 3, 10),
            species = "acer-palmatum",
            style = "moyogi",
            status = BonsaiStatus.ARCHIVED,
            archived = ArchivedInfo(ArchivedReason.SOLD, LocalDate(2026, 1, 1)),
            age = BonsaiAge.Years(5),
            substrate = Substrate.NamedMix("mix-1"),
            pot = "pot-1",
            cover = "attachments/cover.jpg",
            description = "notes",
        )
        val b = a.copy()
        assertThat(a).isEqualTo(b)
        assertThat(a.hashCode()).isEqualTo(b.hashCode())
        assertThat(a.toString().contains("acer-palmatum")).isTrue()
        assertThat(a.copy(species = null)).isNotEqualTo(a)
    }

    @Test
    fun bonsaiStatusEntriesAreDistinct() {
        assertThat(BonsaiStatus.entries.toSet().size).isEqualTo(BonsaiStatus.entries.size)
    }
}
