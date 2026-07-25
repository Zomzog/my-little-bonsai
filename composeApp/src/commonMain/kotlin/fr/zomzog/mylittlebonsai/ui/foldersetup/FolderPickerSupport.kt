package fr.zomzog.mylittlebonsai.ui.foldersetup

import androidx.compose.runtime.Composable
import fr.zomzog.mylittlebonsai.domain.FolderStorageManager

/**
 * Opaque handle that lets the UI trigger the platform folder picker without knowing
 * platform-specific details.
 */
interface FolderPickerLauncher {
    fun launch()
}

/**
 * Creates and remembers a [FolderPickerLauncher].
 *
 * On Android this registers an `ActivityResultLauncher` for `OpenDocumentTree`.
 * On Web this calls `window.showDirectoryPicker()`.
 * On JVM (tests) this immediately calls [onGranted].
 *
 * [onGranted] is invoked after the platform has persisted the folder reference and is safe to
 * call `storageManager.createMetadataFile()` from.
 */
@Composable
expect fun rememberFolderPickerLauncher(
    storageManager: FolderStorageManager,
    onGranted: () -> Unit,
    onDenied: () -> Unit = {},
): FolderPickerLauncher

/**
 * Returns a remembered [FolderStorageManager] for the current platform.
 * When [provided] is non-null it is used as-is (useful in tests and for explicit DI).
 * Otherwise a platform-appropriate default is created.
 */
@Composable
expect fun rememberFolderStorageManager(provided: FolderStorageManager?): FolderStorageManager
