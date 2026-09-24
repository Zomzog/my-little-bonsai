package fr.zomzog.mylittlebonsai.data.vault

import fr.zomzog.mylittlebonsai.domain.Action
import fr.zomzog.mylittlebonsai.domain.DoseUnit
import fr.zomzog.mylittlebonsai.domain.FertilizerDose
import fr.zomzog.mylittlebonsai.domain.Session
import fr.zomzog.mylittlebonsai.domain.VaultTimestamp
import kotlinx.datetime.LocalDate

/** Reads and writes a bonsai's `sessions/YYYY-MM-DD-HHmm.md`, per vault-format.md. */
object SessionVaultCodec {

    fun encode(session: Session): String {
        val yaml = buildString {
            appendLine("id: ${session.id}")
            appendLine("date: ${session.date}")
            appendLine("createdAt: ${session.createdAt}")
            appendLine("actions:")
            for (action in session.actions) appendAction(action)
        }
        return renderFrontMatter(yaml, session.body)
    }

    fun decode(text: String): Session {
        val document = parseFrontMatter(text)
        val yaml = parseYaml(document.yaml)
        val id = yaml.string("id") ?: throw VaultFormatException("session is missing 'id'")
        val dateText = yaml.string("date") ?: throw VaultFormatException("session is missing 'date'")
        val createdAtText = yaml.string("createdAt")
            ?: throw VaultFormatException("session is missing 'createdAt'")
        val actionsSequence = yaml.sequence("actions")
            ?: throw VaultFormatException("session is missing 'actions'")
        val actions = actionsSequence.items.map { item ->
            val mapping = item as? YamlNode.Mapping
                ?: throw VaultFormatException("an action must be a mapping")
            parseAction(mapping)
        }
        return Session(
            id = id,
            date = LocalDate.parse(dateText),
            createdAt = VaultTimestamp.parse(createdAtText),
            actions = actions,
            body = document.body,
        )
    }

    private fun StringBuilder.appendAction(action: Action) {
        appendLine("  - action: ${actionId(action)}")
        when (action) {
            is Action.Measuring -> {
                action.height?.let { appendLine("    height: ${yamlNumber(it)}") }
                action.width?.let { appendLine("    width: ${yamlNumber(it)}") }
                action.nebari?.let { appendLine("    nebari: ${yamlNumber(it)}") }
                action.weight?.let { appendLine("    weight: ${yamlNumber(it)}") }
            }
            is Action.Repotting -> {
                action.substrate?.let {
                    appendLine("    substrate:")
                    appendSubstrateBody(it, indent = "      ")
                }
                action.pot?.let { appendLine("    pot: $it") }
            }
            is Action.Fertilizing -> {
                appendLine("    fertilizer: ${action.fertilizer}")
                action.dose?.let { dose ->
                    appendLine("    dose:")
                    appendLine("      amount: ${yamlNumber(dose.amount)}")
                    appendLine("      unit: ${doseUnitToYaml(dose.unit)}")
                }
            }
            is Action.Treatment -> {
                appendLine("    treatment: ${action.treatment}")
                action.target?.let { appendLine("    target: ${yamlScalar(it)}") }
            }
            is Action.Other -> Unit
        }
        action.note?.let { appendLine("    note: ${yamlScalar(it)}") }
    }

    private fun actionId(action: Action): String = when (action) {
        is Action.Measuring -> "measuring"
        is Action.Repotting -> "repotting"
        is Action.Fertilizing -> "fertilizing"
        is Action.Treatment -> "treatment"
        is Action.Other -> action.actionId
    }

    private fun doseUnitToYaml(unit: DoseUnit): String = when (unit) {
        DoseUnit.G -> "g"
        DoseUnit.ML -> "ml"
        DoseUnit.G_PER_L -> "g/l"
        DoseUnit.ML_PER_L -> "ml/l"
    }

    private fun parseDoseUnit(text: String): DoseUnit = when (text) {
        "g" -> DoseUnit.G
        "ml" -> DoseUnit.ML
        "g/l" -> DoseUnit.G_PER_L
        "ml/l" -> DoseUnit.ML_PER_L
        else -> throw VaultFormatException("Unknown dose unit '$text'")
    }

    private fun parseAction(mapping: YamlNode.Mapping): Action {
        val actionId = mapping.string("action")
            ?: throw VaultFormatException("an action item is missing 'action'")
        val note = mapping.string("note")
        return when (actionId) {
            "measuring" -> Action.Measuring(
                height = mapping.string("height")?.toDouble(),
                width = mapping.string("width")?.toDouble(),
                nebari = mapping.string("nebari")?.toDouble(),
                weight = mapping.string("weight")?.toDouble(),
                note = note,
            )
            "repotting" -> Action.Repotting(
                substrate = mapping.mapping("substrate")?.let(::parseSubstrate),
                pot = mapping.string("pot"),
                note = note,
            )
            "fertilizing" -> Action.Fertilizing(
                fertilizer = mapping.string("fertilizer")
                    ?: throw VaultFormatException("fertilizing action is missing 'fertilizer'"),
                dose = mapping.mapping("dose")?.let { dose ->
                    FertilizerDose(
                        amount = dose.string("amount")?.toDouble()
                            ?: throw VaultFormatException("dose is missing 'amount'"),
                        unit = parseDoseUnit(
                            dose.string("unit") ?: throw VaultFormatException("dose is missing 'unit'"),
                        ),
                    )
                },
                note = note,
            )
            "treatment" -> Action.Treatment(
                treatment = mapping.string("treatment")
                    ?: throw VaultFormatException("treatment action is missing 'treatment'"),
                target = mapping.string("target"),
                note = note,
            )
            else -> Action.Other(actionId = actionId, note = note)
        }
    }
}
