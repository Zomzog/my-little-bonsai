package fr.zomzog.mylittlebonsai.data.vault

import fr.zomzog.mylittlebonsai.domain.Bonsai
import fr.zomzog.mylittlebonsai.domain.BonsaiRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * [BonsaiRepository] backed by `bonsais/<slug>/bonsai.md` files in a [VaultFileSystem].
 *
 * The whole `bonsais/` tree is read once, lazily, on first use, building an in-memory
 * index (the bonsai list plus an id-to-slug map) for fast listing after that. A file
 * that fails to parse is skipped rather than failing the whole load, since a hand-edited
 * vault may contain an entry the app cannot yet read.
 */
class VaultBonsaiRepository(private val fileSystem: VaultFileSystem) : BonsaiRepository {

    private val bonsais = MutableStateFlow<List<Bonsai>>(emptyList())
    private val slugsById = mutableMapOf<String, String>()
    private val loadMutex = Mutex()
    private val writeMutex = Mutex()
    private var isLoaded = false

    override fun getBonsaisStream(): Flow<List<Bonsai>> = flow {
        ensureLoaded()
        emitAll(bonsais)
    }

    override suspend fun addBonsai(bonsai: Bonsai) {
        ensureLoaded()
        writeMutex.withLock {
            // Checked against the actual folders on disk, not just slugsById's loaded
            // entries: a folder that failed to parse at load time (or has no bonsai.md)
            // still occupies its slug and must not be picked for a new bonsai. The mutex
            // makes the check-then-write atomic with respect to other adds on this
            // repository instance.
            val takenSlugs = fileSystem.listDirectories("bonsais").toSet() + slugsById.values
            val slug = slugify(bonsai.name, takenSlugs)
            fileSystem.writeTextAtomic("bonsais/$slug/bonsai.md", BonsaiVaultCodec.encode(bonsai))
            slugsById[bonsai.id] = slug
            bonsais.update { it + bonsai }
        }
    }

    override suspend fun getBonsai(id: String): Bonsai? {
        ensureLoaded()
        return bonsais.value.find { it.id == id }
    }

    /**
     * Rewrites the bonsai's existing folder in place. Renaming a bonsai (which would
     * change its slug and rename the folder, per vault-format.md) is not supported here
     * yet — there is no UI path that changes a bonsai's name after creation.
     */
    override suspend fun updateBonsai(bonsai: Bonsai) {
        ensureLoaded()
        writeMutex.withLock {
            val slug = slugsById[bonsai.id]
                ?: error("Cannot update bonsai ${bonsai.id}: it was never loaded or added")
            fileSystem.writeTextAtomic("bonsais/$slug/bonsai.md", BonsaiVaultCodec.encode(bonsai))
            bonsais.update { list -> list.map { if (it.id == bonsai.id) bonsai else it } }
        }
    }

    /** The folder name a bonsai id lives in, or `null` if it isn't loaded. */
    internal suspend fun slugFor(id: String): String? {
        ensureLoaded()
        return slugsById[id]
    }

    /**
     * Re-reads [id]'s `bonsai.md` from disk and refreshes the in-memory copy, returning the
     * fresh value. Used by a writer that must base a rewrite on the file's current content
     * (see [VaultSessionRepository]'s repotting sync) rather than on this repository's
     * possibly-stale in-memory snapshot, so it doesn't clobber a hand-edit made since this
     * repository last loaded the file.
     */
    internal suspend fun refreshBonsai(id: String): Bonsai? {
        ensureLoaded()
        val slug = slugsById[id] ?: return null
        val fresh = runCatching {
            BonsaiVaultCodec.decode(fileSystem.readText("bonsais/$slug/bonsai.md"))
        }.getOrNull() ?: return null
        bonsais.update { list -> list.map { if (it.id == id) fresh else it } }
        return fresh
    }

    private suspend fun ensureLoaded() {
        if (isLoaded) return
        loadMutex.withLock {
            if (isLoaded) return@withLock
            val loadedBonsais = mutableListOf<Bonsai>()
            for (slug in fileSystem.listDirectories("bonsais")) {
                val bonsai = runCatching {
                    BonsaiVaultCodec.decode(fileSystem.readText("bonsais/$slug/bonsai.md"))
                }.getOrNull() ?: continue
                loadedBonsais += bonsai
                slugsById[bonsai.id] = slug
            }
            bonsais.value = loadedBonsais
            isLoaded = true
        }
    }
}
