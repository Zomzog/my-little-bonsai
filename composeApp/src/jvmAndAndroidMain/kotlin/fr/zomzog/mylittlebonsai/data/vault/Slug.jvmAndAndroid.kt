package fr.zomzog.mylittlebonsai.data.vault

import java.text.Normalizer

private val combiningMarks = Regex("\\p{Mn}+")

actual fun stripDiacritics(text: String): String {
    val decomposed = Normalizer.normalize(text, Normalizer.Form.NFD)
    return combiningMarks.replace(decomposed, "")
}
