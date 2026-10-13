package fr.zomzog.mylittlebonsai.data.vault

@JsFun("(text) => text.normalize('NFD').replace(/\\p{Mn}/gu, '')")
private external fun stripDiacriticsJs(text: String): String

actual fun stripDiacritics(text: String): String = stripDiacriticsJs(text)
