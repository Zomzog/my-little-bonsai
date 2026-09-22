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
import mylittlebonsai.composeapp.generated.resources.Res
import mylittlebonsai.composeapp.generated.resources.unsupported_browser_explanation
import mylittlebonsai.composeapp.generated.resources.unsupported_browser_mobile_app_link
import mylittlebonsai.composeapp.generated.resources.unsupported_browser_supported_body
import mylittlebonsai.composeapp.generated.resources.unsupported_browser_supported_heading
import mylittlebonsai.composeapp.generated.resources.unsupported_browser_title
import org.jetbrains.compose.resources.stringResource

const val UNSUPPORTED_BROWSER_MOBILE_APP_URL = "https://www.youtube.com/watch?v=dQw4w9WgXcQ"

@Composable
fun UnsupportedBrowserScreen(
    onOpenMobileApp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    UnsupportedBrowserContent(onOpenMobileApp = onOpenMobileApp, modifier = modifier)
}

@Composable
internal fun UnsupportedBrowserContent(
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
            text = stringResource(Res.string.unsupported_browser_title),
            style = MaterialTheme.typography.headlineMedium,
        )

        Spacer(Modifier.height(24.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(Res.string.unsupported_browser_explanation),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = stringResource(Res.string.unsupported_browser_supported_heading),
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(Res.string.unsupported_browser_supported_body),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        TextButton(onClick = onOpenMobileApp) {
            Text(stringResource(Res.string.unsupported_browser_mobile_app_link))
        }
    }
}
