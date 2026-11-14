package fr.zomzog.mylittlebonsai.data.vault

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFailure
import assertk.assertions.isNull
import fr.zomzog.mylittlebonsai.domain.ArchivedInfo
import fr.zomzog.mylittlebonsai.domain.ArchivedReason
import fr.zomzog.mylittlebonsai.domain.Bonsai
import fr.zomzog.mylittlebonsai.domain.BonsaiAge
import fr.zomzog.mylittlebonsai.domain.BonsaiStatus
import fr.zomzog.mylittlebonsai.domain.Substrate
import fr.zomzog.mylittlebonsai.domain.SubstrateComponent
import kotlinx.datetime.LocalDate
import kotlin.test.Test

class BonsaiVaultCodecTest {

    private val minimal = Bonsai(
        id = "3f6c1e0a-9b7d-4e2a-8c51-0d4e7a2b9f10",
        name = "Akira",
        addedOn = LocalDate(2024, 3, 2),
    )

    private val full = Bonsai(
        id = "3f6c1e0a-9b7d-4e2a-8c51-0d4e7a2b9f10",
        name = "Érable du jardin",
        species = "acer-palmatum",
        style = "moyogi",
        status = BonsaiStatus.ACTIVE,
        addedOn = LocalDate(2024, 3, 2),
        age = BonsaiAge.Birthday("2016-04"),
        substrate = Substrate.NamedMix("6a3f9e1d-2c4b-4d8a-b7e5-0f1a2b3c4d5e"),
        pot = "5b2d9c77-1a4e-4f0b-b3d8-6e9f0a1c2d34",
        cover = "attachments/20260412-103512-a1b2c3.jpg",
        description = "Bought at the Saint-Jean nursery.",
    )

    @Test
    fun encodesTheSpecSampleShape() {
        val encoded = BonsaiVaultCodec.encode(full)
        assertThat(encoded).isEqualTo(
            "---\n" +
                "id: 3f6c1e0a-9b7d-4e2a-8c51-0d4e7a2b9f10\n" +
                "name: Érable du jardin\n" +
                "species: acer-palmatum\n" +
                "style: moyogi\n" +
                "status: active\n" +
                "addedOn: 2024-03-02\n" +
                "age:\n" +
                "  birthday: 2016-04\n" +
                "substrate:\n" +
                "  mix: 6a3f9e1d-2c4b-4d8a-b7e5-0f1a2b3c4d5e\n" +
                "pot: 5b2d9c77-1a4e-4f0b-b3d8-6e9f0a1c2d34\n" +
                "cover: attachments/20260412-103512-a1b2c3.jpg\n" +
                "---\n" +
                "\n" +
                "Bought at the Saint-Jean nursery.\n",
        )
    }

    @Test
    fun decodesTheSpecSampleShape() {
        val decoded = BonsaiVaultCodec.decode(BonsaiVaultCodec.encode(full))
        assertThat(decoded).isEqualTo(full)
    }

    @Test
    fun roundTripsMinimalBonsai() {
        assertThat(BonsaiVaultCodec.decode(BonsaiVaultCodec.encode(minimal))).isEqualTo(minimal)
    }

    @Test
    fun minimalEncodeOmitsAbsentOptionalFields() {
        val encoded = BonsaiVaultCodec.encode(minimal)
        assertThat(encoded.contains("species:")).isEqualTo(false)
        assertThat(encoded.contains("cover:")).isEqualTo(false)
        assertThat(encoded.contains("archived:")).isEqualTo(false)
    }

    @Test
    fun roundTripsArchivedBonsai() {
        val archived = minimal.copy(
            status = BonsaiStatus.ARCHIVED,
            archived = ArchivedInfo(ArchivedReason.DEAD, LocalDate(2026, 1, 1), note = "Root rot"),
        )
        assertThat(BonsaiVaultCodec.decode(BonsaiVaultCodec.encode(archived))).isEqualTo(archived)
    }

    @Test
    fun roundTripsArchivedWithoutNote() {
        val archived = minimal.copy(
            status = BonsaiStatus.ARCHIVED,
            archived = ArchivedInfo(ArchivedReason.SOLD, LocalDate(2026, 1, 1)),
        )
        val decoded = BonsaiVaultCodec.decode(BonsaiVaultCodec.encode(archived))
        assertThat(decoded.archived?.note).isNull()
    }

    @Test
    fun roundTripsYearsAge() {
        val bonsai = minimal.copy(age = BonsaiAge.Years(5))
        assertThat(BonsaiVaultCodec.decode(BonsaiVaultCodec.encode(bonsai))).isEqualTo(bonsai)
    }

    @Test
    fun roundTripsInlineMixSubstrate() {
        val bonsai = minimal.copy(
            substrate = Substrate.InlineMix(
                listOf(SubstrateComponent("akadama", 60), SubstrateComponent("pumice", 40)),
            ),
        )
        assertThat(BonsaiVaultCodec.decode(BonsaiVaultCodec.encode(bonsai))).isEqualTo(bonsai)
    }

    @Test
    fun roundTripsNameNeedingQuoting() {
        val bonsai = minimal.copy(name = "Bonsai: Special Edition")
        assertThat(BonsaiVaultCodec.decode(BonsaiVaultCodec.encode(bonsai))).isEqualTo(bonsai)
    }

    @Test
    fun decodeThrowsWhenIdIsMissing() {
        val text = "---\nname: Akira\nstatus: active\naddedOn: 2024-03-02\n---\n"
        assertThat(runCatching { BonsaiVaultCodec.decode(text) }).isFailure()
    }

    @Test
    fun decodeThrowsWhenStatusIsUnknown() {
        val text = "---\nid: 1\nname: Akira\nstatus: dormant\naddedOn: 2024-03-02\n---\n"
        assertThat(runCatching { BonsaiVaultCodec.decode(text) }).isFailure()
    }

    @Test
    fun decodeThrowsWhenSubstrateHasNeitherMixNorComponents() {
        val text = "---\nid: 1\nname: Akira\nstatus: active\naddedOn: 2024-03-02\nsubstrate:\n  other: x\n---\n"
        assertThat(runCatching { BonsaiVaultCodec.decode(text) }).isFailure()
    }

    @Test
    fun decodeThrowsWhenAgeHasNeitherBirthdayNorYears() {
        val text = "---\nid: 1\nname: Akira\nstatus: active\naddedOn: 2024-03-02\nage:\n  other: x\n---\n"
        assertThat(runCatching { BonsaiVaultCodec.decode(text) }).isFailure()
    }

    @Test
    fun decodeThrowsWhenArchivedReasonIsUnknown() {
        val text =
            "---\nid: 1\nname: Akira\nstatus: archived\naddedOn: 2024-03-02\n" +
                "archived:\n  reason: exploded\n  date: 2026-01-01\n---\n"
        assertThat(runCatching { BonsaiVaultCodec.decode(text) }).isFailure()
    }

    @Test
    fun decodeThrowsWhenNameIsMissing() {
        val text = "---\nid: 1\nstatus: active\naddedOn: 2024-03-02\n---\n"
        assertThat(runCatching { BonsaiVaultCodec.decode(text) }).isFailure()
    }

    @Test
    fun decodeThrowsWhenStatusIsMissing() {
        val text = "---\nid: 1\nname: Akira\naddedOn: 2024-03-02\n---\n"
        assertThat(runCatching { BonsaiVaultCodec.decode(text) }).isFailure()
    }

    @Test
    fun decodeThrowsWhenAddedOnIsMissing() {
        val text = "---\nid: 1\nname: Akira\nstatus: active\n---\n"
        assertThat(runCatching { BonsaiVaultCodec.decode(text) }).isFailure()
    }

    @Test
    fun decodeThrowsWhenArchivedIsMissingReason() {
        val text =
            "---\nid: 1\nname: Akira\nstatus: archived\naddedOn: 2024-03-02\n" +
                "archived:\n  date: 2026-01-01\n---\n"
        assertThat(runCatching { BonsaiVaultCodec.decode(text) }).isFailure()
    }

    @Test
    fun decodeThrowsWhenArchivedIsMissingDate() {
        val text =
            "---\nid: 1\nname: Akira\nstatus: archived\naddedOn: 2024-03-02\n" +
                "archived:\n  reason: dead\n---\n"
        assertThat(runCatching { BonsaiVaultCodec.decode(text) }).isFailure()
    }

    @Test
    fun decodeThrowsWhenSubstrateComponentIsNotAMapping() {
        val text =
            "---\nid: 1\nname: Akira\nstatus: active\naddedOn: 2024-03-02\n" +
                "substrate:\n  components:\n    - akadama\n---\n"
        assertThat(runCatching { BonsaiVaultCodec.decode(text) }).isFailure()
    }

    @Test
    fun decodeThrowsWhenSubstrateComponentIsMissingSoil() {
        val text =
            "---\nid: 1\nname: Akira\nstatus: active\naddedOn: 2024-03-02\n" +
                "substrate:\n  components:\n    - percent: 100\n---\n"
        assertThat(runCatching { BonsaiVaultCodec.decode(text) }).isFailure()
    }

    @Test
    fun decodeThrowsWhenSubstrateComponentIsMissingPercent() {
        val text =
            "---\nid: 1\nname: Akira\nstatus: active\naddedOn: 2024-03-02\n" +
                "substrate:\n  components:\n    - soil: akadama\n---\n"
        assertThat(runCatching { BonsaiVaultCodec.decode(text) }).isFailure()
    }
}
