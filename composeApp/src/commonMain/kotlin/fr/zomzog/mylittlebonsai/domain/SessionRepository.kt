package fr.zomzog.mylittlebonsai.domain

import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    fun getSessionsStream(bonsaiId: String): Flow<List<Session>>
    suspend fun addSession(bonsaiId: String, session: Session)
}
