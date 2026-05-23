package fr.zomzog.mylittlebonsai

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import fr.zomzog.mylittlebonsai.data.InMemoryBonsaiRepository
import fr.zomzog.mylittlebonsai.domain.BonsaiRepository
import fr.zomzog.mylittlebonsai.ui.addbonsai.AddBonsaiScreen
import fr.zomzog.mylittlebonsai.ui.bonsailist.BonsaiListScreen

sealed class Screen {
    data object Home : Screen()
    data object BonsaiList : Screen()
    data object AddBonsai : Screen()
}

@Composable
fun App(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    repository: BonsaiRepository? = null,
) {
    val effectiveRepository = remember { repository ?: InMemoryBonsaiRepository() }
    val colors = if (useDarkTheme) darkColorScheme() else lightColorScheme()
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

    MaterialTheme(colorScheme = colors) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            when (currentScreen) {
                Screen.Home -> HomeScreen(
                    onNavigate = { currentScreen = Screen.BonsaiList },
                )
                Screen.BonsaiList -> BonsaiListScreen(
                    repository = effectiveRepository,
                    onNavigateToAdd = { currentScreen = Screen.AddBonsai },
                )
                Screen.AddBonsai -> AddBonsaiScreen(
                    repository = effectiveRepository,
                    onBonsaiAdded = { currentScreen = Screen.BonsaiList },
                )
            }
        }
    }
}
