package fr.zomzog.mylittlebonsai.data

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import fr.zomzog.mylittlebonsai.domain.BonsaiRepository

@Composable
actual fun rememberBonsaiRepository(provided: BonsaiRepository?): BonsaiRepository =
    remember { provided ?: StoredBonsaiRepository(LocalStorageKeyValueStore()) }
