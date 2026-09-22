package fr.zomzog.mylittlebonsai.ui.foldersetup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import fr.zomzog.mylittlebonsai.data.BrowserStorageManager
import fr.zomzog.mylittlebonsai.domain.FolderStorageManager

/**
 * Web never shows the folder-setup screen — [BrowserStorageManager] always reports
 * access — so the launcher only has to satisfy the shared contract.
 */
@Composable
actual fun rememberFolderPickerLauncher(
    storageManager: FolderStorageManager,
    onGranted: () -> Unit,
    onDenied: () -> Unit,
): FolderPickerLauncher = remember {
    object : FolderPickerLauncher {
        override fun launch() = onGranted()
    }
}

@Composable
actual fun rememberFolderStorageManager(provided: FolderStorageManager?): FolderStorageManager =
    remember { provided ?: BrowserStorageManager() }
