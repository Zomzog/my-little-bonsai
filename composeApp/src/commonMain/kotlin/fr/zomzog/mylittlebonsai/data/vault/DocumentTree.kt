package fr.zomzog.mylittlebonsai.data.vault

/**
 * A minimal, testable abstraction over a SAF-shaped document tree: children are resolved
 * by id under a parent id rather than by path, and I/O is byte-level. The Android
 * `ContentResolver`/`DocumentsContract` adapter lives in androidMain
 * (`AndroidDocumentTree`); [DocumentTreeVaultFileSystem] implements [VaultFileSystem]
 * against this interface so that logic can be exercised here in commonTest against a fake
 * instead of needing an on-device test.
 */
interface DocumentTree {
    fun rootDocumentId(): String
    fun queryChildren(parentId: String): List<DocumentRow>
    fun createDocument(parentId: String, mimeType: String, name: String): String?
    fun renameDocument(documentId: String, newName: String): String?
    fun deleteDocument(documentId: String): Boolean
    fun readBytes(documentId: String): ByteArray?
    fun writeBytes(documentId: String, bytes: ByteArray): Boolean
}

data class DocumentRow(val documentId: String, val name: String, val mimeType: String)

/** `DocumentsContract.Document.MIME_TYPE_DIR`'s value, duplicated here to stay platform-agnostic. */
internal const val DOCUMENT_MIME_TYPE_DIR = "vnd.android.document/directory"
