package fr.zomzog.mylittlebonsai.ui.layout

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

/**
 * Widest logical width, in dp, a touch viewport is laid out at.
 */
const val MAX_TOUCH_LAYOUT_WIDTH_DP = 480f

/**
 * A browser that does not honour `width=device-width` — Chrome's "Desktop site"
 * mode, or a cached page from before the meta tag existed — lays the document
 * out at its 980 CSS px desktop fallback. The Compose canvas then covers the
 * whole screen with a scene that is ~980 dp wide, so every control is drawn at
 * roughly a third of its intended size and the only way to read the form is to
 * zoom, which clips it because a canvas cannot reflow.
 *
 * Capping the logical width restores phone proportions: the scene keeps filling
 * the canvas, but 480 dp of UI are stretched across it instead of 980.
 */
fun touchLayoutScale(viewportWidthDp: Float, isCoarsePointer: Boolean): Float =
    if (isCoarsePointer && viewportWidthDp > MAX_TOUCH_LAYOUT_WIDTH_DP) {
        viewportWidthDp / MAX_TOUCH_LAYOUT_WIDTH_DP
    } else {
        1f
    }

/**
 * Applies [touchLayoutScale] to [content] by scaling the density it is composed
 * with, leaving the window density — and with it pointer and accessibility
 * coordinates — untouched.
 */
@Composable
fun TouchLayoutScale(
    isCoarsePointer: Boolean,
    content: @Composable () -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val scale = touchLayoutScale(maxWidth.value, isCoarsePointer)
        CompositionLocalProvider(
            LocalDensity provides Density(density.density * scale, density.fontScale),
            content = content,
        )
    }
}
