package fr.zomzog.mylittlebonsai.data

/** In-memory [KeyValueStore] standing in for browser storage. */
class FakeKeyValueStore(initial: Map<String, String> = emptyMap()) : KeyValueStore {
    private val entries = initial.toMutableMap()

    var writeCount = 0
        private set

    override fun read(key: String): String? = entries[key]

    override fun write(key: String, value: String) {
        entries[key] = value
        writeCount++
    }
}
