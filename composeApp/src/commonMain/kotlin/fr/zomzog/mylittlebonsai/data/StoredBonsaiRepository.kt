package fr.zomzog.mylittlebonsai.data

import fr.zomzog.mylittlebonsai.domain.Bonsai
import fr.zomzog.mylittlebonsai.domain.BonsaiRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

const val BONSAI_LIST_STORAGE_KEY = "bonsai.bonsais"

/**
 * [BonsaiRepository] that keeps the list in a [KeyValueStore], so records survive
 * a page reload without the user having to grant access to anything.
 *
 * The list is read once at construction and rewritten in full on every change —
 * the dataset is small enough that incremental writes would be needless complexity.
 */
class StoredBonsaiRepository(
    private val store: KeyValueStore,
    private val key: String = BONSAI_LIST_STORAGE_KEY,
) : BonsaiRepository {

    private val bonsais = MutableStateFlow(load())

    override fun getBonsaisStream(): Flow<List<Bonsai>> = bonsais.asStateFlow()

    override suspend fun addBonsai(bonsai: Bonsai) {
        val updated = bonsais.value + bonsai
        bonsais.value = updated
        store.write(key, BonsaiSerialization.encode(updated))
    }

    private fun load(): List<Bonsai> =
        store.read(key)?.let(BonsaiSerialization::decode) ?: emptyList()
}
