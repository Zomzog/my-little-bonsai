package fr.zomzog.mylittlebonsai.data.vault

/** Renders [value] as a plain or double-quoted YAML scalar, quoting only when needed. */
fun yamlScalar(value: String): String {
    val looksLikeNumber = value.toDoubleOrNull() != null
    val needsQuoting = value.isEmpty() ||
        value != value.trim() ||
        value.any { it == '\n' || it == '\r' } ||
        value.startsWith("#") ||
        value.startsWith("- ") || value == "-" ||
        value.startsWith("\"") || value.startsWith("'") ||
        value.contains(": ") || value.endsWith(":") ||
        looksLikeNumber ||
        value in setOf("true", "false", "null", "~")
    if (!needsQuoting) return value
    val escaped = value.replace("\\", "\\\\").replace("\"", "\\\"")
    return "\"$escaped\""
}

/** Formats a measurement without a trailing `.0` for whole numbers, per the sample vault. */
fun yamlNumber(value: Double): String {
    val whole = value.toLong()
    return if (value == whole.toDouble()) whole.toString() else value.toString()
}
