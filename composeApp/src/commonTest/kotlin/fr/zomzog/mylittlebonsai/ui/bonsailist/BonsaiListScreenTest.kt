package fr.zomzog.mylittlebonsai.ui.bonsailist

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import fr.zomzog.mylittlebonsai.data.InMemoryBonsaiRepository
import fr.zomzog.mylittlebonsai.domain.Bonsai
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class BonsaiListScreenTest {

    private fun bonsai(id: String, name: String, addedOn: LocalDate = LocalDate(2024, 3, 10)) =
        Bonsai(id = id, name = name, addedOn = addedOn)

    @Test
    fun emptyRepositoryShowsEmptyState() = runComposeUiTest {
        setContent {
            BonsaiListScreen(repository = InMemoryBonsaiRepository(), onNavigateToAdd = {})
        }
        onNodeWithText(EMPTY_LIST_MESSAGE).assertExists()
    }

    @Test
    fun emptyRepositoryDoesNotShowCard() = runComposeUiTest {
        setContent {
            BonsaiListScreen(repository = InMemoryBonsaiRepository(), onNavigateToAdd = {})
        }
        onAllNodesWithText("Akira").assertCountEquals(0)
    }

    @Test
    fun repositoryWithBonsaiShowsCardName() = runComposeUiTest {
        val repo = InMemoryBonsaiRepository(listOf(bonsai("1", "Akira")))
        setContent { BonsaiListScreen(repository = repo, onNavigateToAdd = {}) }
        onNodeWithText("Akira").assertExists()
    }

    @Test
    fun cardShowsAddedOnDate() = runComposeUiTest {
        val repo = InMemoryBonsaiRepository(listOf(bonsai("1", "Akira")))
        setContent { BonsaiListScreen(repository = repo, onNavigateToAdd = {}) }
        onNodeWithText("2024-03-10").assertExists()
    }

    @Test
    fun multipleBonsaisAreAllVisible() = runComposeUiTest {
        val repo = InMemoryBonsaiRepository(
            listOf(bonsai("1", "Akira"), bonsai("2", "Bonsuke", LocalDate(2023, 7, 1))),
        )
        setContent { BonsaiListScreen(repository = repo, onNavigateToAdd = {}) }
        onNodeWithText("Akira").assertExists()
        onNodeWithText("Bonsuke").assertExists()
    }

    @Test
    fun addButtonClickCallsOnNavigateToAdd() = runComposeUiTest {
        var clicked = false
        setContent {
            BonsaiListScreen(repository = InMemoryBonsaiRepository(), onNavigateToAdd = { clicked = true })
        }
        onNodeWithContentDescription(ADD_BONSAI_BUTTON_DESCRIPTION).performClick()
        assertTrue(clicked)
    }
}
