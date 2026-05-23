package fr.zomzog.mylittlebonsai.ui.foldersetup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import fr.zomzog.mylittlebonsai.data.AndroidFolderStorageManager
import fr.zomzog.mylittlebonsai.domain.FolderStorageManager

@Composable
actual fun rememberFolderPickerLauncher(
    storageManager: FolderStorageManager,
    onGranted: () -> Unit,
    onDenied: () -> Unit,
): FolderPickerLauncher {
    // Only wire up the real SAF picker when running with the real Android manager.
    // Any other implementation (e.g. test doubles in Android instrumented tests) auto-grants.
    if (storageManager !is AndroidFolderStorageManager) {
        return remember { object : FolderPickerLauncher { override fun launch() = onGranted() } }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
    ) { uri ->
        if (uri != null) {
            storageManager.persistFolder(uri)
            onGranted()
        } else {
            onDenied()
        }
    }
    return remember { object : FolderPickerLauncher { override fun launch() = launcher.launch(null) } }
}

@Composable
actual fun rememberFolderStorageManager(provided: FolderStorageManager?): FolderStorageManager {
    val context = LocalContext.current
    return remember(provided) { provided ?: AndroidFolderStorageManager(context) }
}
