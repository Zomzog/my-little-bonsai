package fr.zomzog.mylittlebonsai.data.vault

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFailure
import kotlin.test.Test

class FrontMatterTest {

    @Test
    fun parsesYamlAndBody() {
        val text = "---\nid: 1\nname: Akira\n---\n\nHello world\n"
        val doc = parseFrontMatter(text)
        assertThat(doc.yaml).isEqualTo("id: 1\nname: Akira")
        assertThat(doc.body).isEqualTo("Hello world")
    }

    @Test
    fun parsesEmptyBody() {
        val doc = parseFrontMatter("---\nid: 1\n---\n")
        assertThat(doc.body).isEqualTo("")
    }

    @Test
    fun parsesEmptyFrontMatter() {
        val doc = parseFrontMatter("---\n---\n\nBody only\n")
        assertThat(doc.yaml).isEqualTo("")
        assertThat(doc.body).isEqualTo("Body only")
    }

    @Test
    fun throwsWhenMissingOpeningMarker() {
        assertThat(runCatching { parseFrontMatter("id: 1\n---\n") }).isFailure()
    }

    @Test
    fun throwsWhenMissingClosingMarker() {
        assertThat(runCatching { parseFrontMatter("---\nid: 1\n") }).isFailure()
    }

    @Test
    fun rendersYamlAndBodyWithTrailingNewlines() {
        val rendered = renderFrontMatter("id: 1\nname: Akira", "Hello world")
        assertThat(rendered).isEqualTo("---\nid: 1\nname: Akira\n---\n\nHello world\n")
    }

    @Test
    fun rendersEmptyBodyWithoutTrailingBlankSection() {
        val rendered = renderFrontMatter("id: 1", "")
        assertThat(rendered).isEqualTo("---\nid: 1\n---\n")
    }

    @Test
    fun roundTripsThroughParseAndRender() {
        val original = "---\nid: 1\nname: Akira\n---\n\nBought in spring.\n"
        val doc = parseFrontMatter(original)
        assertThat(renderFrontMatter(doc.yaml, doc.body)).isEqualTo(original)
    }
}
