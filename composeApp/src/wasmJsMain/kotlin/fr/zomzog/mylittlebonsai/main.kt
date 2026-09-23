package fr.zomzog.mylittlebonsai

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import fr.zomzog.mylittlebonsai.ui.unsupportedbrowser.UNSUPPORTED_BROWSER_MOBILE_APP_URL
import fr.zomzog.mylittlebonsai.ui.unsupportedbrowser.UnsupportedBrowserScreen
import kotlinx.browser.document

// Feature-detected, not user-agent sniffed: any browser that ships
// `showDirectoryPicker` works, whatever it calls itself.
@JsFun("() => 'showDirectoryPicker' in window")
private external fun isFileSystemAccessSupported(): Boolean

@JsFun("(url) => { window.open(url, '_blank') }")
private external fun openInNewTab(url: String)

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(document.body!!) {
        if (isFileSystemAccessSupported()) {
            App()
        } else {
            UnsupportedBrowserScreen(
                onOpenMobileApp = { openInNewTab(UNSUPPORTED_BROWSER_MOBILE_APP_URL) },
            )
        }
    }
}
