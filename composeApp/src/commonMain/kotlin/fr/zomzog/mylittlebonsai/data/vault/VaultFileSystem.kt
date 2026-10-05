package fr.zomzog.mylittlebonsai.data.vault

/**
 * File access to the vault root, keyed by paths relative to it (`"bonsais/erable/bonsai.md"`,
 * never a leading or trailing `/`; `""` denotes the root). Platform implementations translate
 * these into SAF `DocumentsContract` calls (Android) or plain file I/O (JVM).
 */
interface VaultFileSystem {
    /** Names of the direct subdirectories of [path]. Empty when [path] does not exist. */
    suspend fun listDirectories(path: String): List<String>

    /** Names of the files (not directories) directly under [path]. Empty when [path] does not exist. */
    suspend fun listFiles(path: String): List<String>

    suspend fun readText(path: String): String

    /**
     * Writes [content] to [path] atomically where the platform allows it (temp file,
     * then rename over the target), creating any missing parent directories.
     */
    suspend fun writeTextAtomic(path: String, content: String)
}
