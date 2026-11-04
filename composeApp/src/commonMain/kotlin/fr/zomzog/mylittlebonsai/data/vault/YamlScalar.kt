package fr.zomzog.mylittlebonsai.data.vault

/** Characters that YAML 1.2.2 §7.3.3 forbids as the first character of a plain scalar. */
private val plainScalarIndicators = charArrayOf(
    '[', ']', '{', '}', ',', '&', '*', '!', '|', '>', '\'', '"', '%', '@', '`',
)

/** Renders [value] as a plain or double-quoted YAML scalar, quoting only when needed. */
fun yamlScalar(value: String): String {
    val looksLikeNumber = value.toDoubleOrNull() != null
    val needsQuoting = value.isEmpty() ||
        value != value.trim() ||
        value.any { it == '\n' || it == '\r' } ||
        value.contains(" #") ||
        value.startsWith("#") ||
        value.first().let { it in plainScalarIndicators } ||
        value.startsWith("- ") || value == "-" ||
        value.contains(": ") || value.endsWith(":") ||
        looksLikeNumber ||
        value in setOf("true", "false", "null", "~")
    if (!needsQuoting) return value
    return "\"${escapeYamlDoubleQuoted(value)}\""
}

/** Formats a measurement without a trailing `.0` for whole numbers, per the sample vault. */
fun yamlNumber(value: Double): String {
    val whole = value.toLong()
    return if (value == whole.toDouble()) whole.toString() else value.toString()
}

private fun escapeYamlDoubleQuoted(value: String): String {
    val builder = StringBuilder(value.length)
    for (c in value) {
        when (c) {
            '\\' -> builder.append("\\\\")
            '"' -> builder.append("\\\"")
            '\n' -> builder.append("\\n")
            '\r' -> builder.append("\\r")
            else -> builder.append(c)
        }
    }
    return builder.toString()
}

/**
 * Reverses [escapeYamlDoubleQuoted] in a single left-to-right pass. Running independent
 * `replace` calls over the whole string instead (as this used to) mis-decodes a value
 * whose escaped form contains a backslash immediately before a quote, such as the
 * escaped form of a literal trailing `\`.
 */
internal fun unescapeYamlDoubleQuoted(value: String): String {
    val builder = StringBuilder(value.length)
    var i = 0
    while (i < value.length) {
        val c = value[i]
        if (c == '\\' && i + 1 < value.length) {
            when (value[i + 1]) {
                '\\' -> {
                    builder.append('\\')
                    i += 2
                }
                '"' -> {
                    builder.append('"')
                    i += 2
                }
                'n' -> {
                    builder.append('\n')
                    i += 2
                }
                'r' -> {
                    builder.append('\r')
                    i += 2
                }
                else -> {
                    builder.append(c)
                    i += 1
                }
            }
        } else {
            builder.append(c)
            i += 1
        }
    }
    return builder.toString()
}
