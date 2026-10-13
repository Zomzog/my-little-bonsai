package fr.zomzog.mylittlebonsai.data.vault

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isFailure
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class DocumentTreeVaultFileSystemTest {

    @Test
    fun writesAndReadsBackAFile() = runTest {
        val fs = DocumentTreeVaultFileSystem(FakeDocumentTree())
        fs.writeTextAtomic("bonsais/akira/bonsai.md", "id: a")
        assertThat(fs.readText("bonsais/akira/bonsai.md")).isEqualTo("id: a")
    }

    @Test
    fun overwritingAnExistingFileSucceeds() = runTest {
        val fs = DocumentTreeVaultFileSystem(FakeDocumentTree())
        fs.writeTextAtomic("bonsais/akira/bonsai.md", "id: a")
        fs.writeTextAtomic("bonsais/akira/bonsai.md", "id: a\npot: pot-1")
        assertThat(fs.readText("bonsais/akira/bonsai.md")).isEqualTo("id: a\npot: pot-1")
    }

    @Test
    fun overwriteSucceedsEvenWhenTheProviderCannotRenameOntoAnExistingName() = runTest {
        // Reproduces the reported AOSP behaviour: renameDocument returns null rather than
        // replacing an existing target. writeTextAtomic must delete the target first so the
        // rename never needs to overwrite anything.
        val tree = FakeDocumentTree().apply { renameCanOverwrite = false }
        val fs = DocumentTreeVaultFileSystem(tree)
        fs.writeTextAtomic("bonsais/akira/bonsai.md", "id: a")
        fs.writeTextAtomic("bonsais/akira/bonsai.md", "id: a\npot: pot-1")
        assertThat(fs.readText("bonsais/akira/bonsai.md")).isEqualTo("id: a\npot: pot-1")
    }

    @Test
    fun aFailedWriteLeavesTheOriginalFileIntactAndThrows() = runTest {
        val tree = FakeDocumentTree()
        val fs = DocumentTreeVaultFileSystem(tree)
        fs.writeTextAtomic("bonsais/akira/bonsai.md", "id: a")

        tree.failNextWrite = true
        assertThat(runCatching { fs.writeTextAtomic("bonsais/akira/bonsai.md", "corrupt") }).isFailure()

        assertThat(fs.readText("bonsais/akira/bonsai.md")).isEqualTo("id: a")
        assertThat(fs.listFiles("bonsais/akira")).containsExactly("bonsai.md")
    }

    @Test
    fun listingADirectoryPopulatesTheDocumentIdCacheSoReadingDoesNotRequeryTheParent() = runTest {
        val tree = FakeDocumentTree()
        val fs = DocumentTreeVaultFileSystem(tree)
        fs.writeTextAtomic("bonsais/akira/bonsai.md", "id: a")

        fs.listFiles("bonsais/akira")
        val queriesAfterListing = tree.queryCount
        fs.readText("bonsais/akira/bonsai.md")

        assertThat(tree.queryCount).isEqualTo(queriesAfterListing)
    }

    @Test
    fun listDirectoriesAndListFilesPartitionByMimeType() = runTest {
        val fs = DocumentTreeVaultFileSystem(FakeDocumentTree())
        fs.writeTextAtomic("bonsais/akira/bonsai.md", "id: a")
        fs.writeTextAtomic("bonsais/akira/sessions/2026-01-01-0000.md", "id: s1")

        assertThat(fs.listDirectories("bonsais/akira")).containsExactly("sessions")
        assertThat(fs.listFiles("bonsais/akira")).containsExactly("bonsai.md")
    }

    @Test
    fun readingAMissingFileThrows() = runTest {
        val fs = DocumentTreeVaultFileSystem(FakeDocumentTree())
        assertThat(runCatching { fs.readText("bonsais/missing/bonsai.md") }).isFailure()
    }
}
