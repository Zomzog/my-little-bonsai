package fr.zomzog.mylittlebonsai.ui.unsupportedbrowser

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class UnsupportedBrowserScreenTest {

    @Test
    fun englishLanguageTagShowsEnglishText() = runComposeUiTest {
        setContent { UnsupportedBrowserScreen(languageTag = "en-US", onOpenMobileApp = {}) }
        onNodeWithText(UNSUPPORTED_BROWSER_STRINGS_EN.title).assertExists()
        onNodeWithText(UNSUPPORTED_BROWSER_STRINGS_EN.explanation).assertExists()
        onNodeWithText(UNSUPPORTED_BROWSER_STRINGS_EN.supportedBrowsersBody).assertExists()
        onNodeWithText(UNSUPPORTED_BROWSER_STRINGS_EN.mobileAppLink).assertExists()
    }

    @Test
    fun frenchLanguageTagShowsFrenchText() = runComposeUiTest {
        setContent { UnsupportedBrowserScreen(languageTag = "fr-FR", onOpenMobileApp = {}) }
        onNodeWithText(UNSUPPORTED_BROWSER_STRINGS_FR.title).assertExists()
        onNodeWithText(UNSUPPORTED_BROWSER_STRINGS_FR.explanation).assertExists()
        onNodeWithText(UNSUPPORTED_BROWSER_STRINGS_FR.supportedBrowsersBody).assertExists()
        onNodeWithText(UNSUPPORTED_BROWSER_STRINGS_FR.mobileAppLink).assertExists()
    }

    @Test
    fun unknownLanguageTagFallsBackToEnglish() = runComposeUiTest {
        setContent { UnsupportedBrowserScreen(languageTag = "de-DE", onOpenMobileApp = {}) }
        onNodeWithText(UNSUPPORTED_BROWSER_STRINGS_EN.title).assertExists()
    }

    @Test
    fun clickingMobileAppLinkInvokesCallback() = runComposeUiTest {
        var clicked = false
        setContent {
            UnsupportedBrowserScreen(languageTag = "en-US", onOpenMobileApp = { clicked = true })
        }
        onNodeWithText(UNSUPPORTED_BROWSER_STRINGS_EN.mobileAppLink).performClick()
        assertTrue(clicked)
    }
}
