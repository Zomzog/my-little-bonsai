package fr.zomzog.mylittlebonsai.ui.foldersetup

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import fr.zomzog.mylittlebonsai.FakeFolderStorageManager
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class FolderSetupScreenTest {

    @Test
    fun folderSetupScreenShowsTitle() = runComposeUiTest {
        val manager = FakeFolderStorageManager(hasAccess = false)
        setContent { FolderSetupScreen(storageManager = manager, onFolderGranted = {}) }
        onNodeWithText(FOLDER_SETUP_TITLE).assertExists()
    }

    @Test
    fun folderSetupScreenShowsPrivacyHeading() = runComposeUiTest {
        val manager = FakeFolderStorageManager(hasAccess = false)
        setContent { FolderSetupScreen(storageManager = manager, onFolderGranted = {}) }
        onNodeWithText(FOLDER_SETUP_PRIVACY_HEADING).assertExists()
    }

    @Test
    fun folderSetupScreenShowsPrivacyBody() = runComposeUiTest {
        val manager = FakeFolderStorageManager(hasAccess = false)
        setContent { FolderSetupScreen(storageManager = manager, onFolderGranted = {}) }
        onNodeWithText(FOLDER_SETUP_PRIVACY_BODY).assertExists()
    }

    @Test
    fun folderSetupScreenShowsFolderHeading() = runComposeUiTest {
        val manager = FakeFolderStorageManager(hasAccess = false)
        setContent { FolderSetupScreen(storageManager = manager, onFolderGranted = {}) }
        onNodeWithText(FOLDER_SETUP_FOLDER_HEADING).assertExists()
    }

    @Test
    fun folderSetupScreenShowsFolderBody() = runComposeUiTest {
        val manager = FakeFolderStorageManager(hasAccess = false)
        setContent { FolderSetupScreen(storageManager = manager, onFolderGranted = {}) }
        onNodeWithText(FOLDER_SETUP_FOLDER_BODY).assertExists()
    }

    @Test
    fun folderSetupScreenShowsChooseFolderButton() = runComposeUiTest {
        val manager = FakeFolderStorageManager(hasAccess = false)
        setContent { FolderSetupScreen(storageManager = manager, onFolderGranted = {}) }
        onNodeWithText(FOLDER_SETUP_BUTTON).assertExists()
        onNodeWithText(FOLDER_SETUP_BUTTON).assertIsEnabled()
    }

    @Test
    fun clickingChooseFolderButtonCallsOnFolderGranted() = runComposeUiTest {
        val manager = FakeFolderStorageManager(hasAccess = false)
        var granted = false
        setContent {
            FolderSetupScreen(
                storageManager = manager,
                onFolderGranted = { granted = true },
            )
        }
        onNodeWithText(FOLDER_SETUP_BUTTON).performClick()
        waitForIdle()
        assertTrue(granted)
    }

    @Test
    fun clickingChooseFolderButtonCreatesMetadataFile() = runComposeUiTest {
        val manager = FakeFolderStorageManager(hasAccess = false)
        setContent {
            FolderSetupScreen(
                storageManager = manager,
                onFolderGranted = {},
            )
        }
        onNodeWithText(FOLDER_SETUP_BUTTON).performClick()
        waitForIdle()
        assertTrue(manager.metadataCreated)
    }
}
