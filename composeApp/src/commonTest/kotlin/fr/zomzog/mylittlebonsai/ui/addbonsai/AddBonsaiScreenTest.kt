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
        onNodeWithText(LABEL_KIND).assertExists()
        onNodeWithText(LABEL_PURCHASE_DATE).assertExists()
        onNodeWithText(LABEL_LAST_MAINTENANCE).assertExists()
        onNodeWithText(BUTTON_ADD).assertExists()
    }

    @Test
    fun clickingAddWithBlankNameShowsNameError() = runComposeUiTest {
        setContent { AddBonsaiScreen(repository = InMemoryBonsaiRepository(), onBonsaiAdded = {}) }
        onNodeWithText(BUTTON_ADD).performClick()
        onNodeWithText(ERROR_NAME_BLANK).assertExists()
    }

    @Test
    fun clickingAddWithBlankKindShowsKindError() = runComposeUiTest {
        setContent { AddBonsaiScreen(repository = InMemoryBonsaiRepository(), onBonsaiAdded = {}) }
        onNodeWithText(LABEL_NAME).performTextInput("Akira")
        onNodeWithText(BUTTON_ADD).performClick()
        onNodeWithText(ERROR_KIND_BLANK).assertExists()
    }

    @Test
    fun clickingAddWithInvalidDateShowsDateError() = runComposeUiTest {
        setContent { AddBonsaiScreen(repository = InMemoryBonsaiRepository(), onBonsaiAdded = {}) }
        onNodeWithText(LABEL_NAME).performTextInput("Akira")
        onNodeWithText(LABEL_KIND).performTextInput("Maple")
        onNodeWithText(LABEL_PURCHASE_DATE).performTextInput("not-a-date")
        onNodeWithText(BUTTON_ADD).performClick()
        onNodeWithText(ERROR_DATE_FORMAT).assertExists()
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
        onNodeWithText(LABEL_KIND).performTextInput("Maple")
        onNodeWithText(LABEL_PURCHASE_DATE).performTextInput("2024-03-10")
        onNodeWithText(BUTTON_ADD).performClick()
        waitForIdle()
        assertTrue(added)
    }

    @Test
    fun invalidMaintenanceDateShowsError() = runComposeUiTest {
        setContent { AddBonsaiScreen(repository = InMemoryBonsaiRepository(), onBonsaiAdded = {}) }
        onNodeWithText(LABEL_NAME).performTextInput("Akira")
        onNodeWithText(LABEL_KIND).performTextInput("Maple")
        onNodeWithText(LABEL_PURCHASE_DATE).performTextInput("2024-03-10")
        onNodeWithText(LABEL_LAST_MAINTENANCE).performTextInput("bad-date")
        onNodeWithText(BUTTON_ADD).performClick()
        onNodeWithText(ERROR_DATE_FORMAT).assertExists()
    }

    @Test
    fun validFormWithMaintenanceDateCallsOnBonsaiAdded() = runComposeUiTest {
        var added = false
        setContent {
            AddBonsaiScreen(
                repository = InMemoryBonsaiRepository(),
                onBonsaiAdded = { added = true },
            )
        }
        onNodeWithText(LABEL_NAME).performTextInput("Akira")
        onNodeWithText(LABEL_KIND).performTextInput("Maple")
        onNodeWithText(LABEL_PURCHASE_DATE).performTextInput("2024-03-10")
        onNodeWithText(LABEL_LAST_MAINTENANCE).performTextInput("2025-01-20")
        onNodeWithText(BUTTON_ADD).performClick()
        waitForIdle()
        assertTrue(added)
    }
}
