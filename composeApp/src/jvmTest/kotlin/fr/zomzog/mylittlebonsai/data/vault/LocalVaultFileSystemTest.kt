package fr.zomzog.mylittlebonsai.data.vault

import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import kotlinx.coroutines.test.runTest
import kotlin.io.path.createTempDirectory
import kotlin.io.path.exists
import kotlin.test.Test

class LocalVaultFileSystemTest {

    @Test
    fun listDirectoriesReturnsEmptyForMissingPath() = runTest {
        val fs = LocalVaultFileSystem(createTempDirectory())
        assertThat(fs.listDirectories("bonsais")).isEmpty()
    }

    @Test
    fun listFilesReturnsEmptyForMissingPath() = runTest {
        val fs = LocalVaultFileSystem(createTempDirectory())
        assertThat(fs.listFiles("bonsais")).isEmpty()
    }

    @Test
    fun writeTextAtomicCreatesParentDirectoriesAndFile() = runTest {
        val root = createTempDirectory()
        val fs = LocalVaultFileSystem(root)
        fs.writeTextAtomic("bonsais/akira/bonsai.md", "hello")
        assertThat(fs.readText("bonsais/akira/bonsai.md")).isEqualTo("hello")
    }

    @Test
    fun writeTextAtomicOverwritesExistingFile() = runTest {
        val root = createTempDirectory()
        val fs = LocalVaultFileSystem(root)
        fs.writeTextAtomic("metadata.yaml", "first")
        fs.writeTextAtomic("metadata.yaml", "second")
        assertThat(fs.readText("metadata.yaml")).isEqualTo("second")
    }

    @Test
    fun writeTextAtomicLeavesNoTempFileBehind() = runTest {
        val root = createTempDirectory()
        val fs = LocalVaultFileSystem(root)
        fs.writeTextAtomic("metadata.yaml", "content")
        assertThat(root.resolve(".metadata.yaml.tmp").exists()).isEqualTo(false)
    }

    @Test
    fun listDirectoriesAndListFilesDistinguishEntryTypes() = runTest {
        val root = createTempDirectory()
        val fs = LocalVaultFileSystem(root)
        fs.writeTextAtomic("bonsais/akira/bonsai.md", "a")
        fs.writeTextAtomic("bonsais/bonsuke/bonsai.md", "b")
        assertThat(fs.listDirectories("bonsais")).containsExactlyInAnyOrder("akira", "bonsuke")
        assertThat(fs.listFiles("bonsais")).isEmpty()
        assertThat(fs.listFiles("bonsais/akira")).containsExactlyInAnyOrder("bonsai.md")
    }

    @Test
    fun readTextReadsFileWrittenOutsideTheStore() = runTest {
        val root = createTempDirectory()
        root.resolve("metadata.yaml").also { it.toFile().writeText("creationDate: 2026-05-23\n") }
        val fs = LocalVaultFileSystem(root)
        assertThat(fs.readText("metadata.yaml")).isEqualTo("creationDate: 2026-05-23\n")
    }
}
