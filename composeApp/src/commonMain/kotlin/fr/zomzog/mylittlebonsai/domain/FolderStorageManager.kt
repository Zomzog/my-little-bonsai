package fr.zomzog.mylittlebonsai.domain

interface FolderStorageManager {
    suspend fun hasStorageAccess(): Boolean
    suspend fun createMetadataFile()
}
