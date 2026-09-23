package fr.zomzog.mylittlebonsai.data.vault

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFailure
import fr.zomzog.mylittlebonsai.domain.Action
import fr.zomzog.mylittlebonsai.domain.Bonsai
import fr.zomzog.mylittlebonsai.domain.Session
import fr.zomzog.mylittlebonsai.domain.Substrate
import fr.zomzog.mylittlebonsai.domain.VaultTimestamp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlin.io.path.createTempDirectory
import kotlin.test.Test

class VaultSessionRepositoryTest {

    private val akira = Bonsai(id = "id-a", name = "Akira", addedOn = LocalDate(2024, 3, 10))

    private fun session(
        id: String,
        date: LocalDate,
        createdAt: VaultTimestamp,
        actions: List<Action> = listOf(Action.Other("pruning")),
    ) = Session(id = id, date = date, createdAt = createdAt, actions = actions)

    private fun repositories(): Pair<VaultBonsaiRepository, VaultSessionRepository> {
        val fileSystem = LocalVaultFileSystem(createTempDirectory())
        val bonsaiRepository = VaultBonsaiRepository(fileSystem)
        return bonsaiRepository to VaultSessionRepository(fileSystem, bonsaiRepository)
    }

    @Test
    fun streamIsEmptyForAnUnknownBonsai() = runTest {
        val (_, sessionRepository) = repositories()
        assertThat(sessionRepository.getSessionsStream("missing").first()).isEmpty()
    }

    @Test
    fun addSessionEmitsIt() = runTest {
        val (bonsaiRepository, sessionRepository) = repositories()
        bonsaiRepository.addBonsai(akira)
        val session = session("s1", LocalDate(2026, 4, 12), VaultTimestamp(LocalDateTime(2026, 4, 12, 10, 35, 12), 0))
        sessionRepository.addSession("id-a", session)
        assertThat(sessionRepository.getSessionsStream("id-a").first()).containsExactly(session)
    }

    @Test
    fun addedSessionIsWrittenUnderTheBonsaiSlugFolder() = runTest {
        val fileSystem = LocalVaultFileSystem(createTempDirectory())
        val bonsaiRepository = VaultBonsaiRepository(fileSystem)
        val sessionRepository = VaultSessionRepository(fileSystem, bonsaiRepository)
        bonsaiRepository.addBonsai(akira)
        val session = session("s1", LocalDate(2026, 4, 12), VaultTimestamp(LocalDateTime(2026, 4, 12, 10, 35, 12), 0))
        sessionRepository.addSession("id-a", session)
        assertThat(fileSystem.listFiles("bonsais/akira/sessions")).containsExactly("2026-04-12-1035.md")
    }

    @Test
    fun sameMinuteCollisionAppendsNumericSuffix() = runTest {
        val fileSystem = LocalVaultFileSystem(createTempDirectory())
        val bonsaiRepository = VaultBonsaiRepository(fileSystem)
        val sessionRepository = VaultSessionRepository(fileSystem, bonsaiRepository)
        bonsaiRepository.addBonsai(akira)
        val createdAt = VaultTimestamp(LocalDateTime(2026, 4, 12, 10, 35, 12), 0)
        sessionRepository.addSession("id-a", session("s1", LocalDate(2026, 4, 12), createdAt))
        sessionRepository.addSession("id-a", session("s2", LocalDate(2026, 4, 12), createdAt))
        assertThat(fileSystem.listFiles("bonsais/akira/sessions").sorted())
            .containsExactly("2026-04-12-1035-2.md", "2026-04-12-1035.md")
    }

    @Test
    fun sessionsAreOrderedNewestFirstByDate() = runTest {
        val (bonsaiRepository, sessionRepository) = repositories()
        bonsaiRepository.addBonsai(akira)
        val older = session("s1", LocalDate(2026, 1, 1), VaultTimestamp(LocalDateTime(2026, 1, 1, 9, 0, 0), 0))
        val newer = session("s2", LocalDate(2026, 4, 12), VaultTimestamp(LocalDateTime(2026, 4, 12, 9, 0, 0), 0))
        sessionRepository.addSession("id-a", older)
        sessionRepository.addSession("id-a", newer)
        assertThat(sessionRepository.getSessionsStream("id-a").first()).containsExactly(newer, older)
    }

    @Test
    fun aFreshRepositoryOverTheSameFileSystemLoadsSessions() = runTest {
        val fileSystem = LocalVaultFileSystem(createTempDirectory())
        val bonsaiRepository = VaultBonsaiRepository(fileSystem)
        val sessionRepository = VaultSessionRepository(fileSystem, bonsaiRepository)
        bonsaiRepository.addBonsai(akira)
        val session = session("s1", LocalDate(2026, 4, 12), VaultTimestamp(LocalDateTime(2026, 4, 12, 10, 35, 12), 0))
        sessionRepository.addSession("id-a", session)

        val reloadedBonsais = VaultBonsaiRepository(fileSystem)
        val reloadedSessions = VaultSessionRepository(fileSystem, reloadedBonsais)
        assertThat(reloadedSessions.getSessionsStream("id-a").first()).containsExactly(session)
    }

    @Test
    fun aSessionFileThatFailsToParseIsSkipped() = runTest {
        val fileSystem = LocalVaultFileSystem(createTempDirectory())
        val bonsaiRepository = VaultBonsaiRepository(fileSystem)
        bonsaiRepository.addBonsai(akira)
        fileSystem.writeTextAtomic("bonsais/akira/sessions/2026-01-01-0000.md", "not front matter")

        val sessionRepository = VaultSessionRepository(fileSystem, bonsaiRepository)
        assertThat(sessionRepository.getSessionsStream("id-a").first()).isEmpty()
    }

    @Test
    fun addingASessionThrowsForAnUnknownBonsai() = runTest {
        val (_, sessionRepository) = repositories()
        val session = session("s1", LocalDate(2026, 4, 12), VaultTimestamp(LocalDateTime(2026, 4, 12, 10, 35, 12), 0))
        assertThat(runCatching { sessionRepository.addSession("missing", session) }).isFailure()
    }

    @Test
    fun repottingActionSyncsSubstrateAndPotToTheBonsai() = runTest {
        val (bonsaiRepository, sessionRepository) = repositories()
        bonsaiRepository.addBonsai(akira)
        val repotting = session(
            "s1",
            LocalDate(2026, 4, 12),
            VaultTimestamp(LocalDateTime(2026, 4, 12, 10, 0, 0), 0),
            actions = listOf(Action.Repotting(substrate = Substrate.NamedMix("mix-1"), pot = "pot-1")),
        )
        sessionRepository.addSession("id-a", repotting)

        val updated = bonsaiRepository.getBonsai("id-a")!!
        assertThat(updated.substrate).isEqualTo(Substrate.NamedMix("mix-1"))
        assertThat(updated.pot).isEqualTo("pot-1")
    }

    @Test
    fun onlyTheLatestRepottingByDateIsSyncedToTheBonsai() = runTest {
        val (bonsaiRepository, sessionRepository) = repositories()
        bonsaiRepository.addBonsai(akira)
        val earlier = session(
            "s1",
            LocalDate(2026, 1, 1),
            VaultTimestamp(LocalDateTime(2026, 1, 1, 9, 0, 0), 0),
            actions = listOf(Action.Repotting(substrate = Substrate.NamedMix("old-mix"), pot = "old-pot")),
        )
        val later = session(
            "s2",
            LocalDate(2026, 4, 12),
            VaultTimestamp(LocalDateTime(2026, 4, 12, 9, 0, 0), 0),
            actions = listOf(Action.Repotting(substrate = Substrate.NamedMix("new-mix"), pot = "new-pot")),
        )
        // Added out of chronological order: the newer session lands first, the older
        // one after — the sync must still pick the later one by `date`, not add order.
        sessionRepository.addSession("id-a", later)
        sessionRepository.addSession("id-a", earlier)

        val updated = bonsaiRepository.getBonsai("id-a")!!
        assertThat(updated.substrate).isEqualTo(Substrate.NamedMix("new-mix"))
        assertThat(updated.pot).isEqualTo("new-pot")
    }

    @Test
    fun nonRepottingSessionsDoNotTouchTheBonsai() = runTest {
        val (bonsaiRepository, sessionRepository) = repositories()
        bonsaiRepository.addBonsai(akira)
        sessionRepository.addSession(
            "id-a",
            session("s1", LocalDate(2026, 4, 12), VaultTimestamp(LocalDateTime(2026, 4, 12, 9, 0, 0), 0)),
        )
        val updated = bonsaiRepository.getBonsai("id-a")!!
        assertThat(updated.substrate).isEqualTo(akira.substrate)
        assertThat(updated.pot).isEqualTo(akira.pot)
    }
}
