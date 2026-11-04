package fr.zomzog.mylittlebonsai.data.vault

/**
 * Splits a vault Markdown file into its YAML front-matter and Markdown body, per
 * vault-format.md: front-matter starts on the first line with `---` and ends with the
 * next line that is exactly `---`; the body is everything after it and may be empty.
 */
data class FrontMatterDocument(val yaml: String, val body: String)

class VaultFormatException(message: String) : Exception(message)

fun parseFrontMatter(text: String): FrontMatterDocument {
    val lines = text.replace("\r\n", "\n").split("\n")
    if (lines.firstOrNull() != "---") {
        throw VaultFormatException("File does not start with a '---' front-matter marker")
    }
    val endIndex = lines.drop(1).indexOfFirst { it == "---" } + 1
    if (endIndex <= 0) {
        throw VaultFormatException("Front-matter is not closed with a '---' marker")
    }
    val yaml = lines.subList(1, endIndex).joinToString("\n")
    // Leading/trailing blank lines are a formatting artifact of the file, not part of
    // the body value, so both ends are trimmed (renderFrontMatter adds them back).
    val body = lines.subList(endIndex + 1, lines.size).joinToString("\n").trim('\n')
    return FrontMatterDocument(yaml, body)
}

fun renderFrontMatter(yaml: String, body: String): String = buildString {
    append("---\n")
    append(yaml)
    if (!yaml.endsWith("\n")) append("\n")
    append("---\n")
    if (body.isNotEmpty()) {
        append("\n")
        append(body)
        if (!body.endsWith("\n")) append("\n")
    }
}
