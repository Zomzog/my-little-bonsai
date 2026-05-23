package fr.zomzog.mylittlebonsai.domain

import kotlinx.datetime.LocalDate

data class Bonsai(
    val id: String,
    val name: String,
    val kind: String,
    val purchaseDate: LocalDate,
    val lastMaintenanceDate: LocalDate? = null,
)
