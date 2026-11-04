package fr.zomzog.mylittlebonsai.data.vault

/** An in-memory [DocumentTree], for testing [DocumentTreeVaultFileSystem] without a real SAF provider. */
class FakeDocumentTree : DocumentTree {

    private val rootId = "root"
    private var nextId = 1
    private val childrenOf = mutableMapOf(rootId to mutableListOf<DocumentRow>())
    private val bytesById = mutableMapOf<String, ByteArray>()

    var queryCount = 0
        private set

    /** Simulates a provider whose rename cannot overwrite an existing target name. */
    var renameCanOverwrite = true

    /** Simulates `openOutputStream` returning null (e.g. a provider refusing the write). */
    var failNextWrite = false

    override fun rootDocumentId(): String = rootId

    override fun queryChildren(parentId: String): List<DocumentRow> {
        queryCount++
        return childrenOf[parentId].orEmpty()
    }

    override fun createDocument(parentId: String, mimeType: String, name: String): String? {
        val id = "doc-${nextId++}"
        childrenOf.getOrPut(parentId) { mutableListOf() } += DocumentRow(id, name, mimeType)
        if (mimeType == DOCUMENT_MIME_TYPE_DIR) childrenOf.getOrPut(id) { mutableListOf() }
        return id
    }

    override fun renameDocument(documentId: String, newName: String): String? {
        val (parentId, row) = findRow(documentId) ?: return null
        val siblings = childrenOf.getValue(parentId)
        if (!renameCanOverwrite && siblings.any { it.name == newName && it.documentId != documentId }) return null
        siblings[siblings.indexOf(row)] = row.copy(name = newName)
        return documentId
    }

    override fun deleteDocument(documentId: String): Boolean {
        val (parentId, row) = findRow(documentId) ?: return false
        childrenOf.getValue(parentId).remove(row)
        bytesById.remove(documentId)
        return true
    }

    override fun readBytes(documentId: String): ByteArray? = bytesById[documentId]

    override fun writeBytes(documentId: String, bytes: ByteArray): Boolean {
        if (failNextWrite) {
            failNextWrite = false
            return false
        }
        bytesById[documentId] = bytes
        return true
    }

    private fun findRow(documentId: String): Pair<String, DocumentRow>? {
        for ((parentId, rows) in childrenOf) {
            rows.firstOrNull { it.documentId == documentId }?.let { return parentId to it }
        }
        return null
    }
}
