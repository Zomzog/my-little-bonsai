package fr.zomzog.mylittlebonsai.data.vault

/**
 * Decomposes Unicode letters to their base form and drops the resulting combining
 * marks, so accents fold away (`é` -> `e`). Ligatures that do not decompose (`æ`,
 * `œ`, `ß`, `ø`...) are handled separately by [foldLigatures] before this runs.
 */
expect fun stripDiacritics(text: String): String

private val reservedWindowsNames = setOf(
    "con", "prn", "aux", "nul",
    "com1", "com2", "com3", "com4", "com5", "com6", "com7", "com8", "com9",
    "lpt1", "lpt2", "lpt3", "lpt4", "lpt5", "lpt6", "lpt7", "lpt8", "lpt9",
)

private val ligatures = mapOf(
    'æ' to "ae", 'Æ' to "ae",
    'œ' to "oe", 'Œ' to "oe",
    'ß' to "ss",
    'ø' to "o", 'Ø' to "o",
)

private val nonAlphaNumeric = Regex("[^a-z0-9]+")

private fun foldLigatures(text: String): String = buildString {
    for (char in text) append(ligatures[char] ?: char)
}

/** The bonsai folder name derived from its `name`, per vault-format.md's slug algorithm. */
fun slugify(name: String, existingSlugs: Set<String>): String {
    val stripped = stripDiacritics(foldLigatures(name))
    val lowered = stripped.lowercase()
    val dashed = nonAlphaNumeric.replace(lowered, "-")
    val trimmed = dashed.trim('-').take(60).trim('-')
    val base = trimmed.ifEmpty { "bonsai" }

    var candidate = base
    var suffix = 2
    while (candidate in existingSlugs || candidate in reservedWindowsNames) {
        candidate = "$base-$suffix"
        suffix++
    }
    return candidate
}
