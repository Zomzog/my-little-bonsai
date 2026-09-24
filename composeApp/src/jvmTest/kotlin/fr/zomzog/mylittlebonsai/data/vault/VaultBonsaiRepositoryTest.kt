package fr.zomzog.mylittlebonsai.data.vault

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFailure
import assertk.assertions.isNull
import fr.zomzog.mylittlebonsai.domain.Bonsai
import fr.zomzog.mylittlebonsai.domain.Substrate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.io.path.createTempDirectory
import kotlin.test.Test

class VaultBonsaiRepositoryTest {

    private fun newRepository(): VaultBonsaiRepository =
        VaultBonsaiRepository(LocalVaultFileSystem(createTempDirectory()))

    private val akira = Bonsai(id = "id-a", name = "Akira", addedOn = LocalDate(2024, 3, 10))

    @Test
    fun streamIsEmptyForAFreshVault() = runTest {
        assertThat(newRepository().getBonsaisStream().first()).isEmpty()
    }

    @Test
    fun addBonsaiEmitsUpdatedList() = runTest {
        val repository = newRepository()
        repository.addBonsai(akira)
        assertThat(repository.getBonsaisStream().first()).containsExactly(akira)
    }

    @Test
    fun addedBonsaiIsWrittenToTheExpectedSlugFolder() = runTest {
        val root = createTempDirectory()
        val fileSystem = LocalVaultFileSystem(root)
        VaultBonsaiRepository(fileSystem).addBonsai(akira)
        assertThat(fileSystem.listDirectories("bonsais")).containsExactly("akira")
        assertThat(BonsaiVaultCodec.decode(fileSystem.readText("bonsais/akira/bonsai.md"))).isEqualTo(akira)
    }

    @Test
    fun aFreshRepositoryOverTheSameFileSystemLoadsTheBonsai() = runTest {
        val root = createTempDirectory()
        val fileSystem = LocalVaultFileSystem(root)
        VaultBonsaiRepository(fileSystem).addBonsai(akira)

        val reloaded = VaultBonsaiRepository(fileSystem)
        assertThat(reloaded.getBonsaisStream().first()).containsExactly(akira)
    }

    @Test
    fun twoBonsaisWithTheSameNameGetDistinctSlugFolders() = runTest {
        val root = createTempDirectory()
        val fileSystem = LocalVaultFileSystem(root)
        val repository = VaultBonsaiRepository(fileSystem)
        val first = akira
        val second = akira.copy(id = "id-b")
        repository.addBonsai(first)
        repository.addBonsai(second)
        assertThat(fileSystem.listDirectories("bonsais")).containsExactly("akira", "akira-2")
    }

    @Test
    fun aFileThatFailsToParseIsSkippedRatherThanFailingTheLoad() = runTest {
        val root = createTempDirectory()
        val fileSystem = LocalVaultFileSystem(root)
        fileSystem.writeTextAtomic("bonsais/broken/bonsai.md", "not front matter at all")
        fileSystem.writeTextAtomic("bonsais/akira/bonsai.md", BonsaiVaultCodec.encode(akira))

        val repository = VaultBonsaiRepository(fileSystem)
        assertThat(repository.getBonsaisStream().first()).containsExactly(akira)
    }

    @Test
    fun getBonsaiReturnsNullForAnUnknownId() = runTest {
        assertThat(newRepository().getBonsai("missing")).isNull()
    }

    @Test
    fun getBonsaiFindsAnAddedBonsai() = runTest {
        val repository = newRepository()
        repository.addBonsai(akira)
        assertThat(repository.getBonsai("id-a")).isEqualTo(akira)
    }

    @Test
    fun updateBonsaiRewritesItsExistingFile() = runTest {
        val root = createTempDirectory()
        val fileSystem = LocalVaultFileSystem(root)
        val repository = VaultBonsaiRepository(fileSystem)
        repository.addBonsai(akira)

        val updated = akira.copy(pot = "pot-1", substrate = Substrate.NamedMix("mix-1"))
        repository.updateBonsai(updated)

        assertThat(repository.getBonsai("id-a")).isEqualTo(updated)
        assertThat(fileSystem.listDirectories("bonsais")).containsExactly("akira")
        assertThat(BonsaiVaultCodec.decode(fileSystem.readText("bonsais/akira/bonsai.md"))).isEqualTo(updated)
    }

    @Test
    fun updateBonsaiThrowsForAnUnknownBonsai() = runTest {
        val repository = newRepository()
        assertThat(runCatching { repository.updateBonsai(akira) }).isFailure()
    }
}
