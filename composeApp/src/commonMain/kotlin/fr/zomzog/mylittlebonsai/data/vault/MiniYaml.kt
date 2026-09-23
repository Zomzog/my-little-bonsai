package fr.zomzog.mylittlebonsai.data.vault

/**
 * A tiny reader for the subset of block-style YAML the vault format uses: scalars,
 * nested mappings, and sequences of scalars or of mappings (`- key: value` items with
 * further keys aligned two spaces past the dash). This is not a general YAML parser —
 * it only needs to read back what [BonsaiVaultCodec]/[SessionVaultCodec] write, plus
 * reasonable hand edits from Obsidian.
 */
sealed interface YamlNode {
    data class Scalar(val value: String?) : YamlNode
    data class Mapping(val entries: Map<String, YamlNode>) : YamlNode
    data class Sequence(val items: List<YamlNode>) : YamlNode
}

fun YamlNode.Mapping.string(key: String): String? = (entries[key] as? YamlNode.Scalar)?.value

fun YamlNode.Mapping.mapping(key: String): YamlNode.Mapping? = entries[key] as? YamlNode.Mapping

fun YamlNode.Mapping.sequence(key: String): YamlNode.Sequence? = entries[key] as? YamlNode.Sequence

fun parseYaml(text: String): YamlNode.Mapping {
    val lines = tokenizeYaml(text)
    if (lines.isEmpty()) return YamlNode.Mapping(emptyMap())
    val parser = MiniYamlParser(lines)
    val root = parser.parseNode(lines[0].indent)
    return root as? YamlNode.Mapping ?: throw VaultFormatException("Expected a top-level mapping")
}

private data class YamlLine(val indent: Int, val content: String)

private fun tokenizeYaml(text: String): List<YamlLine> =
    text.replace("\r\n", "\n").split("\n")
        .filter { it.isNotBlank() && !it.trimStart().startsWith("#") }
        .map { raw -> YamlLine(indent = raw.takeWhile { it == ' ' }.length, content = raw.trim()) }

private class MiniYamlParser(private val lines: List<YamlLine>) {
    private var pos = 0

    fun parseNode(indent: Int): YamlNode =
        if (pos < lines.size && isSeqItem(lines[pos])) parseSequence(indent) else parseMapping(indent)

    private fun parseMapping(indent: Int): YamlNode.Mapping {
        val entries = LinkedHashMap<String, YamlNode>()
        while (pos < lines.size && lines[pos].indent == indent && !isSeqItem(lines[pos])) {
            val (key, value) = parseEntry(lines[pos].content, indent)
            entries[key] = value
        }
        return YamlNode.Mapping(entries)
    }

    private fun parseSequence(indent: Int): YamlNode.Sequence {
        val items = mutableListOf<YamlNode>()
        while (pos < lines.size && lines[pos].indent == indent && isSeqItem(lines[pos])) {
            val afterDash = lines[pos].content.removePrefix("-").trim()
            when {
                afterDash.isEmpty() -> {
                    pos++
                    items += if (pos < lines.size && lines[pos].indent > indent) {
                        parseNode(lines[pos].indent)
                    } else {
                        YamlNode.Scalar(null)
                    }
                }
                findColon(afterDash) != null -> items += parseDashMapping(indent + 2)
                else -> {
                    items += YamlNode.Scalar(unquote(afterDash))
                    pos++
                }
            }
        }
        return YamlNode.Sequence(items)
    }

    /** Parses a `- key: value` item and its continuation lines aligned at [itemIndent]. */
    private fun parseDashMapping(itemIndent: Int): YamlNode.Mapping {
        val entries = LinkedHashMap<String, YamlNode>()
        val firstContent = lines[pos].content.removePrefix("-").trim()
        val (firstKey, firstValue) = parseEntry(firstContent, itemIndent)
        entries[firstKey] = firstValue
        while (pos < lines.size && lines[pos].indent == itemIndent && !isSeqItem(lines[pos])) {
            val (key, value) = parseEntry(lines[pos].content, itemIndent)
            entries[key] = value
        }
        return YamlNode.Mapping(entries)
    }

    /** Parses one `key: value` (or `key:` with a nested block) line at [indent]; advances [pos]. */
    private fun parseEntry(content: String, indent: Int): Pair<String, YamlNode> {
        val colonIndex = findColon(content)
            ?: throw VaultFormatException("Expected 'key: value' at '$content'")
        val key = unquote(content.substring(0, colonIndex).trim())
        val rest = content.substring(colonIndex + 1).trim()
        pos++
        val value = if (rest.isEmpty()) {
            if (pos < lines.size && lines[pos].indent > indent) {
                parseNode(lines[pos].indent)
            } else {
                YamlNode.Scalar(null)
            }
        } else {
            YamlNode.Scalar(unquote(rest))
        }
        return key to value
    }

    private fun isSeqItem(line: YamlLine) = line.content == "-" || line.content.startsWith("- ")

    private fun findColon(content: String): Int? {
        val spaceColon = content.indexOf(": ")
        if (spaceColon >= 0) return spaceColon
        if (content.endsWith(":")) return content.length - 1
        return null
    }

    private fun unquote(value: String): String = when {
        value.length >= 2 && value.first() == '"' && value.last() == '"' ->
            value.substring(1, value.length - 1).replace("\\\"", "\"").replace("\\\\", "\\")
        value.length >= 2 && value.first() == '\'' && value.last() == '\'' ->
            value.substring(1, value.length - 1).replace("''", "'")
        else -> value
    }
}
