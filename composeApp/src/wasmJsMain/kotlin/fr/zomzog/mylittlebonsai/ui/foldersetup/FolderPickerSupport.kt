package fr.zomzog.mylittlebonsai.ui.foldersetup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import fr.zomzog.mylittlebonsai.data.WebFolderStorageManager
import fr.zomzog.mylittlebonsai.domain.FolderStorageManager
import kotlinx.coroutines.launch

@Composable
actual fun rememberFolderPickerLauncher(
    storageManager: FolderStorageManager,
    onGranted: () -> Unit,
    onDenied: () -> Unit,
): FolderPickerLauncher {
    val webManager = storageManager as WebFolderStorageManager
    val scope = rememberCoroutineScope()
    return remember {
        object : FolderPickerLauncher {
            override fun launch() {
                scope.launch {
                    if (webManager.pickFolder()) {
                        onGranted()
                    } else {
                        onDenied()
                    }
                }
            }
        }
    }
}

@Composable
actual fun rememberFolderStorageManager(provided: FolderStorageManager?): FolderStorageManager =
    remember { provided ?: WebFolderStorageManager() }
