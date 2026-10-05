package fr.zomzog.mylittlebonsai.data.vault

import fr.zomzog.mylittlebonsai.domain.Action
import fr.zomzog.mylittlebonsai.domain.Session
import fr.zomzog.mylittlebonsai.domain.SessionRepository
import fr.zomzog.mylittlebonsai.domain.VaultTimestamp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Newest first: by `date`, then by `createdAt` — the order vault-format.md's timeline uses. */
private val sessionOrder = compareBy<Session>({ it.date }, { it.createdAt.toString() })

/**
 * [SessionRepository] backed by session files (`*.md`) under `bonsais/<slug>/sessions/` in a
 * [VaultFileSystem].
 *
 * Depends on [bonsaiRepository] to resolve a bonsai id to its vault folder, and to apply
 * the vault format's rule that the latest repotting session's substrate/pot are copied
 * onto `bonsai.md` (vault-format.md, "Actions" table).
 */
class VaultSessionRepository(
    private val fileSystem: VaultFileSystem,
    private val bonsaiRepository: VaultBonsaiRepository,
) : SessionRepository {

    private val sessionsByBonsai = mutableMapOf<String, MutableStateFlow<List<Session>>>()
    private val loadedBonsaiIds = mutableSetOf<String>()
    private val loadMutex = Mutex()

    override fun getSessionsStream(bonsaiId: String): Flow<List<Session>> = flow {
        emitAll(ensureLoaded(bonsaiId))
    }

    override suspend fun addSession(bonsaiId: String, session: Session) {
        val slug = bonsaiRepository.slugFor(bonsaiId)
            ?: error("Cannot add a session to unknown bonsai $bonsaiId")
        val stateFlow = ensureLoaded(bonsaiId)
        val sessionsDir = "bonsais/$slug/sessions"
        val fileName = sessionFileName(session.createdAt, fileSystem.listFiles(sessionsDir).toSet())
        fileSystem.writeTextAtomic("$sessionsDir/$fileName", SessionVaultCodec.encode(session))
        stateFlow.update { (it + session).sortedWith(sessionOrder.reversed()) }
        syncIfLatestRepotting(bonsaiId, session, stateFlow.value)
    }

    private suspend fun ensureLoaded(bonsaiId: String): MutableStateFlow<List<Session>> {
        sessionsByBonsai[bonsaiId]?.let { if (bonsaiId in loadedBonsaiIds) return it }
        return loadMutex.withLock {
            sessionsByBonsai[bonsaiId]?.let { if (bonsaiId in loadedBonsaiIds) return@withLock it }
            val slug = bonsaiRepository.slugFor(bonsaiId)
            val sessions = if (slug != null) {
                val sessionsDir = "bonsais/$slug/sessions"
                fileSystem.listFiles(sessionsDir).mapNotNull { fileName ->
                    runCatching { SessionVaultCodec.decode(fileSystem.readText("$sessionsDir/$fileName")) }
                        .getOrNull()
                }.sortedWith(sessionOrder.reversed())
            } else {
                emptyList()
            }
            loadedBonsaiIds += bonsaiId
            sessionsByBonsai.getOrPut(bonsaiId) { MutableStateFlow(emptyList()) }.also { it.value = sessions }
        }
    }

    /**
     * Copies [session]'s substrate/pot onto `bonsai.md`, but only when [session] is itself a
     * repotting session and is the chronologically-latest one — a session with no repotting
     * action (or an older repotting session loaded alongside a newer one) must not trigger a
     * rewrite. Rereads `bonsai.md` from disk via [VaultBonsaiRepository.refreshBonsai] right
     * before the rewrite rather than trusting this repository's in-memory snapshot, so a sync
     * never clobbers a hand-edit (or another write) made to the file since it was last loaded.
     */
    private suspend fun syncIfLatestRepotting(bonsaiId: String, session: Session, allSessions: List<Session>) {
        if (session.actions.none { it is Action.Repotting }) return
        val latestRepotting = allSessions
            .filter { s -> s.actions.any { it is Action.Repotting } }
            .maxWithOrNull(sessionOrder)
            ?: return
        if (latestRepotting.id != session.id) return
        val repottingAction = latestRepotting.actions.filterIsInstance<Action.Repotting>().last()
        val bonsai = bonsaiRepository.refreshBonsai(bonsaiId) ?: return
        bonsaiRepository.updateBonsai(
            bonsai.copy(
                substrate = repottingAction.substrate ?: bonsai.substrate,
                pot = repottingAction.pot ?: bonsai.pot,
            ),
        )
    }

    private fun sessionFileName(createdAt: VaultTimestamp, existingNames: Set<String>): String {
        val prefix = createdAt.toFileNamePrefix()
        var candidate = "$prefix.md"
        var suffix = 2
        while (candidate in existingNames) {
            candidate = "$prefix-$suffix.md"
            suffix++
        }
        return candidate
    }
}
