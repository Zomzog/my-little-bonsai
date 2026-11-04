package fr.zomzog.mylittlebonsai.domain

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlinx.datetime.LocalDateTime
import kotlin.test.Test

class VaultTimestampTest {

    @Test
    fun toStringRendersOffsetTimestamp() {
        val timestamp = VaultTimestamp(LocalDateTime(2026, 4, 12, 10, 35, 12), utcOffsetSeconds = 7200)
        assertThat(timestamp.toString()).isEqualTo("2026-04-12T10:35:12+02:00")
    }

    @Test
    fun toStringRendersNegativeOffset() {
        val timestamp = VaultTimestamp(LocalDateTime(2026, 4, 12, 10, 35, 12), utcOffsetSeconds = -18000)
        assertThat(timestamp.toString()).isEqualTo("2026-04-12T10:35:12-05:00")
    }

    @Test
    fun toStringRendersUtcAsZ() {
        val timestamp = VaultTimestamp(LocalDateTime(2026, 4, 12, 10, 35, 12), utcOffsetSeconds = 0)
        assertThat(timestamp.toString()).isEqualTo("2026-04-12T10:35:12Z")
    }

    @Test
    fun toStringPadsSingleDigitFields() {
        val timestamp = VaultTimestamp(LocalDateTime(2026, 1, 2, 3, 4, 5), utcOffsetSeconds = 1800)
        assertThat(timestamp.toString()).isEqualTo("2026-01-02T03:04:05+00:30")
    }

    @Test
    fun toFileNamePrefixUsesLocalDateAndMinute() {
        val timestamp = VaultTimestamp(LocalDateTime(2026, 4, 12, 10, 35, 12), utcOffsetSeconds = 7200)
        assertThat(timestamp.toFileNamePrefix()).isEqualTo("2026-04-12-1035")
    }

    @Test
    fun parseRoundTripsWithPositiveOffset() {
        val text = "2026-04-12T10:35:12+02:00"
        assertThat(VaultTimestamp.parse(text).toString()).isEqualTo(text)
    }

    @Test
    fun parseRoundTripsWithNegativeOffset() {
        val text = "2026-04-12T10:35:12-05:30"
        assertThat(VaultTimestamp.parse(text).toString()).isEqualTo(text)
    }

    @Test
    fun parseRoundTripsWithZ() {
        val text = "2026-04-12T10:35:12Z"
        assertThat(VaultTimestamp.parse(text).toString()).isEqualTo(text)
    }

    @Test
    fun parseReadsLocalDateTimeFields() {
        val parsed = VaultTimestamp.parse("2026-04-12T10:35:12+02:00")
        assertThat(parsed.dateTime).isEqualTo(LocalDateTime(2026, 4, 12, 10, 35, 12))
    }

    @Test
    fun equalityHashCodeAndCopy() {
        val a = VaultTimestamp(LocalDateTime(2026, 4, 12, 10, 35, 12), 7200)
        val b = VaultTimestamp(LocalDateTime(2026, 4, 12, 10, 35, 12), 7200)
        assertThat(a).isEqualTo(b)
        assertThat(a.hashCode()).isEqualTo(b.hashCode())
        assertThat(a.copy(utcOffsetSeconds = 0).utcOffsetSeconds).isEqualTo(0)
    }
}
