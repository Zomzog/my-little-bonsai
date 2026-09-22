package fr.zomzog.mylittlebonsai.data

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import fr.zomzog.mylittlebonsai.domain.Bonsai
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test

class StoredBonsaiRepositoryTest {

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
    fun streamIsEmptyWhenStoreHasNoEntry() = runTest {
        val repository = StoredBonsaiRepository(FakeKeyValueStore())
        assertThat(repository.getBonsaisStream().first()).isEmpty()
    }

    @Test
    fun loadsExistingBonsaisFromStoreOnCreation() = runTest {
        val store = FakeKeyValueStore(
            mapOf(BONSAI_LIST_STORAGE_KEY to BonsaiSerialization.encode(listOf(bonsaiA, bonsaiB))),
        )
        assertThat(StoredBonsaiRepository(store).getBonsaisStream().first())
            .containsExactly(bonsaiA, bonsaiB)
    }

    @Test
    fun addBonsaiEmitsUpdatedList() = runTest {
        val repository = StoredBonsaiRepository(FakeKeyValueStore())
        repository.addBonsai(bonsaiA)
        assertThat(repository.getBonsaisStream().first()).containsExactly(bonsaiA)
    }

    @Test
    fun addBonsaiWritesThroughToTheStore() = runTest {
        val store = FakeKeyValueStore()
        StoredBonsaiRepository(store).addBonsai(bonsaiA)
        assertThat(store.read(BONSAI_LIST_STORAGE_KEY)).isNotNull()
            .isEqualTo(BonsaiSerialization.encode(listOf(bonsaiA)))
    }

    @Test
    fun addedBonsaisAreVisibleToANewRepositoryOverTheSameStore() = runTest {
        val store = FakeKeyValueStore()
        StoredBonsaiRepository(store).addBonsai(bonsaiA)

        // Simulates a page reload: fresh repository, same underlying browser storage.
        assertThat(StoredBonsaiRepository(store).getBonsaisStream().first())
            .containsExactly(bonsaiA)
    }

    @Test
    fun multipleAddsAccumulateInOrderAndPersist() = runTest {
        val store = FakeKeyValueStore()
        val repository = StoredBonsaiRepository(store)
        repository.addBonsai(bonsaiA)
        repository.addBonsai(bonsaiB)

        assertThat(repository.getBonsaisStream().first()).containsExactly(bonsaiA, bonsaiB)
        assertThat(StoredBonsaiRepository(store).getBonsaisStream().first())
            .containsExactly(bonsaiA, bonsaiB)
    }

    @Test
    fun corruptStoredValueIsIgnoredRatherThanFailing() = runTest {
        val store = FakeKeyValueStore(mapOf(BONSAI_LIST_STORAGE_KEY to "{not json"))
        assertThat(StoredBonsaiRepository(store).getBonsaisStream().first()).isEmpty()
    }

    @Test
    fun readingDoesNotWriteToTheStore() = runTest {
        val store = FakeKeyValueStore()
        StoredBonsaiRepository(store).getBonsaisStream().first()
        assertThat(store.writeCount).isEqualTo(0)
    }

    @Test
    fun usesTheProvidedStorageKey() = runTest {
        val store = FakeKeyValueStore()
        StoredBonsaiRepository(store, key = "custom.key").addBonsai(bonsaiA)
        assertThat(store.read("custom.key")).isNotNull()
    }
}
