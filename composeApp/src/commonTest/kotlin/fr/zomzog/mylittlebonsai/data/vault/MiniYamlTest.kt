package fr.zomzog.mylittlebonsai.data.vault

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFailure
import assertk.assertions.isNull
import kotlin.test.Test

class MiniYamlTest {

    @Test
    fun parsesFlatScalars() {
        val yaml = parseYaml("schemaVersion: 1\ncreationDate: 2026-05-23")
        assertThat(yaml.string("schemaVersion")).isEqualTo("1")
        assertThat(yaml.string("creationDate")).isEqualTo("2026-05-23")
    }

    @Test
    fun parsesEmptyDocumentAsEmptyMapping() {
        assertThat(parseYaml("").entries.size).isEqualTo(0)
    }

    @Test
    fun parsesNestedMapping() {
        val yaml = parseYaml("age:\n  birthday: 2016-04")
        assertThat(yaml.mapping("age")?.string("birthday")).isEqualTo("2016-04")
    }

    @Test
    fun parsesKeyWithNoValueAsNullScalar() {
        val yaml = parseYaml("cover:")
        assertThat((yaml.entries["cover"] as YamlNode.Scalar).value).isNull()
    }

    @Test
    fun parsesSequenceOfScalars() {
        val yaml = parseYaml("removed:\n  - ulmus-parvifolia\n  - moyogi")
        val items = yaml.sequence("removed")!!.items.map { (it as YamlNode.Scalar).value }
        assertThat(items).isEqualTo(listOf("ulmus-parvifolia", "moyogi"))
    }

    @Test
    fun parsesSequenceOfMappingsWithContinuationLines() {
        val text = """
            actions:
              - action: repotting
                substrate:
                  mix: abc-123
                pot: pot-1
              - action: measuring
                height: 42.5
                width: 38
        """.trimIndent()
        val yaml = parseYaml(text)
        val actions = yaml.sequence("actions")!!.items.map { it as YamlNode.Mapping }
        assertThat(actions[0].string("action")).isEqualTo("repotting")
        assertThat(actions[0].mapping("substrate")?.string("mix")).isEqualTo("abc-123")
        assertThat(actions[0].string("pot")).isEqualTo("pot-1")
        assertThat(actions[1].string("action")).isEqualTo("measuring")
        assertThat(actions[1].string("height")).isEqualTo("42.5")
        assertThat(actions[1].string("width")).isEqualTo("38")
    }

    @Test
    fun parsesDoublyNestedSequenceUnderADashMapping() {
        val text = """
            substrate:
              components:
                - soil: akadama
                  percent: 60
                - soil: pumice
                  percent: 40
        """.trimIndent()
        val yaml = parseYaml(text)
        val components = yaml.mapping("substrate")!!.sequence("components")!!.items.map { it as YamlNode.Mapping }
        assertThat(components[0].string("soil")).isEqualTo("akadama")
        assertThat(components[0].string("percent")).isEqualTo("60")
        assertThat(components[1].string("soil")).isEqualTo("pumice")
        assertThat(components[1].string("percent")).isEqualTo("40")
    }

    @Test
    fun parsesDoubleQuotedScalarWithEscapes() {
        val yaml = parseYaml("note: \"Fix: broken pot\"")
        assertThat(yaml.string("note")).isEqualTo("Fix: broken pot")
    }

    @Test
    fun parsesSingleQuotedScalarWithDoubledQuoteEscape() {
        val yaml = parseYaml("note: 'It''s fine'")
        assertThat(yaml.string("note")).isEqualTo("It's fine")
    }

    @Test
    fun ignoresBlankLinesAndComments() {
        val yaml = parseYaml("id: 1\n\n# a comment\nname: Akira\n")
        assertThat(yaml.string("id")).isEqualTo("1")
        assertThat(yaml.string("name")).isEqualTo("Akira")
    }

    @Test
    fun missingKeyReturnsNullFromStringAccessor() {
        val yaml = parseYaml("id: 1")
        assertThat(yaml.string("missing")).isNull()
    }

    @Test
    fun wrongTypeAccessorsReturnNull() {
        val yaml = parseYaml("id: 1")
        assertThat(yaml.mapping("id")).isNull()
        assertThat(yaml.sequence("id")).isNull()
    }

    @Test
    fun throwsOnLineWithoutColon() {
        assertThat(runCatching { parseYaml("not a valid line") }).isFailure()
    }

    @Test
    fun throwsWhenTopLevelIsASequence() {
        assertThat(runCatching { parseYaml("- one\n- two") }).isFailure()
    }
}
