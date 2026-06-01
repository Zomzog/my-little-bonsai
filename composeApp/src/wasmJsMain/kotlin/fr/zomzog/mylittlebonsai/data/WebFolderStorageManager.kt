package fr.zomzog.mylittlebonsai.data

import fr.zomzog.mylittlebonsai.domain.FolderStorageManager
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

// ---------------------------------------------------------------------------
// JS global state — the directory handle lives here so it survives Kotlin
// object re-creation within the same page session.
// ---------------------------------------------------------------------------

@JsFun("() => (typeof globalThis.__bonsaiDirHandle !== 'undefined' && globalThis.__bonsaiDirHandle !== null)")
private external fun hasDirHandleJs(): Boolean

@JsFun("(h) => { globalThis.__bonsaiDirHandle = h; }")
private external fun storeDirHandleJs(handle: JsAny)

@JsFun("() => { const h = globalThis.__bonsaiDirHandle; return (h !== undefined && h !== null) ? h : null; }")
private external fun getDirHandleJs(): JsAny?

// ---------------------------------------------------------------------------
// Async picker — uses a polling approach to bridge JS Promises to Kotlin
// coroutines without requiring direct Promise.await() support.
// ---------------------------------------------------------------------------

/**
 * Starts the browser's directory picker and stores the result in JS globals.
 *
 * Sets `globalThis.__bonsaiPickerDone = true` when finished (success or cancel).
 * Sets `globalThis.__bonsaiPickerSuccess = true` only on success.
 */
@JsFun(
    "() => {" +
        " globalThis.__bonsaiPickerDone = false;" +
        " globalThis.__bonsaiPickerSuccess = false;" +
        " if (typeof window.showDirectoryPicker !== 'function') {" +
        "   globalThis.__bonsaiPickerDone = true; return;" +
        " }" +
        " window.showDirectoryPicker({ mode: 'readwrite' })" +
        "   .then(h => {" +
        "     globalThis.__bonsaiDirHandle = h;" +
        "     globalThis.__bonsaiPickerSuccess = true;" +
        "     globalThis.__bonsaiPickerDone = true;" +
        "   })" +
        "   .catch(() => { globalThis.__bonsaiPickerDone = true; });" +
        "}",
)
private external fun startPickerJs()

@JsFun("() => globalThis.__bonsaiPickerDone === true")
private external fun isPickerDoneJs(): Boolean

@JsFun("() => globalThis.__bonsaiPickerSuccess === true")
private external fun isPickerSuccessJs(): Boolean

@JsFun("() => (typeof window !== 'undefined' && typeof window.showDirectoryPicker === 'function')")
private external fun isPickerSupportedJs(): Boolean

// ---------------------------------------------------------------------------
// Metadata file creation
// ---------------------------------------------------------------------------

@JsFun(
    "() => {" +
        " globalThis.__bonsaiWriteDone = false;" +
        " const dir = globalThis.__bonsaiDirHandle;" +
        " if (!dir) { globalThis.__bonsaiWriteDone = true; return; }" +
        " dir.getFileHandle('metadata.yaml', { create: true })" +
        "   .then(fh => fh.createWritable())" +
        "   .then(w => { const content = globalThis.__bonsaiMetaContent; return w.write(content).then(() => w.close()); })" +
        "   .then(() => { globalThis.__bonsaiWriteDone = true; })" +
        "   .catch(() => { globalThis.__bonsaiWriteDone = true; });" +
        "}",
)
private external fun startWriteMetadataJs()

@JsFun("() => globalThis.__bonsaiWriteDone === true")
private external fun isWriteDoneJs(): Boolean

@JsFun("(c) => { globalThis.__bonsaiMetaContent = c; }")
private external fun setMetaContentJs(content: String)

// ---------------------------------------------------------------------------

/**
 * Web implementation of [FolderStorageManager] using the File System Access API.
 *
 * The directory handle is stored in a JS module-level global and is therefore
 * lost on page reload. Full IndexedDB persistence is a future enhancement.
 */
class WebFolderStorageManager : FolderStorageManager {

    override suspend fun hasStorageAccess(): Boolean = hasDirHandleJs()

    override fun isFolderPickerSupported(): Boolean = isPickerSupportedJs()

    /**
     * Starts the browser folder picker. Must be called synchronously within a user-gesture
     * call stack (e.g. directly in a button onClick handler) so that the browser's transient
     * activation requirement is satisfied. Call [awaitPickerResult] afterwards to poll for
     * the outcome.
     */
    fun startPicker() {
        startPickerJs()
    }

    /**
     * Polls until the picker resolves and returns `true` on success (folder chosen),
     * `false` on cancel or error. Must be called after [startPicker].
     */
    suspend fun awaitPickerResult(): Boolean {
        while (!isPickerDoneJs()) {
            delay(POLL_INTERVAL_MS)
        }
        return isPickerSuccessJs()
    }

    override suspend fun createMetadataFile() {
        if (!hasDirHandleJs()) return
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        setMetaContentJs("creationDate: $today\n")
        startWriteMetadataJs()
        while (!isWriteDoneJs()) {
            delay(POLL_INTERVAL_MS)
        }
    }

    private companion object {
        const val POLL_INTERVAL_MS = 100L
    }
}
