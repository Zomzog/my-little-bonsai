package fr.zomzog.mylittlebonsai.data

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import fr.zomzog.mylittlebonsai.data.vault.SafVaultFileSystem
import fr.zomzog.mylittlebonsai.data.vault.VaultBonsaiRepository
import fr.zomzog.mylittlebonsai.domain.BonsaiRepository

/**
 * Uses the vault at the folder the user picked during onboarding ([AndroidFolderStorageManager]),
 * or an in-memory repository when reached before onboarding (there is no screen that does,
 * since [fr.zomzog.mylittlebonsai.App]'s navigation gate requires storage access first).
 */
@Composable
actual fun rememberBonsaiRepository(provided: BonsaiRepository?): BonsaiRepository {
    val context = LocalContext.current
    return remember {
        provided ?: AndroidFolderStorageManager(context).folderUri()?.let { treeUri ->
            VaultBonsaiRepository(SafVaultFileSystem(context, treeUri))
        } ?: InMemoryBonsaiRepository()
    }
}
