package fr.zomzog.mylittlebonsai

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import fr.zomzog.mylittlebonsai.data.InMemoryBonsaiRepository
import fr.zomzog.mylittlebonsai.ui.addbonsai.ADD_BONSAI_TITLE
import fr.zomzog.mylittlebonsai.ui.addbonsai.BUTTON_ADD
import fr.zomzog.mylittlebonsai.ui.addbonsai.LABEL_KIND
import fr.zomzog.mylittlebonsai.ui.addbonsai.LABEL_NAME
import fr.zomzog.mylittlebonsai.ui.addbonsai.LABEL_PURCHASE_DATE
import fr.zomzog.mylittlebonsai.ui.bonsailist.ADD_BONSAI_BUTTON_DESCRIPTION
import fr.zomzog.mylittlebonsai.ui.bonsailist.BONSAI_LIST_TITLE
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class AppNavigationTest {

    @Test
    fun appStartsAtHomeScreen() = runComposeUiTest {
        setContent { App(useDarkTheme = false) }
        onNodeWithText(APP_TITLE).assertExists()
    }

    @Test
    fun clickingHomeScreenNavigatesToBonsaiList() = runComposeUiTest {
        setContent { App(useDarkTheme = false) }
        onNodeWithText(APP_TITLE).performClick()
        onNodeWithText(BONSAI_LIST_TITLE).assertExists()
    }

    @Test
    fun clickingAddButtonNavigatesToAddBonsai() = runComposeUiTest {
        setContent { App(useDarkTheme = false) }
        onNodeWithText(APP_TITLE).performClick()
        onNodeWithContentDescription(ADD_BONSAI_BUTTON_DESCRIPTION).performClick()
        onNodeWithText(ADD_BONSAI_TITLE).assertExists()
    }

    @Test
    fun submittingFormReturnsToList() = runComposeUiTest {
        val repo = InMemoryBonsaiRepository()
        setContent { App(useDarkTheme = false, repository = repo) }
        onNodeWithText(APP_TITLE).performClick()
        onNodeWithContentDescription(ADD_BONSAI_BUTTON_DESCRIPTION).performClick()
        onNodeWithText(LABEL_NAME).performTextInput("Akira")
        onNodeWithText(LABEL_KIND).performTextInput("Maple")
        onNodeWithText(LABEL_PURCHASE_DATE).performClick()
        onNodeWithText("OK").performClick()
        onNodeWithText(BUTTON_ADD).performClick()
        waitForIdle()
        onNodeWithText(BONSAI_LIST_TITLE).assertExists()
    }

    @Test
    fun addedBonsaiAppearsInListAfterSubmit() = runComposeUiTest {
        val repo = InMemoryBonsaiRepository()
        setContent { App(useDarkTheme = false, repository = repo) }
        onNodeWithText(APP_TITLE).performClick()
        onNodeWithContentDescription(ADD_BONSAI_BUTTON_DESCRIPTION).performClick()
        onNodeWithText(LABEL_NAME).performTextInput("Akira")
        onNodeWithText(LABEL_KIND).performTextInput("Maple")
        onNodeWithText(LABEL_PURCHASE_DATE).performClick()
        onNodeWithText("OK").performClick()
        onNodeWithText(BUTTON_ADD).performClick()
        waitForIdle()
        onNodeWithText("Akira").assertExists()
    }
}
