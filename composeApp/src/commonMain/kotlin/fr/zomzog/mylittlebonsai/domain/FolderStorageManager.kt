package fr.zomzog.mylittlebonsai.domain

interface FolderStorageManager {
    suspend fun hasStorageAccess(): Boolean
    suspend fun createMetadataFile()

    /**
     * Whether this platform asks the user to choose a storage folder.
     *
     * Android presents the Storage Access Framework picker. Web stores data in the
     * browser instead, so it never reaches the folder-setup screen and this value
     * is unused there.
     */
    fun isFolderPickerSupported(): Boolean = true
}
