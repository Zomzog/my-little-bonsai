# Update: Persist Web Folder Selection Across Page Reloads

## Date
2026-06-18

## Affected Spec
[local-folder-storage](../specs/local-folder-storage.md)

## Reason
On the Web target, the `FileSystemDirectoryHandle` was stored only in a JavaScript page-session global (`globalThis.__bonsaiDirHandle`). Refreshing the page cleared the global, causing `hasStorageAccess()` to return `false` and the app to send the user back to `FolderSetupScreen` every time.

## Change Description
The directory handle is now persisted to IndexedDB (database `bonsai-db`, object store `handles`, key `bonsaiDir`) after the user picks a folder. On subsequent page loads, `hasStorageAccess()` attempts to restore the handle from IndexedDB and calls `handle.queryPermission({ mode: 'readwrite' })`. If the browser reports `'granted'` (Chrome typically does after a refresh in the same origin session), the handle is restored to the page-session global and the user proceeds directly to BonsaiList.

All JS interop for IndexedDB lives in `IdbHandleStore.kt` (wasmJsMain), keeping `WebFolderStorageManager` free of new inline JS strings. The `IdbHandleStore` exposes a plain Kotlin API (`save` / `restore`) using `JsPromise.await()` for coroutine integration.

Android behaviour is unchanged.

## Migration / Impact
- No breaking changes.
- If the browser does not auto-grant permission (returns `'prompt'`), the user still sees `FolderSetupScreen` and must re-pick. This is a no-worse fallback.
- Clearing browser site data (IndexedDB) resets the selection as expected.
