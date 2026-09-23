package fr.zomzog.mylittlebonsai.domain.species

/**
 * A species catalogue entry. [id] is the stable kebab-case key: the Latin name
 * for defaults (e.g. "acer-palmatum"), a generated id for user-added entries.
 */
data class Species(
    val id: String,
    val latinName: String?,
    val commonNameEn: String,
    val commonNameFr: String,
)

enum class UiLanguage { EN, FR }

fun Species.label(language: UiLanguage): String = when (language) {
    UiLanguage.EN -> commonNameEn
    UiLanguage.FR -> commonNameFr
}
