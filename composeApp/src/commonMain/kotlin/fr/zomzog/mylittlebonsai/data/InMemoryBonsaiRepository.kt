package fr.zomzog.mylittlebonsai.data

import fr.zomzog.mylittlebonsai.domain.Bonsai
import fr.zomzog.mylittlebonsai.domain.BonsaiRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class InMemoryBonsaiRepository(
    initialBonsais: List<Bonsai> = emptyList(),
) : BonsaiRepository {
    private val _bonsais = MutableStateFlow(initialBonsais)

    override fun getBonsaisStream(): Flow<List<Bonsai>> = _bonsais.asStateFlow()

    override suspend fun addBonsai(bonsai: Bonsai) {
        _bonsais.update { it + bonsai }
    }
}
