package fr.zomzog.mylittlebonsai

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class HomeScreenTest {

    @Test
    fun appRendersTitleTaglineAndBonsaiImageInLightTheme() = runComposeUiTest {
        setContent { App(useDarkTheme = false) }

        onNodeWithText(APP_TITLE).assertExists()
        onNodeWithText(APP_TAGLINE).assertExists()
        onNodeWithContentDescription(BONSAI_IMAGE_DESCRIPTION).assertExists()
    }

    @Test
    fun appRendersTitleTaglineAndBonsaiImageInDarkTheme() = runComposeUiTest {
        setContent { App(useDarkTheme = true) }

        onNodeWithText(APP_TITLE).assertExists()
        onNodeWithText(APP_TAGLINE).assertExists()
        onNodeWithContentDescription(BONSAI_IMAGE_DESCRIPTION).assertExists()
    }

    @Test
    fun appUsesSystemThemeWhenNoOverrideProvided() = runComposeUiTest {
        setContent { App() }

        onNodeWithText(APP_TITLE).assertExists()
    }

    @Test
    fun homeScreenRendersExactlyOnceWhenInvokedDirectly() = runComposeUiTest {
        setContent { HomeScreen() }

        onAllNodesWithText(APP_TITLE).assertCountEquals(1)
        onAllNodesWithText(APP_TAGLINE).assertCountEquals(1)
    }
}
