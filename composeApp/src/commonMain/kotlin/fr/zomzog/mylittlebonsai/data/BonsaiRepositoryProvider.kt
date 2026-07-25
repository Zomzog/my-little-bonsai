package fr.zomzog.mylittlebonsai.data

import androidx.compose.runtime.Composable
import fr.zomzog.mylittlebonsai.domain.BonsaiRepository

/**
 * Returns a remembered [BonsaiRepository] for the current platform.
 *
 * When [provided] is non-null it is used as-is (tests and explicit DI).
 * Web returns a [StoredBonsaiRepository] backed by `localStorage`; the other
 * targets still use an in-memory repository.
 */
@Composable
expect fun rememberBonsaiRepository(provided: BonsaiRepository?): BonsaiRepository
