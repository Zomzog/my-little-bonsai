package fr.zomzog.mylittlebonsai.data.vault

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.DocumentsContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val MIME_TYPE_MARKDOWN = "text/markdown"

/**
 * [VaultFileSystem] over a SAF tree (`DocumentsContract`), rather than the
 * `androidx.documentfile` convenience library — `DocumentFile.listFiles()` issues one
 * query per child for its metadata, which is too slow for a vault with hundreds of
 * bonsais and thousands of sessions (the issue's own stated risk); batched
 * `ContentResolver.query` against `buildChildDocumentsUriUsingTree` avoids that.
 *
 * Resolved document ids are cached per relative path for the lifetime of this instance,
 * since SAF has no path-based lookup and walking the tree by display name is what's slow.
 */
class SafVaultFileSystem(
    private val context: Context,
    private val treeUri: Uri,
) : VaultFileSystem {

    private val resolver get() = context.contentResolver
    private val documentIdCache = mutableMapOf<String, String>()

    override suspend fun listDirectories(path: String): List<String> = withContext(Dispatchers.IO) {
        listChildren(path) { mimeType -> mimeType == DocumentsContract.Document.MIME_TYPE_DIR }
    }

    override suspend fun listFiles(path: String): List<String> = withContext(Dispatchers.IO) {
        listChildren(path) { mimeType -> mimeType != DocumentsContract.Document.MIME_TYPE_DIR }
    }

    override suspend fun readText(path: String): String = withContext(Dispatchers.IO) {
        val documentId = resolveDocumentId(path, create = false) ?: error("Vault file not found: $path")
        val uri = DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId)
        resolver.openInputStream(uri)?.use { it.readBytes().toString(Charsets.UTF_8) }
            ?: error("Could not open vault file: $path")
    }

    override suspend fun writeTextAtomic(path: String, content: String) = withContext(Dispatchers.IO) {
        val parentPath = path.substringBeforeLast('/', "")
        val name = path.substringAfterLast('/')
        val parentId = resolveDocumentId(parentPath, create = true)
            ?: error("Could not create vault folder: $parentPath")
        val parentUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, parentId)

        // A leftover temp file from an interrupted previous write would otherwise make
        // createDocument pick a different (suffixed) name instead of reusing this one.
        // Keeping the real extension at the end (rather than appending ".tmp" after it)
        // keeps the name's extension consistent with MIME_TYPE_MARKDOWN, since some
        // providers (FileUtils.buildUniqueFile) append the MIME type's own extension
        // whenever the given name's extension doesn't already map to it.
        val tmpName = ".tmp-$name"
        deleteChildIfExists(parentId, tmpName)
        val tmpUri = DocumentsContract.createDocument(resolver, parentUri, MIME_TYPE_MARKDOWN, tmpName)
            ?: error("Could not create vault file: $path")

        val wrote = resolver.openOutputStream(tmpUri)?.use {
            it.write(content.toByteArray(Charsets.UTF_8))
            true
        } ?: false
        if (!wrote) {
            deleteDocumentQuietly(tmpUri)
            error("Could not write vault file: $path (no output stream)")
        }

        // DocumentsContract.renameDocument cannot overwrite an existing document of the
        // target name — on at least some providers it returns null rather than replacing
        // it — so remove any existing file at `name` first. The temp file above is fully
        // written before this point, so there is only a brief window with neither name
        // present, never a window with partial content under the real name.
        deleteChildIfExists(parentId, name)
        val renamedUri = DocumentsContract.renameDocument(resolver, tmpUri, name)
        if (renamedUri == null) {
            deleteDocumentQuietly(tmpUri)
            error("Could not finish writing vault file: $path (rename failed)")
        }
        documentIdCache.remove(path)
        Unit
    }

    private fun deleteDocumentQuietly(uri: Uri) {
        runCatching { DocumentsContract.deleteDocument(resolver, uri) }
    }

    private fun listChildren(path: String, predicate: (String) -> Boolean): List<String> {
        val documentId = resolveDocumentId(path, create = false) ?: return emptyList()
        val names = mutableListOf<String>()
        queryChildren(documentId) { cursor ->
            val childId = cursor.getString(0)
            val childName = cursor.getString(1)
            val mimeType = cursor.getString(2)
            documentIdCache[if (path.isEmpty()) childName else "$path/$childName"] = childId
            if (predicate(mimeType)) names += childName
        }
        return names
    }

    private fun findChildDocumentId(parentId: String, name: String): String? {
        var found: String? = null
        queryChildren(parentId) { cursor ->
            if (found == null && cursor.getString(1) == name) found = cursor.getString(0)
        }
        return found
    }

    /** Rows carry `(documentId, displayName, mimeType)` at indices `0, 1, 2`. */
    private inline fun queryChildren(parentId: String, onRow: (Cursor) -> Unit) {
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, parentId)
        resolver.query(
            childrenUri,
            arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_MIME_TYPE,
            ),
            null,
            null,
            null,
        )?.use { cursor ->
            while (cursor.moveToNext()) onRow(cursor)
        }
    }

    private fun deleteChildIfExists(parentId: String, name: String) {
        val id = findChildDocumentId(parentId, name) ?: return
        DocumentsContract.deleteDocument(resolver, DocumentsContract.buildDocumentUriUsingTree(treeUri, id))
    }

    private fun resolveDocumentId(path: String, create: Boolean): String? {
        if (path.isEmpty()) return DocumentsContract.getTreeDocumentId(treeUri)
        documentIdCache[path]?.let { return it }
        val parentPath = path.substringBeforeLast('/', "")
        val name = path.substringAfterLast('/')
        val parentId = resolveDocumentId(parentPath, create) ?: return null
        val existing = findChildDocumentId(parentId, name)
        val resolved = existing ?: if (create) {
            val parentUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, parentId)
            DocumentsContract.createDocument(resolver, parentUri, DocumentsContract.Document.MIME_TYPE_DIR, name)
                ?.let { DocumentsContract.getDocumentId(it) }
        } else {
            null
        }
        if (resolved != null) documentIdCache[path] = resolved
        return resolved
    }
}
