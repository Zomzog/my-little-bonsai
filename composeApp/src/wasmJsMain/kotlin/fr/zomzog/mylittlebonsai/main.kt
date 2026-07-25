package fr.zomzog.mylittlebonsai

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport

private const val VIEWPORT_CONTAINER_ID = "composeTarget"

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(viewportContainerId = VIEWPORT_CONTAINER_ID) { App() }
}
