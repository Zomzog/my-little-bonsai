package fr.zomzog.mylittlebonsai.domain.species

/**
 * Sorts species alphabetically by their label in [language], ignoring case and accents,
 * so "Érable" and "erable" compare equal up to the accent (e.g. #82: EN/FR picker order).
 */
fun List<Species>.sortedByLabel(language: UiLanguage): List<Species> =
    sortedBy { it.label(language).foldToAccentInsensitiveKey() }

private val DIACRITIC_FOLD: Map<Char, Char> = buildMap {
    put('à', 'a'); put('â', 'a'); put('ä', 'a'); put('á', 'a'); put('ã', 'a'); put('å', 'a')
    put('ç', 'c')
    put('é', 'e'); put('è', 'e'); put('ê', 'e'); put('ë', 'e')
    put('î', 'i'); put('ï', 'i'); put('ì', 'i'); put('í', 'i')
    put('ô', 'o'); put('ö', 'o'); put('ò', 'o'); put('ó', 'o'); put('õ', 'o')
    put('ù', 'u'); put('û', 'u'); put('ü', 'u'); put('ú', 'u')
    put('ÿ', 'y')
    put('ñ', 'n')
}

internal fun String.foldToAccentInsensitiveKey(): String =
    lowercase().map { DIACRITIC_FOLD[it] ?: it }.joinToString("")
