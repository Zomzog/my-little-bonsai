package fr.zomzog.mylittlebonsai.domain

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import fr.zomzog.mylittlebonsai.data.InMemoryBonsaiRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test

class InMemoryBonsaiRepositoryTest {

    private val bonsaiA = Bonsai(id = "id-a", name = "Akira", addedOn = LocalDate(2024, 3, 10))
    private val bonsaiB = Bonsai(id = "id-b", name = "Bonsuke", addedOn = LocalDate(2023, 7, 1))

    @Test
    fun streamEmitsEmptyListInitially() = runTest {
        val repository = InMemoryBonsaiRepository()
        assertThat(repository.getBonsaisStream().first()).isEmpty()
    }

    @Test
    fun constructorPopulatesInitialBonsais() = runTest {
        val repository = InMemoryBonsaiRepository(listOf(bonsaiA))
        assertThat(repository.getBonsaisStream().first()).containsExactly(bonsaiA)
    }

    @Test
    fun addBonsaiEmitsUpdatedList() = runTest {
        val repository = InMemoryBonsaiRepository()
        repository.addBonsai(bonsaiA)
        assertThat(repository.getBonsaisStream().first()).containsExactly(bonsaiA)
    }

    @Test
    fun multipleAddsAccumulateInOrder() = runTest {
        val repository = InMemoryBonsaiRepository()
        repository.addBonsai(bonsaiA)
        repository.addBonsai(bonsaiB)
        assertThat(repository.getBonsaisStream().first()).containsExactly(bonsaiA, bonsaiB)
    }

    @Test
    fun addedBonsaiPreservesAllFields() = runTest {
        val repository = InMemoryBonsaiRepository()
        repository.addBonsai(bonsaiB)
        val stored = repository.getBonsaisStream().first().first()
        assertThat(stored).isEqualTo(bonsaiB)
    }

    @Test
    fun getBonsaiReturnsNullForUnknownId() = runTest {
        assertThat(InMemoryBonsaiRepository().getBonsai("missing")).isNull()
    }

    @Test
    fun getBonsaiFindsAnAddedBonsai() = runTest {
        val repository = InMemoryBonsaiRepository()
        repository.addBonsai(bonsaiA)
        assertThat(repository.getBonsai("id-a")).isEqualTo(bonsaiA)
    }

    @Test
    fun updateBonsaiReplacesTheMatchingEntry() = runTest {
        val repository = InMemoryBonsaiRepository(listOf(bonsaiA, bonsaiB))
        val updated = bonsaiA.copy(name = "Akira Renamed")
        repository.updateBonsai(updated)
        assertThat(repository.getBonsaisStream().first()).containsExactly(updated, bonsaiB)
    }
}
