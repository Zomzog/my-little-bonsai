package fr.zomzog.mylittlebonsai.ui.addbonsai

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import fr.zomzog.mylittlebonsai.data.InMemoryBonsaiRepository
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class AddBonsaiScreenTest {

    @Test
    fun screenRendersAllFields() = runComposeUiTest {
        setContent { AddBonsaiScreen(repository = InMemoryBonsaiRepository(), onBonsaiAdded = {}) }
        onNodeWithText(LABEL_NAME).assertExists()
        onNodeWithText(LABEL_ADDED_ON).assertExists()
        onNodeWithText(BUTTON_ADD).assertExists()
    }

    @Test
    fun clickingAddWithBlankNameShowsNameError() = runComposeUiTest {
        setContent { AddBonsaiScreen(repository = InMemoryBonsaiRepository(), onBonsaiAdded = {}) }
        onNodeWithText(BUTTON_ADD).performClick()
        onNodeWithText(ERROR_NAME_BLANK).assertExists()
    }

    @Test
    fun clickingAddWithoutAddedOnShowsDateError() = runComposeUiTest {
        setContent { AddBonsaiScreen(repository = InMemoryBonsaiRepository(), onBonsaiAdded = {}) }
        onNodeWithText(LABEL_NAME).performTextInput("Akira")
        onNodeWithText(BUTTON_ADD).performClick()
        onNodeWithText(ERROR_ADDED_ON_REQUIRED).assertExists()
    }

    @Test
    fun validFormCallsOnBonsaiAdded() = runComposeUiTest {
        var added = false
        setContent {
            AddBonsaiScreen(
                repository = InMemoryBonsaiRepository(),
                onBonsaiAdded = { added = true },
            )
        }
        onNodeWithText(LABEL_NAME).performTextInput("Akira")
        onNodeWithText(LABEL_ADDED_ON).performClick()
        onNodeWithText("OK").performClick()
        onNodeWithText(BUTTON_ADD).performClick()
        waitForIdle()
        assertTrue(added)
    }

    @Test
    fun cancellingDatePickerKeepsNoAddedOn() = runComposeUiTest {
        setContent { AddBonsaiScreen(repository = InMemoryBonsaiRepository(), onBonsaiAdded = {}) }
        onNodeWithText(LABEL_ADDED_ON).performClick()
        onNodeWithText("Cancel").performClick()
        onNodeWithText(LABEL_ADDED_ON).assertExists()
    }

    @Test
    fun addedOnErrorClearsAfterPickingDate() = runComposeUiTest {
        setContent { AddBonsaiScreen(repository = InMemoryBonsaiRepository(), onBonsaiAdded = {}) }
        onNodeWithText(LABEL_NAME).performTextInput("Akira")
        onNodeWithText(BUTTON_ADD).performClick()
        onNodeWithText(ERROR_ADDED_ON_REQUIRED).assertExists()
        onNodeWithText(LABEL_ADDED_ON).performClick()
        onNodeWithText("OK").performClick()
        onNodeWithText(ERROR_ADDED_ON_REQUIRED).assertDoesNotExist()
    }
}
