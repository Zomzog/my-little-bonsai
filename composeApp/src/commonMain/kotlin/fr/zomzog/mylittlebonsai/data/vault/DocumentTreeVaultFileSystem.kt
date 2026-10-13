package fr.zomzog.mylittlebonsai.data.vault

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val MIME_TYPE_MARKDOWN = "text/markdown"

/**
 * [VaultFileSystem] over a [DocumentTree]. Resolved document ids are cached per
 * vault-relative path for the lifetime of this instance, since this kind of tree has no
 * path-based lookup and walking it by display name is slow.
 */
class DocumentTreeVaultFileSystem(private val tree: DocumentTree) : VaultFileSystem {

    private val documentIdCache = mutableMapOf<String, String>()

    override suspend fun listDirectories(path: String): List<String> = withContext(Dispatchers.IO) {
        listChildren(path) { mimeType -> mimeType == DOCUMENT_MIME_TYPE_DIR }
    }

    override suspend fun listFiles(path: String): List<String> = withContext(Dispatchers.IO) {
        listChildren(path) { mimeType -> mimeType != DOCUMENT_MIME_TYPE_DIR }
    }

    override suspend fun readText(path: String): String = withContext(Dispatchers.IO) {
        val documentId = resolveDocumentId(path, create = false) ?: error("Vault file not found: $path")
        tree.readBytes(documentId)?.toString(Charsets.UTF_8) ?: error("Could not open vault file: $path")
    }

    override suspend fun writeTextAtomic(path: String, content: String) = withContext(Dispatchers.IO) {
        val parentPath = path.substringBeforeLast('/', "")
        val name = path.substringAfterLast('/')
        val parentId = resolveDocumentId(parentPath, create = true)
            ?: error("Could not create vault folder: $parentPath")

        // A leftover temp file from an interrupted previous write would otherwise make
        // createDocument pick a different (suffixed) name instead of reusing this one.
        // Keeping the real extension at the end (rather than appending ".tmp" after it)
        // keeps the name's extension consistent with MIME_TYPE_MARKDOWN, since some
        // providers (FileUtils.buildUniqueFile) append the MIME type's own extension
        // whenever the given name's extension doesn't already map to it.
        val tmpName = ".tmp-$name"
        deleteChildIfExists(parentId, tmpName)
        val tmpId = tree.createDocument(parentId, MIME_TYPE_MARKDOWN, tmpName)
            ?: error("Could not create vault file: $path")

        val wrote = tree.writeBytes(tmpId, content.toByteArray(Charsets.UTF_8))
        if (!wrote) {
            deleteDocumentQuietly(tmpId)
            error("Could not write vault file: $path (no output stream)")
        }

        // Renaming cannot overwrite an existing document of the target name — on at least
        // some providers it returns null rather than replacing it — so remove any existing
        // file at `name` first. The temp file above is fully written before this point, so
        // there is only a brief window with neither name present, never a window with
        // partial content under the real name.
        deleteChildIfExists(parentId, name)
        val renamedId = tree.renameDocument(tmpId, name)
        if (renamedId == null) {
            deleteDocumentQuietly(tmpId)
            error("Could not finish writing vault file: $path (rename failed)")
        }
        documentIdCache.remove(path)
        Unit
    }

    private fun deleteDocumentQuietly(documentId: String) {
        runCatching { tree.deleteDocument(documentId) }
    }

    private fun listChildren(path: String, predicate: (String) -> Boolean): List<String> {
        val documentId = resolveDocumentId(path, create = false) ?: return emptyList()
        val names = mutableListOf<String>()
        for (row in tree.queryChildren(documentId)) {
            documentIdCache[if (path.isEmpty()) row.name else "$path/${row.name}"] = row.documentId
            if (predicate(row.mimeType)) names += row.name
        }
        return names
    }

    private fun findChildDocumentId(parentId: String, name: String): String? =
        tree.queryChildren(parentId).firstOrNull { it.name == name }?.documentId

    private fun deleteChildIfExists(parentId: String, name: String) {
        val id = findChildDocumentId(parentId, name) ?: return
        tree.deleteDocument(id)
    }

    private fun resolveDocumentId(path: String, create: Boolean): String? {
        if (path.isEmpty()) return tree.rootDocumentId()
        documentIdCache[path]?.let { return it }
        val parentPath = path.substringBeforeLast('/', "")
        val name = path.substringAfterLast('/')
        val parentId = resolveDocumentId(parentPath, create) ?: return null
        val existing = findChildDocumentId(parentId, name)
        val resolved = existing ?: if (create) {
            tree.createDocument(parentId, DOCUMENT_MIME_TYPE_DIR, name)
        } else {
            null
        }
        if (resolved != null) documentIdCache[path] = resolved
        return resolved
    }
}
