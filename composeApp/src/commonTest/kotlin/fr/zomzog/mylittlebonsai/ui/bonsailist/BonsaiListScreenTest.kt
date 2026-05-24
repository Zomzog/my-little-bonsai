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
        val repo = InMemoryBonsaiRepository(listOf(Bonsai("1", "Akira", "Maple", LocalDate(2024, 3, 10))))
        setContent { BonsaiListScreen(repository = repo, onNavigateToAdd = {}) }
        onNodeWithText("Akira").assertExists()
    }

    @Test
    fun cardShowsKindAndPurchaseDate() = runComposeUiTest {
        val repo = InMemoryBonsaiRepository(listOf(Bonsai("1", "Akira", "Maple", LocalDate(2024, 3, 10))))
        setContent { BonsaiListScreen(repository = repo, onNavigateToAdd = {}) }
        onNodeWithText("Maple").assertExists()
        onNodeWithText("2024-03-10").assertExists()
    }

    @Test
    fun cardShowsLastMaintenanceDateWhenPresent() = runComposeUiTest {
        val repo = InMemoryBonsaiRepository(
            listOf(Bonsai("1", "Akira", "Maple", LocalDate(2024, 3, 10), LocalDate(2025, 1, 5))),
        )
        setContent { BonsaiListScreen(repository = repo, onNavigateToAdd = {}) }
        onNodeWithText("2025-01-05").assertExists()
    }

    @Test
    fun cardWithNullMaintenanceDateDoesNotShowIt() = runComposeUiTest {
        val repo = InMemoryBonsaiRepository(
            listOf(Bonsai("1", "Akira", "Maple", LocalDate(2024, 3, 10), lastMaintenanceDate = null)),
        )
        setContent { BonsaiListScreen(repository = repo, onNavigateToAdd = {}) }
        onNodeWithText("Akira").assertExists()
        onAllNodesWithText(EMPTY_LIST_MESSAGE).assertCountEquals(0)
    }

    @Test
    fun multipleBonsaisAreAllVisible() = runComposeUiTest {
        val repo = InMemoryBonsaiRepository(
            listOf(
                Bonsai("1", "Akira", "Maple", LocalDate(2024, 3, 10)),
                Bonsai("2", "Bonsuke", "Pine", LocalDate(2023, 7, 1)),
            ),
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
