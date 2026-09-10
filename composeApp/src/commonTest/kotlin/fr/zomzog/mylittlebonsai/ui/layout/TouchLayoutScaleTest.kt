package fr.zomzog.mylittlebonsai.ui.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class TouchLayoutScaleTest {

    @Test
    fun aPhoneSizedTouchViewportIsNotScaled() {
        assertThat(touchLayoutScale(viewportWidthDp = 393f, isCoarsePointer = true)).isEqualTo(1f)
    }

    @Test
    fun theWidestSupportedTouchViewportIsNotScaled() {
        assertThat(
            touchLayoutScale(
                viewportWidthDp = MAX_TOUCH_LAYOUT_WIDTH_DP,
                isCoarsePointer = true,
            ),
        ).isEqualTo(1f)
    }

    @Test
    fun aDesktopWidthTouchViewportIsScaledBackToPhoneProportions() {
        assertThat(touchLayoutScale(viewportWidthDp = 960f, isCoarsePointer = true)).isEqualTo(2f)
    }

    @Test
    fun aDesktopWidthMouseViewportIsNotScaled() {
        assertThat(touchLayoutScale(viewportWidthDp = 960f, isCoarsePointer = false)).isEqualTo(1f)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun contentIsComposedWithTheScaledDensity() = runComposeUiTest {
        var outerDensity = 0f
        var innerDensity = 0f
        setContent {
            outerDensity = LocalDensity.current.density
            Box(modifier = Modifier.requiredSize(960.dp, 480.dp)) {
                TouchLayoutScale(isCoarsePointer = true) {
                    innerDensity = LocalDensity.current.density
                }
            }
        }
        waitForIdle()
        assertThat(innerDensity / outerDensity).isEqualTo(2f)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun contentKeepsTheWindowDensityOnAPhoneSizedViewport() = runComposeUiTest {
        var outerDensity = 0f
        var innerDensity = 0f
        setContent {
            outerDensity = LocalDensity.current.density
            Box(modifier = Modifier.requiredSize(360.dp, 480.dp)) {
                TouchLayoutScale(isCoarsePointer = true) {
                    innerDensity = LocalDensity.current.density
                }
            }
        }
        waitForIdle()
        assertThat(innerDensity).isEqualTo(outerDensity)
    }
}
