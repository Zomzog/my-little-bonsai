package fr.zomzog.mylittlebonsai.data.vault

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class SlugTest {

    @Test
    fun stripsAccentsAndLowercases() {
        assertThat(slugify("Érable du Jardin", emptySet())).isEqualTo("erable-du-jardin")
    }

    @Test
    fun replacesNonAlphanumericRunsWithASingleDash() {
        assertThat(slugify("Juniperus #1", emptySet())).isEqualTo("juniperus-1")
    }

    @Test
    fun fallsBackToBonsaiWhenNothingAlphanumericRemains() {
        assertThat(slugify("盆栽", emptySet())).isEqualTo("bonsai")
    }

    @Test
    fun foldsLigaturesThatDoNotDecompose() {
        assertThat(slugify("Bonsaï æøœß", emptySet())).isEqualTo("bonsai-aeooess")
    }

    @Test
    fun trimsLeadingAndTrailingDashes() {
        assertThat(slugify("  -- Akira -- ", emptySet())).isEqualTo("akira")
    }

    @Test
    fun cutsToSixtyCharactersAndTrimsAgain() {
        val longName = "a".repeat(65)
        val slug = slugify(longName, emptySet())
        assertThat(slug).isEqualTo("a".repeat(60))
    }

    @Test
    fun appendsSuffixOnCollision() {
        assertThat(slugify("Akira", setOf("akira"))).isEqualTo("akira-2")
    }

    @Test
    fun appendsIncreasingSuffixUntilFree() {
        assertThat(slugify("Akira", setOf("akira", "akira-2", "akira-3"))).isEqualTo("akira-4")
    }

    @Test
    fun appendsSuffixForReservedWindowsName() {
        assertThat(slugify("con", emptySet())).isEqualTo("con-2")
    }

    @Test
    fun noCollisionReturnsBaseSlugUnchanged() {
        assertThat(slugify("Akira", setOf("bonsuke"))).isEqualTo("akira")
    }
}
