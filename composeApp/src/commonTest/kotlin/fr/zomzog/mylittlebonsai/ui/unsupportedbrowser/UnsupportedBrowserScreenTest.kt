package fr.zomzog.mylittlebonsai.ui.unsupportedbrowser

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertTrue

// Compose resource strings load asynchronously on wasmJs (a real fetch, unlike
// the JVM test target which resolves them synchronously from the classpath),
// so tests poll for the text via waitUntil rather than a single waitForIdle().
// These must match `composeResources/values/strings.xml` literally.
private const val TITLE = "This browser isn't supported"
private const val EXPLANATION =
    "My Little Bonsai stores your data in a real folder on your device. That requires a " +
        "browser feature (the File System Access API) that this browser does not provide."
private const val SUPPORTED_BODY = "Chrome, Edge, and other Chromium-based desktop browsers."
private const val MOBILE_APP_LINK = "Get the mobile app instead"
private const val LOAD_TIMEOUT_MILLIS = 10_000L

@OptIn(ExperimentalTestApi::class)
class UnsupportedBrowserScreenTest {

    @Test
    fun showsTitleExplanationAndSupportedBrowsers() = runComposeUiTest {
        setContent { UnsupportedBrowserScreen(onOpenMobileApp = {}) }
        waitUntil(LOAD_TIMEOUT_MILLIS) {
            onAllNodesWithText(TITLE).fetchSemanticsNodes().isNotEmpty()
        }

        onNodeWithText(TITLE).assertExists()
        onNodeWithText(EXPLANATION).assertExists()
        onNodeWithText(SUPPORTED_BODY).assertExists()
    }

    @Test
    fun showsMobileAppLink() = runComposeUiTest {
        setContent { UnsupportedBrowserScreen(onOpenMobileApp = {}) }
        waitUntil(LOAD_TIMEOUT_MILLIS) {
            onAllNodesWithText(MOBILE_APP_LINK).fetchSemanticsNodes().isNotEmpty()
        }

        onNodeWithText(MOBILE_APP_LINK).assertExists()
    }

    @Test
    fun clickingMobileAppLinkInvokesCallback() = runComposeUiTest {
        var clicked = false
        setContent { UnsupportedBrowserScreen(onOpenMobileApp = { clicked = true }) }
        waitUntil(LOAD_TIMEOUT_MILLIS) {
            onAllNodesWithText(MOBILE_APP_LINK).fetchSemanticsNodes().isNotEmpty()
        }

        onNodeWithText(MOBILE_APP_LINK).performClick()
        waitForIdle()
        assertTrue(clicked)
    }
}
