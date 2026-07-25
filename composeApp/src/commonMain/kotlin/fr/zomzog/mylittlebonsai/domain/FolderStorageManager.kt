package fr.zomzog.mylittlebonsai.domain

interface FolderStorageManager {
    suspend fun hasStorageAccess(): Boolean
    suspend fun createMetadataFile()

    /**
     * Whether this platform can actually present a folder picker to the user.
     *
     * Web returns `false` when the browser disables the File System Access API
     * (e.g. Brave, which hides `window.showDirectoryPicker` behind a flag by
     * default — turning off the ad-blocker/Shields does not re-enable it).
     * All other platforms always support the picker.
     */
    fun isFolderPickerSupported(): Boolean = true
}
