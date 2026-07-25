package fr.zomzog.mylittlebonsai.ui.foldersetup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import fr.zomzog.mylittlebonsai.data.JvmFolderStorageManager
import fr.zomzog.mylittlebonsai.domain.FolderStorageManager

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
    remember { provided ?: JvmFolderStorageManager() }
