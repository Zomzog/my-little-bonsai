package fr.zomzog.mylittlebonsai.data

import fr.zomzog.mylittlebonsai.domain.FolderStorageManager
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

const val CREATION_DATE_STORAGE_KEY = "bonsai.creationDate"

/**
 * Web implementation of [FolderStorageManager] backed by browser storage.
 *
 * Browser storage needs no user grant, so access is always available and the
 * folder-setup onboarding never appears on Web. This replaces the File System
 * Access API flow, whose permission does not survive a page reload — the browser
 * downgrades it to `'prompt'`, which forced the user to re-pick a folder on every
 * visit.
 */
class BrowserStorageManager(
    private val store: KeyValueStore = LocalStorageKeyValueStore(),
) : FolderStorageManager {

    /** Also records the vault creation date on the very first visit. */
    override suspend fun hasStorageAccess(): Boolean {
        createMetadataFile()
        return true
    }

    override suspend fun createMetadataFile() {
        if (store.read(CREATION_DATE_STORAGE_KEY) != null) return
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        store.write(CREATION_DATE_STORAGE_KEY, today.toString())
    }
}
