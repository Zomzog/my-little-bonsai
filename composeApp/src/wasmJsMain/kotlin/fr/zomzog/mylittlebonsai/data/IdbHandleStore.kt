package fr.zomzog.mylittlebonsai.data

import kotlinx.coroutines.delay

// ---------------------------------------------------------------------------
// Fire-and-forget save — reads handle from globalThis.__bonsaiDirHandle
// ---------------------------------------------------------------------------

@JsFun(
    "() => {" +
        " const handle = globalThis.__bonsaiDirHandle;" +
        " if (!handle) return;" +
        " const req = indexedDB.open('bonsai-db', 1);" +
        " req.onupgradeneeded = e => {" +
        "   const db = e.target.result;" +
        "   if (!db.objectStoreNames.contains('handles')) db.createObjectStore('handles');" +
        " };" +
        " req.onsuccess = e => {" +
        "   const tx = e.target.result.transaction('handles', 'readwrite');" +
        "   tx.objectStore('handles').put(handle, 'bonsaiDir');" +
        " };" +
        "}",
)
private external fun startIdbSaveJs()

// ---------------------------------------------------------------------------
// Polling-based restore — writes result to globalThis flags
// ---------------------------------------------------------------------------

@JsFun(
    "() => {" +
        " globalThis.__bonsaiIdbDone = false;" +
        " globalThis.__bonsaiIdbHandle = null;" +
        " const openReq = indexedDB.open('bonsai-db', 1);" +
        " openReq.onupgradeneeded = e => {" +
        "   const db = e.target.result;" +
        "   if (!db.objectStoreNames.contains('handles')) db.createObjectStore('handles');" +
        " };" +
        " openReq.onerror = () => { globalThis.__bonsaiIdbDone = true; };" +
        " openReq.onsuccess = e => {" +
        "   const db = e.target.result;" +
        "   const tx = db.transaction('handles', 'readonly');" +
        "   const getReq = tx.objectStore('handles').get('bonsaiDir');" +
        "   getReq.onerror = () => { globalThis.__bonsaiIdbDone = true; };" +
        "   getReq.onsuccess = e => {" +
        "     const handle = e.target.result;" +
        "     if (!handle) { globalThis.__bonsaiIdbDone = true; return; }" +
        "     handle.queryPermission({ mode: 'readwrite' })" +
        "       .then(perm => {" +
        "         if (perm === 'granted') globalThis.__bonsaiIdbHandle = handle;" +
        "         globalThis.__bonsaiIdbDone = true;" +
        "       })" +
        "       .catch(() => { globalThis.__bonsaiIdbDone = true; });" +
        "   };" +
        " };" +
        "}",
)
private external fun startIdbRestoreJs()

@JsFun("() => globalThis.__bonsaiIdbDone === true")
private external fun isIdbDoneJs(): Boolean

@JsFun("() => { const h = globalThis.__bonsaiIdbHandle; return (h !== undefined && h !== null) ? h : null; }")
private external fun getIdbHandleJs(): JsAny?

// ---------------------------------------------------------------------------

internal object IdbHandleStore {
    private const val POLL_INTERVAL_MS = 100L

    fun save() {
        startIdbSaveJs()
    }

    suspend fun restore(): JsAny? {
        startIdbRestoreJs()
        while (!isIdbDoneJs()) {
            delay(POLL_INTERVAL_MS)
        }
        return getIdbHandleJs()
    }
}
