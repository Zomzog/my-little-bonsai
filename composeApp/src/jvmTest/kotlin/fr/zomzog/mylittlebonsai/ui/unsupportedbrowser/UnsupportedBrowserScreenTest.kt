package fr.zomzog.mylittlebonsai.ui.unsupportedbrowser

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertTrue

// Compose string resources only resolve synchronously (from the classpath) on
// the jvm target, so this lives in jvmTest rather than commonTest — on
// wasmJs the same lookup is a real async fetch that a Compose UI test's
// idling/waitUntil isn't guaranteed to wait out. These literals must match
// `composeResources/values/strings.xml`.
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

        onNodeWithText(TITLE).assertExists()
        onNodeWithText(EXPLANATION).assertExists()
        onNodeWithText(SUPPORTED_BODY).assertExists()
    }

    @Test
    fun showsMobileAppLink() = runComposeUiTest {
        setContent { UnsupportedBrowserScreen(onOpenMobileApp = {}) }

        onNodeWithText(MOBILE_APP_LINK).assertExists()
    }

    @Test
    fun clickingMobileAppLinkInvokesCallback() = runComposeUiTest {
        var clicked = false
        setContent { UnsupportedBrowserScreen(onOpenMobileApp = { clicked = true }) }

        onNodeWithText(MOBILE_APP_LINK).performClick()
        waitForIdle()
        assertTrue(clicked)
    }
}
