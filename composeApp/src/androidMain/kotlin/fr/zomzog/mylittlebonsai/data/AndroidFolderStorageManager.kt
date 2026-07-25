package fr.zomzog.mylittlebonsai.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import fr.zomzog.mylittlebonsai.domain.FolderStorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

private const val PREFS_NAME = "bonsai_storage"
private const val KEY_FOLDER_URI = "folder_uri"
private const val METADATA_FILENAME = "metadata.yaml"
private const val METADATA_MIME = "text/x-yaml"

/**
 * Android implementation that stores bonsai data in a user-chosen folder via the
 * Storage Access Framework (SAF).  The folder URI is persisted in SharedPreferences
 * and the OS persistent-URI-permission is taken so the app can access the folder
 * across restarts without prompting again.
 */
class AndroidFolderStorageManager(private val context: Context) : FolderStorageManager {

    private val prefs by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    override suspend fun hasStorageAccess(): Boolean = withContext(Dispatchers.IO) {
        val uriString = prefs.getString(KEY_FOLDER_URI, null) ?: return@withContext false
        val uri = Uri.parse(uriString)
        context.contentResolver.persistedUriPermissions.any { permission ->
            permission.uri == uri && permission.isReadPermission && permission.isWritePermission
        }
    }

    /**
     * Called by the Android folder-picker actual after the user selects a folder.
     * Takes a persistent permission and stores the URI.
     */
    fun persistFolder(uri: Uri) {
        context.contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
        )
        prefs.edit().putString(KEY_FOLDER_URI, uri.toString()).apply()
    }

    override suspend fun createMetadataFile() = withContext(Dispatchers.IO) {
        val uriString = prefs.getString(KEY_FOLDER_URI, null) ?: return@withContext
        val treeUri = Uri.parse(uriString)
        val docId = DocumentsContract.getTreeDocumentId(treeUri)
        val baseDocUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)

        // Check whether metadata.yaml already exists to preserve the original creationDate.
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, docId)
        val alreadyExists = context.contentResolver.query(
            childrenUri,
            arrayOf(DocumentsContract.Document.COLUMN_DISPLAY_NAME),
            null,
            null,
            null,
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                if (cursor.getString(0) == METADATA_FILENAME) return@use true
            }
            false
        } ?: false

        if (!alreadyExists) {
            val fileUri = DocumentsContract.createDocument(
                context.contentResolver,
                baseDocUri,
                METADATA_MIME,
                METADATA_FILENAME,
            ) ?: return@withContext

            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            context.contentResolver.openOutputStream(fileUri)?.use { stream ->
                stream.write("creationDate: $today\n".toByteArray(Charsets.UTF_8))
            }
        }
    }
}
