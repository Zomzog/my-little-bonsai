package fr.zomzog.mylittlebonsai.domain

import kotlinx.datetime.LocalDateTime

/**
 * An ISO-8601 local date-time with a fixed UTC offset, to the second, as used for a
 * session's `createdAt` (vault-format.md). Kept as its own local time plus offset,
 * rather than converted to an `Instant`, because the vault format stores the offset
 * the note was created under, not a UTC instant.
 */
data class VaultTimestamp(
    val dateTime: LocalDateTime,
    val utcOffsetSeconds: Int,
) {
    /** `YYYY-MM-DDTHH:mm:ss+HH:MM` (or `Z` for UTC), e.g. `2026-04-12T10:35:12+02:00`. */
    override fun toString(): String {
        val date = "${pad4(dateTime.year)}-${pad2(dateTime.monthNumber)}-${pad2(dateTime.dayOfMonth)}"
        val time = "${pad2(dateTime.hour)}:${pad2(dateTime.minute)}:${pad2(dateTime.second)}"
        return "${date}T$time${offsetSuffix(utcOffsetSeconds)}"
    }

    /** The file-name-safe local date and minute, e.g. `2026-04-12-1035`. */
    fun toFileNamePrefix(): String =
        "${pad4(dateTime.year)}-${pad2(dateTime.monthNumber)}-${pad2(dateTime.dayOfMonth)}" +
            "-${pad2(dateTime.hour)}${pad2(dateTime.minute)}"

    companion object {
        fun parse(text: String): VaultTimestamp {
            val (localPart, offsetSeconds) = splitOffset(text)
            return VaultTimestamp(LocalDateTime.parse(localPart), offsetSeconds)
        }

        private fun splitOffset(text: String): Pair<String, Int> {
            if (text.endsWith("Z")) return text.dropLast(1) to 0
            // The offset sign only ever appears after the 'T' separator (the date
            // portion before it also contains '-' characters).
            val timeStart = text.indexOf('T')
            val signIndex = text.indexOfAny(charArrayOf('+', '-'), startIndex = timeStart)
                .takeIf { it >= 0 }
                ?: error("Timestamp '$text' has no UTC offset")
            val localPart = text.substring(0, signIndex)
            val offsetPart = text.substring(signIndex)
            return localPart to parseOffsetSeconds(offsetPart)
        }

        private fun parseOffsetSeconds(offset: String): Int {
            val sign = if (offset.startsWith("-")) -1 else 1
            val parts = offset.substring(1).split(":")
            val hours = parts[0].toInt()
            val minutes = parts.getOrNull(1)?.toInt() ?: 0
            return sign * (hours * 3600 + minutes * 60)
        }

        private fun offsetSuffix(totalSeconds: Int): String {
            if (totalSeconds == 0) return "Z"
            val sign = if (totalSeconds < 0) "-" else "+"
            val abs = kotlin.math.abs(totalSeconds)
            val hours = abs / 3600
            val minutes = (abs % 3600) / 60
            return "$sign${pad2(hours)}:${pad2(minutes)}"
        }

        private fun pad2(value: Int): String = value.toString().padStart(2, '0')
        private fun pad4(value: Int): String = value.toString().padStart(4, '0')
    }
}
