package fr.zomzog.mylittlebonsai.data

import kotlinx.coroutines.await

// ---------------------------------------------------------------------------
// JS interop layer — Promise-wrapping stubs only, no business logic in JS
// ---------------------------------------------------------------------------

@JsFun("""
    (name, version) => new Promise((resolve, reject) => {
        const req = indexedDB.open(name, version);
        req.onupgradeneeded = e => {
            const db = e.target.result;
            if (!db.objectStoreNames.contains('handles')) db.createObjectStore('handles');
        };
        req.onsuccess = e => resolve(e.target.result);
        req.onerror = e => reject(e.target.error);
    })
""")
private external fun jsOpenIdb(name: String, version: Int): JsPromise<JsAny>

@JsFun("""
    (db, key, value) => new Promise((resolve, reject) => {
        const tx = db.transaction('handles', 'readwrite');
        tx.objectStore('handles').put(value, key);
        tx.oncomplete = () => resolve(undefined);
        tx.onerror = e => reject(e.target.error);
    })
""")
private external fun jsIdbPut(db: JsAny, key: String, value: JsAny): JsPromise<JsAny?>

@JsFun("""
    (db, key) => new Promise((resolve, reject) => {
        const tx = db.transaction('handles', 'readonly');
        const req = tx.objectStore('handles').get(key);
        req.onsuccess = e => resolve(e.target.result || null);
        req.onerror = e => reject(e.target.error);
    })
""")
private external fun jsIdbGet(db: JsAny, key: String): JsPromise<JsAny?>

@JsFun("(handle) => handle.queryPermission({ mode: 'readwrite' })")
private external fun jsQueryPermission(handle: JsAny): JsPromise<JsString>

// ---------------------------------------------------------------------------

internal object IdbHandleStore {
    private const val DB_NAME = "bonsai-db"
    private const val DB_VERSION = 1
    private const val HANDLE_KEY = "bonsaiDir"

    suspend fun save(handle: JsAny) {
        runCatching {
            val db = jsOpenIdb(DB_NAME, DB_VERSION).await()
            jsIdbPut(db, HANDLE_KEY, handle).await()
        }
    }

    suspend fun restore(): JsAny? = runCatching {
        val db = jsOpenIdb(DB_NAME, DB_VERSION).await()
        val handle = jsIdbGet(db, HANDLE_KEY).await() ?: return@runCatching null
        val permission = jsQueryPermission(handle).await()
        if (permission.toString() == "granted") handle else null
    }.getOrNull()
}
