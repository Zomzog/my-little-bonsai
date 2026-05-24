package fr.zomzog.mylittlebonsai.domain

import kotlinx.coroutines.flow.Flow

interface BonsaiRepository {
    fun getBonsaisStream(): Flow<List<Bonsai>>
    suspend fun addBonsai(bonsai: Bonsai)
}
