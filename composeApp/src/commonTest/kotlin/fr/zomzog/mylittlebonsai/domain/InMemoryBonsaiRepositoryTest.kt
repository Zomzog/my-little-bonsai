package fr.zomzog.mylittlebonsai.domain

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import fr.zomzog.mylittlebonsai.data.InMemoryBonsaiRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test

class InMemoryBonsaiRepositoryTest {

    private val bonsaiA = Bonsai(
        id = "id-a",
        name = "Akira",
        kind = "Maple",
        purchaseDate = LocalDate(2024, 3, 10),
    )
    private val bonsaiB = Bonsai(
        id = "id-b",
        name = "Bonsuke",
        kind = "Pine",
        purchaseDate = LocalDate(2023, 7, 1),
        lastMaintenanceDate = LocalDate(2024, 1, 15),
    )

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
        assertThat(stored.id).isEqualTo("id-b")
        assertThat(stored.name).isEqualTo("Bonsuke")
        assertThat(stored.kind).isEqualTo("Pine")
        assertThat(stored.purchaseDate).isEqualTo(LocalDate(2023, 7, 1))
        assertThat(stored.lastMaintenanceDate).isEqualTo(LocalDate(2024, 1, 15))
    }
}
