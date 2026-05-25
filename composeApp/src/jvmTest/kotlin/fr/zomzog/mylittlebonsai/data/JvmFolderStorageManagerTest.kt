package fr.zomzog.mylittlebonsai.data

import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class JvmFolderStorageManagerTest {

    @Test
    fun hasStorageAccessReturnsTrueByDefault() = runTest {
        val manager = JvmFolderStorageManager()
        assertThat(manager.hasStorageAccess()).isTrue()
    }

    @Test
    fun hasStorageAccessReturnsFalseWhenConfigured() = runTest {
        val manager = JvmFolderStorageManager(hasAccess = false)
        assertThat(manager.hasStorageAccess()).isFalse()
    }

    @Test
    fun createMetadataFileCompletesWithoutError() = runTest {
        val manager = JvmFolderStorageManager()
        manager.createMetadataFile()
    }
}
