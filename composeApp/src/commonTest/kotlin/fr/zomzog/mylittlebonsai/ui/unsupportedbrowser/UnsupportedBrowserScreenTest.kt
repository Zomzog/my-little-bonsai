package fr.zomzog.mylittlebonsai.ui.unsupportedbrowser

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertTrue

// Compose resource strings load asynchronously on wasmJs (unlike the JVM test
// target, where they resolve synchronously from the classpath), so these must
// match `composeResources/values/strings.xml` literally rather than being
// read back via stringResource() inside the test's own composition.
private const val TITLE = "This browser isn't supported"
private const val EXPLANATION =
    "My Little Bonsai stores your data in a real folder on your device. That requires a " +
        "browser feature (the File System Access API) that this browser does not provide."
private const val SUPPORTED_BODY = "Chrome, Edge, and other Chromium-based desktop browsers."
private const val MOBILE_APP_LINK = "Get the mobile app instead"

@OptIn(ExperimentalTestApi::class)
class UnsupportedBrowserScreenTest {

    @Test
    fun showsTitleExplanationAndSupportedBrowsers() = runComposeUiTest {
        setContent { UnsupportedBrowserScreen(onOpenMobileApp = {}) }
        waitForIdle()

        onNodeWithText(TITLE).assertExists()
        onNodeWithText(EXPLANATION).assertExists()
        onNodeWithText(SUPPORTED_BODY).assertExists()
    }

    @Test
    fun showsMobileAppLink() = runComposeUiTest {
        setContent { UnsupportedBrowserScreen(onOpenMobileApp = {}) }
        waitForIdle()

        onNodeWithText(MOBILE_APP_LINK).assertExists()
    }

    @Test
    fun clickingMobileAppLinkInvokesCallback() = runComposeUiTest {
        var clicked = false
        setContent { UnsupportedBrowserScreen(onOpenMobileApp = { clicked = true }) }
        waitForIdle()

        onNodeWithText(MOBILE_APP_LINK).performClick()
        waitForIdle()
        assertTrue(clicked)
    }
}
