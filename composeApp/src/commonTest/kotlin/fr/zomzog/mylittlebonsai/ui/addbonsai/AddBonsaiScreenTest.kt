package fr.zomzog.mylittlebonsai.ui.addbonsai

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertExists
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
    fun clickingAddWithoutPurchaseDateShowsDateError() = runComposeUiTest {
        setContent { AddBonsaiScreen(repository = InMemoryBonsaiRepository(), onBonsaiAdded = {}) }
        onNodeWithText(LABEL_NAME).performTextInput("Akira")
        onNodeWithText(LABEL_KIND).performTextInput("Maple")
        onNodeWithText(BUTTON_ADD).performClick()
        onNodeWithText(ERROR_PURCHASE_DATE_REQUIRED).assertExists()
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
        onNodeWithText(LABEL_PURCHASE_DATE).performClick()
        onNodeWithText("Year").performTextInput("2024")
        onNodeWithText("Month").performTextInput("3")
        onNodeWithText("Day").performTextInput("10")
        onNodeWithText("OK").performClick()
        onNodeWithText(BUTTON_ADD).performClick()
        waitForIdle()
        assertTrue(added)
    }

    @Test
    fun invalidDateInPickerShowsError() = runComposeUiTest {
        setContent { AddBonsaiScreen(repository = InMemoryBonsaiRepository(), onBonsaiAdded = {}) }
        onNodeWithText(LABEL_PURCHASE_DATE).performClick()
        onNodeWithText("OK").performClick()
        onNodeWithText(ERROR_INVALID_DATE).assertExists()
    }

    @Test
    fun typingAfterInvalidDateClearsError() = runComposeUiTest {
        setContent { AddBonsaiScreen(repository = InMemoryBonsaiRepository(), onBonsaiAdded = {}) }
        onNodeWithText(LABEL_PURCHASE_DATE).performClick()
        onNodeWithText("OK").performClick()
        onNodeWithText(ERROR_INVALID_DATE).assertExists()
        onNodeWithText("Year").performTextInput("2024")
        onNodeWithText(ERROR_INVALID_DATE).assertDoesNotExist()
    }

    @Test
    fun cancellingDatePickerKeepsNoPurchaseDate() = runComposeUiTest {
        setContent { AddBonsaiScreen(repository = InMemoryBonsaiRepository(), onBonsaiAdded = {}) }
        onNodeWithText(LABEL_PURCHASE_DATE).performClick()
        onNodeWithText("Cancel").performClick()
        onNodeWithText(LABEL_PURCHASE_DATE).assertExists()
    }

    @Test
    fun purchaseDateErrorClearsAfterPickingDate() = runComposeUiTest {
        setContent { AddBonsaiScreen(repository = InMemoryBonsaiRepository(), onBonsaiAdded = {}) }
        onNodeWithText(LABEL_NAME).performTextInput("Akira")
        onNodeWithText(LABEL_KIND).performTextInput("Maple")
        onNodeWithText(BUTTON_ADD).performClick()
        onNodeWithText(ERROR_PURCHASE_DATE_REQUIRED).assertExists()
        onNodeWithText(LABEL_PURCHASE_DATE).performClick()
        onNodeWithText("Year").performTextInput("2024")
        onNodeWithText("Month").performTextInput("3")
        onNodeWithText("Day").performTextInput("10")
        onNodeWithText("OK").performClick()
        onNodeWithText(ERROR_PURCHASE_DATE_REQUIRED).assertDoesNotExist()
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
        onNodeWithText(LABEL_PURCHASE_DATE).performClick()
        onNodeWithText("Year").performTextInput("2024")
        onNodeWithText("Month").performTextInput("3")
        onNodeWithText("Day").performTextInput("10")
        onNodeWithText("OK").performClick()
        onNodeWithText(LABEL_LAST_MAINTENANCE).performClick()
        onNodeWithText("Year").performTextInput("2025")
        onNodeWithText("Month").performTextInput("1")
        onNodeWithText("Day").performTextInput("20")
        onNodeWithText("OK").performClick()
        onNodeWithText(BUTTON_ADD).performClick()
        waitForIdle()
        assertTrue(added)
    }
}
