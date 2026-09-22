package fr.zomzog.mylittlebonsai.ui.unsupportedbrowser

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertTrue
import mylittlebonsai.composeapp.generated.resources.Res
import mylittlebonsai.composeapp.generated.resources.unsupported_browser_explanation
import mylittlebonsai.composeapp.generated.resources.unsupported_browser_mobile_app_link
import mylittlebonsai.composeapp.generated.resources.unsupported_browser_supported_body
import mylittlebonsai.composeapp.generated.resources.unsupported_browser_title
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalTestApi::class)
class UnsupportedBrowserScreenTest {

    @Test
    fun showsTitleExplanationAndSupportedBrowsers() = runComposeUiTest {
        var title = ""
        var explanation = ""
        var supportedBrowsers = ""
        setContent {
            title = stringResource(Res.string.unsupported_browser_title)
            explanation = stringResource(Res.string.unsupported_browser_explanation)
            supportedBrowsers = stringResource(Res.string.unsupported_browser_supported_body)
            UnsupportedBrowserScreen(onOpenMobileApp = {})
        }

        onNodeWithText(title).assertExists()
        onNodeWithText(explanation).assertExists()
        onNodeWithText(supportedBrowsers).assertExists()
    }

    @Test
    fun showsMobileAppLink() = runComposeUiTest {
        var linkText = ""
        setContent {
            linkText = stringResource(Res.string.unsupported_browser_mobile_app_link)
            UnsupportedBrowserScreen(onOpenMobileApp = {})
        }

        onNodeWithText(linkText).assertExists()
    }

    @Test
    fun clickingMobileAppLinkInvokesCallback() = runComposeUiTest {
        var linkText = ""
        var clicked = false
        setContent {
            linkText = stringResource(Res.string.unsupported_browser_mobile_app_link)
            UnsupportedBrowserScreen(onOpenMobileApp = { clicked = true })
        }

        onNodeWithText(linkText).performClick()
        assertTrue(clicked)
    }
}
