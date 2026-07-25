package fr.zomzog.mylittlebonsai.data

// localStorage throws when the browser blocks site data (private mode, "block
// cookies"), so every access is guarded in JS and degrades to a no-op store.

@JsFun("(key) => { try { return localStorage.getItem(key) !== null; } catch (e) { return false; } }")
private external fun hasItemJs(key: String): Boolean

@JsFun("(key) => { try { return localStorage.getItem(key) ?? ''; } catch (e) { return ''; } }")
private external fun getItemJs(key: String): String

@JsFun("(key, value) => { try { localStorage.setItem(key, value); } catch (e) {} }")
private external fun setItemJs(key: String, value: String)

/** Web [KeyValueStore] backed by `window.localStorage`. */
class LocalStorageKeyValueStore : KeyValueStore {
    override fun read(key: String): String? = if (hasItemJs(key)) getItemJs(key) else null

    override fun write(key: String, value: String) = setItemJs(key, value)
}
