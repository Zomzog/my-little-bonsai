package fr.zomzog.mylittlebonsai.data.vault

import fr.zomzog.mylittlebonsai.domain.ArchivedInfo
import fr.zomzog.mylittlebonsai.domain.ArchivedReason
import fr.zomzog.mylittlebonsai.domain.Bonsai
import fr.zomzog.mylittlebonsai.domain.BonsaiAge
import fr.zomzog.mylittlebonsai.domain.BonsaiStatus
import fr.zomzog.mylittlebonsai.domain.Substrate
import fr.zomzog.mylittlebonsai.domain.SubstrateComponent
import kotlinx.datetime.LocalDate

/** Reads and writes a bonsai's `bonsai.md`, per vault-format.md. */
object BonsaiVaultCodec {

    fun encode(bonsai: Bonsai): String {
        val yaml = buildString {
            appendLine("id: ${bonsai.id}")
            appendLine("name: ${yamlScalar(bonsai.name)}")
            bonsai.species?.let { appendLine("species: $it") }
            bonsai.style?.let { appendLine("style: $it") }
            appendLine("status: ${bonsai.status.name.lowercase()}")
            bonsai.archived?.let { appendArchived(it) }
            appendLine("addedOn: ${bonsai.addedOn}")
            bonsai.age?.let { appendAge(it) }
            bonsai.substrate?.let { appendSubstrate(it) }
            bonsai.pot?.let { appendLine("pot: $it") }
            bonsai.cover?.let { appendLine("cover: $it") }
        }
        return renderFrontMatter(yaml, bonsai.description)
    }

    fun decode(text: String): Bonsai {
        val document = parseFrontMatter(text)
        val yaml = parseYaml(document.yaml)
        val id = yaml.string("id") ?: throw VaultFormatException("bonsai.md is missing 'id'")
        val name = yaml.string("name") ?: throw VaultFormatException("bonsai.md is missing 'name'")
        val statusText = yaml.string("status") ?: throw VaultFormatException("bonsai.md is missing 'status'")
        val addedOnText = yaml.string("addedOn") ?: throw VaultFormatException("bonsai.md is missing 'addedOn'")
        return Bonsai(
            id = id,
            name = name,
            species = yaml.string("species"),
            style = yaml.string("style"),
            status = parseStatus(statusText),
            archived = yaml.mapping("archived")?.let(::parseArchived),
            addedOn = LocalDate.parse(addedOnText),
            age = yaml.mapping("age")?.let(::parseAge),
            substrate = yaml.mapping("substrate")?.let(::parseSubstrate),
            pot = yaml.string("pot"),
            cover = yaml.string("cover"),
            description = document.body,
        )
    }

    private fun StringBuilder.appendArchived(archived: ArchivedInfo) {
        appendLine("archived:")
        appendLine("  reason: ${archived.reason.name.lowercase()}")
        appendLine("  date: ${archived.date}")
        archived.note?.let { appendLine("  note: ${yamlScalar(it)}") }
    }

    private fun StringBuilder.appendAge(age: BonsaiAge) {
        appendLine("age:")
        when (age) {
            is BonsaiAge.Birthday -> appendLine("  birthday: ${age.value}")
            is BonsaiAge.Years -> appendLine("  years: ${age.years}")
        }
    }

    private fun StringBuilder.appendSubstrate(substrate: Substrate) {
        appendLine("substrate:")
        appendSubstrateBody(substrate, indent = "  ")
    }

    private fun parseStatus(text: String): BonsaiStatus = when (text.lowercase()) {
        "active" -> BonsaiStatus.ACTIVE
        "archived" -> BonsaiStatus.ARCHIVED
        else -> throw VaultFormatException("Unknown bonsai status '$text'")
    }

    private fun parseArchived(mapping: YamlNode.Mapping): ArchivedInfo {
        val reasonText = mapping.string("reason")
            ?: throw VaultFormatException("archived is missing 'reason'")
        val dateText = mapping.string("date")
            ?: throw VaultFormatException("archived is missing 'date'")
        return ArchivedInfo(
            reason = runCatching { ArchivedReason.valueOf(reasonText.uppercase()) }
                .getOrElse { throw VaultFormatException("Unknown archived reason '$reasonText'") },
            date = LocalDate.parse(dateText),
            note = mapping.string("note"),
        )
    }

    private fun parseAge(mapping: YamlNode.Mapping): BonsaiAge {
        mapping.string("birthday")?.let { return BonsaiAge.Birthday(it) }
        mapping.string("years")?.let { return BonsaiAge.Years(it.toInt()) }
        throw VaultFormatException("age needs either 'birthday' or 'years'")
    }
}

internal fun parseSubstrate(mapping: YamlNode.Mapping): Substrate {
    mapping.string("mix")?.let { return Substrate.NamedMix(it) }
    mapping.sequence("components")?.let { sequence ->
        val components = sequence.items.map { item ->
            val componentMapping = item as? YamlNode.Mapping
                ?: throw VaultFormatException("substrate component must be a mapping")
            SubstrateComponent(
                soil = componentMapping.string("soil")
                    ?: throw VaultFormatException("substrate component is missing 'soil'"),
                percent = componentMapping.string("percent")?.toIntOrNull()
                    ?: throw VaultFormatException("substrate component is missing 'percent'"),
            )
        }
        return Substrate.InlineMix(components)
    }
    throw VaultFormatException("substrate needs either 'mix' or 'components'")
}

internal fun StringBuilder.appendSubstrateBody(substrate: Substrate, indent: String) {
    when (substrate) {
        is Substrate.NamedMix -> appendLine("${indent}mix: ${substrate.mixId}")
        is Substrate.InlineMix -> {
            appendLine("${indent}components:")
            for (component in substrate.components) {
                appendLine("$indent  - soil: ${component.soil}")
                appendLine("$indent    percent: ${component.percent}")
            }
        }
    }
}
