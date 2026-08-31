package fr.zomzog.mylittlebonsai

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import fr.zomzog.mylittlebonsai.ui.layout.TouchLayoutScale
import kotlinx.browser.document
import kotlinx.browser.window

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val isCoarsePointer = hasCoarsePointer()
    ComposeViewport(document.body!!) {
        TouchLayoutScale(isCoarsePointer = isCoarsePointer) { App() }
    }
}

/**
 * A browser in desktop mode reports a desktop-width viewport but still runs on
 * the phone it is installed on, so the input device — not the reported width —
 * decides whether the layout has to be capped. A laptop with a touchscreen is
 * excluded by `hover`, which stays `hover` while a mouse is present.
 */
private fun hasCoarsePointer(): Boolean =
    window.matchMedia("(pointer: coarse)").matches ||
        (maxTouchPoints() > 0 && window.matchMedia("(hover: none)").matches)

private fun maxTouchPoints(): Int = js("navigator.maxTouchPoints")
