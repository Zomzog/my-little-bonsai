package fr.zomzog.mylittlebonsai.domain.species

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import kotlin.test.Test

class SpeciesSortingTest {

    private val erable = Species("acer-palmatum", "Acer palmatum", "Japanese maple", "Érable du Japon")
    private val chene = Species("quercus-robur", "Quercus robur", "English oak", "Chêne pédonculé")
    private val buis = Species("buxus-sempervirens", "Buxus sempervirens", "Common boxwood", "Buis commun")
    private val if_ = Species("taxus-baccata", "Taxus baccata", "English yew", "If commun")

    @Test
    fun sortsByEnglishLabelAlphabetically() {
        val sorted = listOf(erable, chene, buis, if_).sortedByLabel(UiLanguage.EN)
        assertThat(sorted.map { it.commonNameEn }).containsExactly(
            "Common boxwood",
            "English oak",
            "English yew",
            "Japanese maple",
        )
    }

    @Test
    fun sortsByFrenchLabelIgnoringAccents() {
        // "Érable" sorts as if it were "Erable": between "Chêne" and "If", not after "If".
        val sorted = listOf(erable, chene, buis, if_).sortedByLabel(UiLanguage.FR)
        assertThat(sorted.map { it.commonNameFr }).containsExactly(
            "Buis commun",
            "Chêne pédonculé",
            "Érable du Japon",
            "If commun",
        )
    }

    @Test
    fun sortIsCaseInsensitive() {
        val lower = Species("a", null, "apple", "pomme")
        val upperFirst = Species("b", null, "Banana", "Poire")
        val sorted = listOf(upperFirst, lower).sortedByLabel(UiLanguage.EN)
        assertThat(sorted.map { it.commonNameEn }).containsExactly("apple", "Banana")
    }

    @Test
    fun foldToAccentInsensitiveKeyStripsCommonDiacritics() {
        assertThat("Érable".foldToAccentInsensitiveKey()).isEqualTo("erable")
    }
}
