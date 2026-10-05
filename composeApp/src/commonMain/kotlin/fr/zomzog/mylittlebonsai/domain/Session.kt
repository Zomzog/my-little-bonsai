package fr.zomzog.mylittlebonsai.domain

import kotlinx.datetime.LocalDate

data class Session(
    val id: String,
    val date: LocalDate,
    val createdAt: VaultTimestamp,
    val actions: List<Action>,
    val body: String = "",
)
