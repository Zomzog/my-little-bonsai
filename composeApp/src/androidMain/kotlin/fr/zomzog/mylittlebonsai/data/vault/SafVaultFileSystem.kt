package fr.zomzog.mylittlebonsai.data.vault

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract

/**
 * [DocumentTree] over a SAF tree (`DocumentsContract`), rather than the
 * `androidx.documentfile` convenience library — `DocumentFile.listFiles()` issues one
 * query per child for its metadata, which is too slow for a vault with hundreds of
 * bonsais and thousands of sessions (the issue's own stated risk); batched
 * `ContentResolver.query` against `buildChildDocumentsUriUsingTree` avoids that.
 *
 * This class only translates to/from `ContentResolver`/`DocumentsContract`; the
 * document-id caching, the tmp-then-rename write and the overwrite/null-stream handling
 * all live in [DocumentTreeVaultFileSystem], which is exercised in commonTest against a
 * fake [DocumentTree] since this class itself needs a real SAF provider to test.
 */
private class AndroidDocumentTree(
    private val context: Context,
    private val treeUri: Uri,
) : DocumentTree {

    private val resolver get() = context.contentResolver

    override fun rootDocumentId(): String = DocumentsContract.getTreeDocumentId(treeUri)

    override fun queryChildren(parentId: String): List<DocumentRow> {
        val rows = mutableListOf<DocumentRow>()
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
            while (cursor.moveToNext()) {
                rows += DocumentRow(cursor.getString(0), cursor.getString(1), cursor.getString(2))
            }
        }
        return rows
    }

    override fun createDocument(parentId: String, mimeType: String, name: String): String? {
        val parentUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, parentId)
        val createdUri = DocumentsContract.createDocument(resolver, parentUri, mimeType, name) ?: return null
        return DocumentsContract.getDocumentId(createdUri)
    }

    override fun renameDocument(documentId: String, newName: String): String? {
        val uri = DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId)
        val renamedUri = DocumentsContract.renameDocument(resolver, uri, newName) ?: return null
        return DocumentsContract.getDocumentId(renamedUri)
    }

    override fun deleteDocument(documentId: String): Boolean {
        val uri = DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId)
        return DocumentsContract.deleteDocument(resolver, uri)
    }

    override fun readBytes(documentId: String): ByteArray? {
        val uri = DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId)
        return resolver.openInputStream(uri)?.use { it.readBytes() }
    }

    override fun writeBytes(documentId: String, bytes: ByteArray): Boolean {
        val uri = DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId)
        return resolver.openOutputStream(uri)?.use {
            it.write(bytes)
            true
        } ?: false
    }
}

/** [VaultFileSystem] over a SAF tree rooted at [treeUri]. */
fun SafVaultFileSystem(context: Context, treeUri: Uri): VaultFileSystem =
    DocumentTreeVaultFileSystem(AndroidDocumentTree(context, treeUri))
