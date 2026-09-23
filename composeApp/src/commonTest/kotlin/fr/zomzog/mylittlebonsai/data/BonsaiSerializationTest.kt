package fr.zomzog.mylittlebonsai.data

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
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

class BonsaiSerializationTest {

    private val bonsai = Bonsai(id = "id-a", name = "Akira", addedOn = LocalDate(2024, 3, 10))

    private val fullBonsai = Bonsai(
        id = "id-b",
        name = "Bonsuke",
        addedOn = LocalDate(2023, 7, 1),
        species = "acer-palmatum",
        style = "moyogi",
        status = BonsaiStatus.ARCHIVED,
        archived = ArchivedInfo(ArchivedReason.SOLD, LocalDate(2024, 1, 15), note = "Sold at market"),
        age = BonsaiAge.Years(5),
        substrate = Substrate.InlineMix(listOf(SubstrateComponent("akadama", 100))),
        pot = "pot-1",
        cover = "attachments/cover.jpg",
        description = "A fine pine.",
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
        val decoded = BonsaiSerialization.decode(BonsaiSerialization.encode(listOf(fullBonsai)))
        assertThat(decoded).containsExactly(fullBonsai)
    }

    @Test
    fun roundTripKeepsMinimalBonsaiFieldsNull() {
        val decoded = BonsaiSerialization.decode(BonsaiSerialization.encode(listOf(bonsai)))
        assertThat(decoded.first()).isEqualTo(bonsai)
    }

    @Test
    fun roundTripWithNamedMixSubstrate() {
        val withMix = bonsai.copy(substrate = Substrate.NamedMix("mix-1"))
        val decoded = BonsaiSerialization.decode(BonsaiSerialization.encode(listOf(withMix)))
        assertThat(decoded).containsExactly(withMix)
    }

    @Test
    fun roundTripWithBirthdayAge() {
        val withAge = bonsai.copy(age = BonsaiAge.Birthday("2016-04"))
        val decoded = BonsaiSerialization.decode(BonsaiSerialization.encode(listOf(withAge)))
        assertThat(decoded).containsExactly(withAge)
    }

    @Test
    fun roundTripPreservesOrder() {
        val encoded = BonsaiSerialization.encode(listOf(bonsai, fullBonsai))
        assertThat(BonsaiSerialization.decode(encoded)).containsExactly(bonsai, fullBonsai)
    }

    @Test
    fun encodesDatesAsIsoStrings() {
        assertThat(BonsaiSerialization.encode(listOf(bonsai))).contains("\"2024-03-10\"")
    }

    @Test
    fun roundTripSurvivesCharactersNeedingJsonEscaping() {
        val quoted = bonsai.copy(name = "\"Aki\\ra\"", description = "Line\nbreak\ttab")
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
        val raw = """[{"id":"a","name":"Akira","addedOn":"not-a-date"}]"""
        assertThat(BonsaiSerialization.decode(raw)).isEmpty()
    }

    @Test
    fun decodeIgnoresUnknownFields() {
        val raw = """[{"id":"id-a","name":"Akira","addedOn":"2024-03-10","x":1}]"""
        assertThat(BonsaiSerialization.decode(raw)).containsExactly(bonsai)
    }

    @Test
    fun decodedMinimalBonsaiHasNullAge() {
        val decoded = BonsaiSerialization.decode(BonsaiSerialization.encode(listOf(bonsai)))
        assertThat(decoded.first().age).isNull()
    }
}
