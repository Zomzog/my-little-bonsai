package fr.zomzog.mylittlebonsai.data

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import fr.zomzog.mylittlebonsai.domain.Bonsai
import kotlinx.datetime.LocalDate
import kotlin.test.Test

class BonsaiSerializationTest {

    private val bonsai = Bonsai(
        id = "id-a",
        name = "Akira",
        kind = "Maple",
        purchaseDate = LocalDate(2024, 3, 10),
    )
    private val bonsaiWithMaintenance = Bonsai(
        id = "id-b",
        name = "Bonsuke",
        kind = "Pine",
        purchaseDate = LocalDate(2023, 7, 1),
        lastMaintenanceDate = LocalDate(2024, 1, 15),
    )

    @Test
    fun encodesEmptyListAsEmptyJsonArray() {
        assertThat(BonsaiSerialization.encode(emptyList())).isEqualTo("[]")
    }

    @Test
    fun decodesEmptyJsonArrayToEmptyList() {
        assertThat(BonsaiSerialization.decode("[]")).isEmpty()
    }

    @Test
    fun roundTripPreservesAllFields() {
        val decoded = BonsaiSerialization.decode(
            BonsaiSerialization.encode(listOf(bonsaiWithMaintenance)),
        )
        assertThat(decoded).containsExactly(bonsaiWithMaintenance)
    }

    @Test
    fun roundTripKeepsNullMaintenanceDate() {
        val decoded = BonsaiSerialization.decode(BonsaiSerialization.encode(listOf(bonsai)))
        assertThat(decoded.first().lastMaintenanceDate).isNull()
    }

    @Test
    fun roundTripPreservesOrder() {
        val encoded = BonsaiSerialization.encode(listOf(bonsai, bonsaiWithMaintenance))
        assertThat(BonsaiSerialization.decode(encoded))
            .containsExactly(bonsai, bonsaiWithMaintenance)
    }

    @Test
    fun encodesDatesAsIsoStrings() {
        assertThat(BonsaiSerialization.encode(listOf(bonsai))).contains("\"2024-03-10\"")
    }

    @Test
    fun roundTripSurvivesCharactersNeedingJsonEscaping() {
        val quoted = bonsai.copy(name = "\"Aki\\ra\"", kind = "Maple\nAcer\ttrue")
        val decoded = BonsaiSerialization.decode(BonsaiSerialization.encode(listOf(quoted)))
        assertThat(decoded).containsExactly(quoted)
    }

    @Test
    fun decodeReturnsEmptyListForMalformedJson() {
        assertThat(BonsaiSerialization.decode("{not json")).isEmpty()
    }

    @Test
    fun decodeReturnsEmptyListWhenRequiredFieldIsMissing() {
        assertThat(BonsaiSerialization.decode("""[{"id":"a","name":"Akira"}]""")).isEmpty()
    }

    @Test
    fun decodeReturnsEmptyListForUnparsableDate() {
        val raw = """[{"id":"a","name":"Akira","kind":"Maple","purchaseDate":"not-a-date"}]"""
        assertThat(BonsaiSerialization.decode(raw)).isEmpty()
    }

    @Test
    fun decodeIgnoresUnknownFields() {
        val raw =
            """[{"id":"id-a","name":"Akira","kind":"Maple","purchaseDate":"2024-03-10","x":1}]"""
        assertThat(BonsaiSerialization.decode(raw)).containsExactly(bonsai)
    }
}
