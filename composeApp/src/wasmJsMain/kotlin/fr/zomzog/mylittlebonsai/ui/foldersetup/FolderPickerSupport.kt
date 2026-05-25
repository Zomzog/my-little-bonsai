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
    val scope = rememberCoroutineScope()
    return remember {
        object : FolderPickerLauncher {
            override fun launch() {
                when (storageManager) {
                    is WebFolderStorageManager -> scope.launch {
                        if (storageManager.pickFolder()) onGranted() else onDenied()
                    }
                    // Any other implementation (e.g. test doubles) auto-grants so tests can
                    // exercise the post-grant navigation path without touching real browser APIs.
                    else -> onGranted()
                }
            }
        }
    }
}

@Composable
actual fun rememberFolderStorageManager(provided: FolderStorageManager?): FolderStorageManager =
    remember { provided ?: WebFolderStorageManager() }
