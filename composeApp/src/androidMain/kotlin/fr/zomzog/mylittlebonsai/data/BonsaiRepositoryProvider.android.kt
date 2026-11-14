package fr.zomzog.mylittlebonsai.data

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import fr.zomzog.mylittlebonsai.data.vault.SafVaultFileSystem
import fr.zomzog.mylittlebonsai.data.vault.VaultBonsaiRepository
import fr.zomzog.mylittlebonsai.domain.BonsaiRepository

/**
 * Uses the vault at the folder the user picked during onboarding ([AndroidFolderStorageManager]).
 * [fr.zomzog.mylittlebonsai.App]'s navigation gate only composes a screen that calls this once
 * storage access is confirmed, so [AndroidFolderStorageManager.folderUri] is trusted to be set.
 */
@Composable
actual fun rememberBonsaiRepository(provided: BonsaiRepository?): BonsaiRepository {
    val context = LocalContext.current
    return remember {
        provided ?: VaultBonsaiRepository(
            SafVaultFileSystem(context, AndroidFolderStorageManager(context).folderUri()),
        )
    }
}
