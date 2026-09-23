package fr.zomzog.mylittlebonsai.domain.species

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEmpty
import assertk.assertions.isTrue
import kotlin.test.Test

class DefaultSpeciesTest {

    private val kebabCase = Regex("[a-z0-9]+(-[a-z0-9]+)*")

    @Test
    fun catalogueHasFortyEntries() {
        assertThat(DefaultSpecies.all).hasSize(40)
    }

    @Test
    fun everyIdIsUnique() {
        val ids = DefaultSpecies.all.map { it.id }
        assertThat(ids.toSet()).hasSize(ids.size)
    }

    @Test
    fun everyIdIsKebabCase() {
        assertThat(DefaultSpecies.all.all { kebabCase.matches(it.id) }).isTrue()
    }

    @Test
    fun everyIdIsTheLatinNameAsKebabCase() {
        val mismatches = DefaultSpecies.all.filter { species ->
            val expected = species.latinName
                ?.lowercase()
                ?.replace(Regex("[^a-z0-9]+"), "-")
                ?.trim('-')
            species.id != expected
        }
        assertThat(mismatches).isEqualTo(emptyList())
    }

    @Test
    fun everyEntryHasLatinNameAndBothLabels() {
        assertThat(
            DefaultSpecies.all.all {
                !it.latinName.isNullOrBlank() && it.commonNameEn.isNotBlank() && it.commonNameFr.isNotBlank()
            },
        ).isTrue()
    }

    @Test
    fun catalogueIsNotEmpty() {
        assertThat(DefaultSpecies.all).isNotEmpty()
    }
}
