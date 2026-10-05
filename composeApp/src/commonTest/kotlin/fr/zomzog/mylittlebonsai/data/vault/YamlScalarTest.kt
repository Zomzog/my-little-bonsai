package fr.zomzog.mylittlebonsai.data.vault

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class YamlScalarTest {

    private fun roundTrip(value: String): String =
        parseYaml("note: ${yamlScalar(value)}").string("note")!!

    @Test
    fun plainTextIsNotQuoted() {
        assertThat(yamlScalar("acer-palmatum")).isEqualTo("acer-palmatum")
    }

    @Test
    fun aTrailingHashPrecededBySpaceIsQuoted() {
        assertThat(yamlScalar("Fix issue #42")).isEqualTo("\"Fix issue #42\"")
    }

    @Test
    fun aLeadingPlainScalarIndicatorIsQuoted() {
        assertThat(yamlScalar("[bracketed]")).isEqualTo("\"[bracketed]\"")
        assertThat(yamlScalar("@mention")).isEqualTo("\"@mention\"")
    }

    @Test
    fun roundTripsANewlineInsideTheValue() {
        assertThat(roundTrip("Line one\nLine two")).isEqualTo("Line one\nLine two")
    }

    @Test
    fun roundTripsACarriageReturnInsideTheValue() {
        assertThat(roundTrip("Line one\r\nLine two")).isEqualTo("Line one\r\nLine two")
    }

    @Test
    fun roundTripsEscapedBackslashesNextToEscapedQuotes() {
        // Forces quoting (via ": ") and interleaves \\ and \" so a decoder that replaces
        // one escape sequence at a time, independently of the other, mis-decodes it.
        assertThat(roundTrip("Note: \\\"quoted\\\"")).isEqualTo("Note: \\\"quoted\\\"")
    }

    @Test
    fun roundTripsATrailingBackslashInAValueThatNeedsQuoting() {
        assertThat(roundTrip(" trailing\\")).isEqualTo(" trailing\\")
    }

    @Test
    fun roundTripsAValueContainingASpaceHash() {
        assertThat(roundTrip("Fix issue #42")).isEqualTo("Fix issue #42")
    }
}
