package fr.zomzog.mylittlebonsai.domain

import kotlinx.datetime.LocalDate

enum class BonsaiStatus { ACTIVE, ARCHIVED }

data class Bonsai(
    val id: String,
    val name: String,
    val addedOn: LocalDate,
    val species: String? = null,
    val style: String? = null,
    val status: BonsaiStatus = BonsaiStatus.ACTIVE,
    val archived: ArchivedInfo? = null,
    val age: BonsaiAge? = null,
    val substrate: Substrate? = null,
    val pot: String? = null,
    val cover: String? = null,
    val description: String = "",
)
