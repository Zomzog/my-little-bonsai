package fr.zomzog.mylittlebonsai.data

/**
 * Minimal string key/value persistence abstraction.
 *
 * Implementations are expected to be synchronous and durable across app restarts
 * (Web: `localStorage`). Keeping the surface this small lets the storage-backed
 * repository be fully unit-tested in `commonTest` with an in-memory fake.
 */
interface KeyValueStore {
    fun read(key: String): String?
    fun write(key: String, value: String)
}
