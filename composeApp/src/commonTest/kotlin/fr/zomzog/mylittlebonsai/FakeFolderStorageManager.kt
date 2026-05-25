package fr.zomzog.mylittlebonsai

import fr.zomzog.mylittlebonsai.domain.FolderStorageManager

/**
 * Test double for [FolderStorageManager].
 *
 * @param hasAccess initial value returned by [hasStorageAccess].
 *   Set to `false` to exercise the folder-setup onboarding flow.
 *   Set to `true` (default) to bypass onboarding and test bonsai-management screens.
 */
class FakeFolderStorageManager(private val hasAccess: Boolean = true) : FolderStorageManager {
    var metadataCreated = false
        private set

    override suspend fun hasStorageAccess(): Boolean = hasAccess
    override suspend fun createMetadataFile() {
        metadataCreated = true
    }
}
