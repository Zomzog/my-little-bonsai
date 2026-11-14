package fr.zomzog.mylittlebonsai.domain

import kotlinx.datetime.LocalDate

enum class ArchivedReason { DEAD, SOLD, GIFTED, LOST, OTHER }

data class ArchivedInfo(
    val reason: ArchivedReason,
    val date: LocalDate,
    val note: String? = null,
)
