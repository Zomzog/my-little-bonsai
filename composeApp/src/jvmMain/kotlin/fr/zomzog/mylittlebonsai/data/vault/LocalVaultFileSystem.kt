package fr.zomzog.mylittlebonsai.data.vault

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.name
import kotlin.io.path.readText
import kotlin.io.path.writeText

/** [VaultFileSystem] over a plain directory on disk, via `java.nio.file`. */
class LocalVaultFileSystem(private val root: Path) : VaultFileSystem {

    override suspend fun listDirectories(path: String): List<String> = withContext(Dispatchers.IO) {
        listEntries(path) { it.isDirectory() }
    }

    override suspend fun listFiles(path: String): List<String> = withContext(Dispatchers.IO) {
        listEntries(path) { it.isRegularFile() }
    }

    override suspend fun readText(path: String): String = withContext(Dispatchers.IO) {
        resolve(path).readText()
    }

    override suspend fun writeTextAtomic(path: String, content: String) = withContext(Dispatchers.IO) {
        val target = resolve(path)
        Files.createDirectories(target.parent)
        val tmp = target.resolveSibling(".${target.name}.tmp")
        tmp.writeText(content)
        try {
            Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
        } catch (e: AtomicMoveNotSupportedException) {
            Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING)
        }
        Unit
    }

    private fun listEntries(path: String, predicate: (Path) -> Boolean): List<String> {
        val dir = resolve(path)
        if (!dir.exists() || !dir.isDirectory()) return emptyList()
        return Files.newDirectoryStream(dir).use { stream -> stream.filter(predicate).map { it.name } }
    }

    private fun resolve(path: String): Path = if (path.isEmpty()) root else root.resolve(path)
}
