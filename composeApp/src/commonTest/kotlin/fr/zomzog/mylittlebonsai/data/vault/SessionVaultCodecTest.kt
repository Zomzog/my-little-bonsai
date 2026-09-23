package fr.zomzog.mylittlebonsai.data.vault

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFailure
import fr.zomzog.mylittlebonsai.domain.Action
import fr.zomzog.mylittlebonsai.domain.DoseUnit
import fr.zomzog.mylittlebonsai.domain.FertilizerDose
import fr.zomzog.mylittlebonsai.domain.Session
import fr.zomzog.mylittlebonsai.domain.Substrate
import fr.zomzog.mylittlebonsai.domain.VaultTimestamp
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlin.test.Test

class SessionVaultCodecTest {

    private val createdAt = VaultTimestamp(LocalDateTime(2026, 4, 12, 10, 35, 12), utcOffsetSeconds = 7200)

    private val full = Session(
        id = "0c7a4f3e-5d21-4b8a-9e6f-2a1b3c4d5e6f",
        date = LocalDate(2026, 4, 12),
        createdAt = createdAt,
        actions = listOf(
            Action.Repotting(
                substrate = Substrate.NamedMix("6a3f9e1d-2c4b-4d8a-b7e5-0f1a2b3c4d5e"),
                pot = "5b2d9c77-1a4e-4f0b-b3d8-6e9f0a1c2d34",
            ),
            Action.Measuring(height = 42.5, width = 38.0, nebari = 21.0, weight = 1850.0),
            Action.Other(actionId = "pruning", note = "Structural pruning of the apex"),
        ),
        body = "Roots were circling, cut back about a third.",
    )

    @Test
    fun encodesTheSpecSampleShape() {
        val encoded = SessionVaultCodec.encode(full)
        assertThat(encoded).isEqualTo(
            "---\n" +
                "id: 0c7a4f3e-5d21-4b8a-9e6f-2a1b3c4d5e6f\n" +
                "date: 2026-04-12\n" +
                "createdAt: 2026-04-12T10:35:12+02:00\n" +
                "actions:\n" +
                "  - action: repotting\n" +
                "    substrate:\n" +
                "      mix: 6a3f9e1d-2c4b-4d8a-b7e5-0f1a2b3c4d5e\n" +
                "    pot: 5b2d9c77-1a4e-4f0b-b3d8-6e9f0a1c2d34\n" +
                "  - action: measuring\n" +
                "    height: 42.5\n" +
                "    width: 38\n" +
                "    nebari: 21\n" +
                "    weight: 1850\n" +
                "  - action: pruning\n" +
                "    note: Structural pruning of the apex\n" +
                "---\n" +
                "\n" +
                "Roots were circling, cut back about a third.\n",
        )
    }

    @Test
    fun roundTripsTheSpecSampleShape() {
        assertThat(SessionVaultCodec.decode(SessionVaultCodec.encode(full))).isEqualTo(full)
    }

    @Test
    fun roundTripsInlineSubstrateInRepottingAction() {
        val session = full.copy(
            actions = listOf(
                Action.Repotting(
                    substrate = Substrate.InlineMix(
                        listOf(
                            fr.zomzog.mylittlebonsai.domain.SubstrateComponent("akadama", 60),
                            fr.zomzog.mylittlebonsai.domain.SubstrateComponent("pumice", 40),
                        ),
                    ),
                ),
            ),
        )
        assertThat(SessionVaultCodec.decode(SessionVaultCodec.encode(session))).isEqualTo(session)
    }

    @Test
    fun roundTripsFertilizingAction() {
        val session = full.copy(
            actions = listOf(
                Action.Fertilizing(
                    fertilizer = "fish-emulsion",
                    dose = FertilizerDose(amount = 5.0, unit = DoseUnit.ML_PER_L),
                ),
            ),
        )
        assertThat(SessionVaultCodec.decode(SessionVaultCodec.encode(session))).isEqualTo(session)
    }

    @Test
    fun roundTripsFertilizingActionWithoutDose() {
        val session = full.copy(actions = listOf(Action.Fertilizing(fertilizer = "fish-emulsion")))
        assertThat(SessionVaultCodec.decode(SessionVaultCodec.encode(session))).isEqualTo(session)
    }

    @Test
    fun roundTripsAllDoseUnits() {
        for (unit in DoseUnit.entries) {
            val session = full.copy(
                actions = listOf(
                    Action.Fertilizing(fertilizer = "x", dose = FertilizerDose(1.0, unit)),
                ),
            )
            assertThat(SessionVaultCodec.decode(SessionVaultCodec.encode(session))).isEqualTo(session)
        }
    }

    @Test
    fun roundTripsTreatmentAction() {
        val session = full.copy(actions = listOf(Action.Treatment(treatment = "fungicide", target = "aphids")))
        assertThat(SessionVaultCodec.decode(SessionVaultCodec.encode(session))).isEqualTo(session)
    }

    @Test
    fun roundTripsActionWithNote() {
        val session = full.copy(
            actions = listOf(Action.Measuring(height = 10.0, note = "First measurement")),
        )
        assertThat(SessionVaultCodec.decode(SessionVaultCodec.encode(session))).isEqualTo(session)
    }

    @Test
    fun roundTripsEmptyBody() {
        val session = full.copy(body = "")
        assertThat(SessionVaultCodec.decode(SessionVaultCodec.encode(session))).isEqualTo(session)
    }

    @Test
    fun decodeThrowsWhenIdIsMissing() {
        val text = "---\ndate: 2026-04-12\ncreatedAt: 2026-04-12T10:35:12Z\nactions:\n  - action: pruning\n---\n"
        assertThat(runCatching { SessionVaultCodec.decode(text) }).isFailure()
    }

    @Test
    fun decodeThrowsWhenActionsIsMissing() {
        val text = "---\nid: 1\ndate: 2026-04-12\ncreatedAt: 2026-04-12T10:35:12Z\n---\n"
        assertThat(runCatching { SessionVaultCodec.decode(text) }).isFailure()
    }

    @Test
    fun decodeThrowsWhenFertilizingActionIsMissingFertilizer() {
        val text =
            "---\nid: 1\ndate: 2026-04-12\ncreatedAt: 2026-04-12T10:35:12Z\n" +
                "actions:\n  - action: fertilizing\n---\n"
        assertThat(runCatching { SessionVaultCodec.decode(text) }).isFailure()
    }

    @Test
    fun decodeThrowsWhenTreatmentActionIsMissingTreatment() {
        val text =
            "---\nid: 1\ndate: 2026-04-12\ncreatedAt: 2026-04-12T10:35:12Z\n" +
                "actions:\n  - action: treatment\n---\n"
        assertThat(runCatching { SessionVaultCodec.decode(text) }).isFailure()
    }

    @Test
    fun decodeThrowsWhenDoseUnitIsUnknown() {
        val text =
            "---\nid: 1\ndate: 2026-04-12\ncreatedAt: 2026-04-12T10:35:12Z\n" +
                "actions:\n  - action: fertilizing\n    fertilizer: x\n    dose:\n      amount: 1\n      unit: oz\n---\n"
        assertThat(runCatching { SessionVaultCodec.decode(text) }).isFailure()
    }

    @Test
    fun decodeThrowsWhenAnActionIsNotAMapping() {
        val text = "---\nid: 1\ndate: 2026-04-12\ncreatedAt: 2026-04-12T10:35:12Z\nactions:\n  - just text\n---\n"
        assertThat(runCatching { SessionVaultCodec.decode(text) }).isFailure()
    }

    @Test
    fun decodesUnknownActionAsOtherWithItsId() {
        val text =
            "---\nid: 1\ndate: 2026-04-12\ncreatedAt: 2026-04-12T10:35:12Z\n" +
                "actions:\n  - action: watering\n---\n"
        val session = SessionVaultCodec.decode(text)
        assertThat(session.actions.single()).isEqualTo(Action.Other(actionId = "watering", note = null))
    }
}
