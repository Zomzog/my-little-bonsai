package fr.zomzog.mylittlebonsai.data

import fr.zomzog.mylittlebonsai.domain.FolderStorageManager

/**
 * JVM (desktop / test) implementation of [FolderStorageManager].
 *
 * [hasAccess] controls the initial state so tests can exercise both the
 * "already granted" and "not yet granted" navigation paths without touching
 * the filesystem.
 */
class JvmFolderStorageManager(private val hasAccess: Boolean = true) : FolderStorageManager {
    override suspend fun hasStorageAccess(): Boolean = hasAccess
    override suspend fun createMetadataFile() = Unit
}
