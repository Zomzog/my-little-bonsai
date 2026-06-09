package fr.zomzog.mylittlebonsai.ui.foldersetup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.zomzog.mylittlebonsai.domain.FolderStorageManager
import kotlinx.coroutines.launch

const val FOLDER_SETUP_TITLE = "Choose a storage folder"
const val FOLDER_SETUP_PRIVACY_HEADING = "Your data stays on your device"
const val FOLDER_SETUP_PRIVACY_BODY =
    "Everything you save in My Little Bonsai is stored exclusively in a folder " +
        "you choose on this device. Nothing is uploaded to the internet. " +
        "Your bonsai records are completely private."
const val FOLDER_SETUP_FOLDER_HEADING = "Pick a folder"
const val FOLDER_SETUP_FOLDER_BODY =
    "Select an existing folder that was previously used with this app, " +
        "or any empty folder where your data should be stored."
const val FOLDER_SETUP_BUTTON = "Choose Folder"
const val FOLDER_SETUP_UNSUPPORTED_MESSAGE =
    "This browser blocks folder access, so the picker can't open. In Brave, open " +
        "brave://flags/#file-system-access-api, enable \"File System Access API\", and reload " +
        "this page — disabling Shields or the ad-blocker is not enough. Chrome and Edge work " +
        "without any changes."

@Composable
fun FolderSetupScreen(
    storageManager: FolderStorageManager,
    onFolderGranted: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val launcher = rememberFolderPickerLauncher(
        storageManager = storageManager,
        onGranted = {
            scope.launch {
                storageManager.createMetadataFile()
                onFolderGranted()
            }
        },
    )

    FolderSetupContent(
        errorMessage = errorMessage,
        onChooseFolder = {
            if (storageManager.isFolderPickerSupported()) {
                errorMessage = null
                launcher.launch()
            } else {
                errorMessage = FOLDER_SETUP_UNSUPPORTED_MESSAGE
            }
        },
        modifier = modifier,
    )
}

@Composable
internal fun FolderSetupContent(
    errorMessage: String?,
    onChooseFolder: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = FOLDER_SETUP_TITLE,
            style = MaterialTheme.typography.headlineMedium,
        )

        Spacer(Modifier.height(32.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = FOLDER_SETUP_PRIVACY_HEADING,
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = FOLDER_SETUP_PRIVACY_BODY,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = FOLDER_SETUP_FOLDER_HEADING,
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = FOLDER_SETUP_FOLDER_BODY,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        Spacer(Modifier.height(40.dp))

        Button(
            onClick = onChooseFolder,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(FOLDER_SETUP_BUTTON)
        }

        if (errorMessage != null) {
            Spacer(Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                ),
            ) {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
    }
}
