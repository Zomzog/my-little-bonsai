package fr.zomzog.mylittlebonsai.ui.unsupportedbrowser

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

const val UNSUPPORTED_BROWSER_MOBILE_APP_URL = "https://www.youtube.com/watch?v=dQw4w9WgXcQ"

internal data class UnsupportedBrowserStrings(
    val title: String,
    val explanation: String,
    val supportedBrowsersHeading: String,
    val supportedBrowsersBody: String,
    val mobileAppLink: String,
)

internal val UNSUPPORTED_BROWSER_STRINGS_EN = UnsupportedBrowserStrings(
    title = "This browser isn't supported",
    explanation = "My Little Bonsai stores your data in a real folder on your device. " +
        "That requires a browser feature (the File System Access API) that this browser " +
        "does not provide.",
    supportedBrowsersHeading = "Supported browsers",
    supportedBrowsersBody = "Chrome, Edge, and other Chromium-based desktop browsers.",
    mobileAppLink = "Get the mobile app instead",
)

internal val UNSUPPORTED_BROWSER_STRINGS_FR = UnsupportedBrowserStrings(
    title = "Ce navigateur n'est pas pris en charge",
    explanation = "My Little Bonsai enregistre vos données dans un vrai dossier sur votre " +
        "appareil. Cela nécessite une fonctionnalité du navigateur (la File System Access " +
        "API) que ce navigateur ne propose pas.",
    supportedBrowsersHeading = "Navigateurs pris en charge",
    supportedBrowsersBody = "Chrome, Edge et les autres navigateurs de bureau basés sur Chromium.",
    mobileAppLink = "Utiliser l'application mobile à la place",
)

internal fun unsupportedBrowserStrings(languageTag: String): UnsupportedBrowserStrings =
    if (languageTag.startsWith("fr", ignoreCase = true)) {
        UNSUPPORTED_BROWSER_STRINGS_FR
    } else {
        UNSUPPORTED_BROWSER_STRINGS_EN
    }

@Composable
fun UnsupportedBrowserScreen(
    languageTag: String,
    onOpenMobileApp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    UnsupportedBrowserContent(
        strings = unsupportedBrowserStrings(languageTag),
        onOpenMobileApp = onOpenMobileApp,
        modifier = modifier,
    )
}

@Composable
internal fun UnsupportedBrowserContent(
    strings: UnsupportedBrowserStrings,
    onOpenMobileApp: () -> Unit,
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
            text = strings.title,
            style = MaterialTheme.typography.headlineMedium,
        )

        Spacer(Modifier.height(24.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = strings.explanation,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = strings.supportedBrowsersHeading,
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = strings.supportedBrowsersBody,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        TextButton(onClick = onOpenMobileApp) {
            Text(strings.mobileAppLink)
        }
    }
}
