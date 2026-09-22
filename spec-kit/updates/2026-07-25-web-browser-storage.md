# Update: Web Stores Data in the Browser Instead of a Chosen Folder

## Date
2026-07-25

## Affected Spec
[local-folder-storage](../specs/local-folder-storage.md)

## Reason
On the Web target the user was asked to pick a storage folder on **every** visit.

The File System Access API does not grant a durable permission: a
`FileSystemDirectoryHandle` restored from IndexedDB reports
`queryPermission({ mode: 'readwrite' }) === 'prompt'` after a page load, not
`'granted'`. Re-granting requires a fresh user gesture, so
[the IndexedDB persistence added on 2026-06-18](2026-06-18-folder-selection-web-persistence.md)
could not remove the prompt — it only avoided it in the narrow cases where Chrome
auto-grants, which is not the common path.

Separately, the chosen folder never held any bonsai data. It only ever received
`metadata.yaml`; the records themselves lived in `InMemoryBonsaiRepository` and
were lost on reload. So the folder prompt cost the user a click on every visit and
bought no persistence at all.

## Change Description

### Web
- Bonsai records are persisted in `localStorage` under the key `bonsai.bonsais`,
  as a JSON array serialised with kotlinx-serialization.
- The vault creation date moves from `metadata.yaml` to the `bonsai.creationDate`
  key, written once on the first visit.
- `BrowserStorageManager` replaces `WebFolderStorageManager` as the Web
  `FolderStorageManager`. Browser storage needs no user grant, so
  `hasStorageAccess()` always returns `true` and `FolderSetupScreen` is
  unreachable on Web.
- The File System Access API is no longer used. `WebFolderStorageManager` and
  `IdbHandleStore` are deleted, along with the Brave "picker unavailable"
  message path on Web — a browser that blocks the API no longer matters.

### Shared
- New `KeyValueStore` interface (`commonMain`): `read` / `write` of string keys.
  Web implements it with `LocalStorageKeyValueStore`; every `localStorage` call is
  guarded in JS so a browser blocking site data degrades to a no-op rather than
  throwing.
- New `StoredBonsaiRepository` (`commonMain`): a `BonsaiRepository` over a
  `KeyValueStore`. It reads the list once at construction and rewrites it in full
  on each add. Unreadable stored values decode to an empty list so a corrupt entry
  cannot stop the app from starting.
- New `expect fun rememberBonsaiRepository(provided)` selects the platform
  repository. `App` uses it instead of constructing `InMemoryBonsaiRepository`
  directly.
- Dates are stored as ISO-8601 strings in the storage DTO, keeping the payload
  independent of kotlinx-datetime's serializer artifacts.

### Android
Unchanged. SAF persistent URI permissions do survive restarts, so Android keeps
the folder picker, `metadata.yaml`, and the folder-setup onboarding, and still
uses the in-memory repository.

### Platform differences (revised)

| Concern | Android | Web (Wasm/JS) |
|---|---|---|
| Data location | User-chosen folder (SAF) | `localStorage` |
| Onboarding | `FolderSetupScreen` on first run | None — goes straight to the list |
| Bonsai persistence | In-memory (not yet on disk) | `localStorage`, survives reload |
| Creation date | `metadata.yaml` in the folder | `bonsai.creationDate` key |

## Migration / Impact
- **Web users are no longer asked for a folder.** Any folder previously chosen is
  ignored; a `metadata.yaml` already written there is left untouched and unread.
- No bonsai data can be lost in the migration, because Web never wrote bonsai data
  to the folder in the first place.
- The stale `bonsai-db` IndexedDB database holding the old directory handle is
  left behind. It is inert; clearing site data removes it.
- Clearing browser site data now clears the bonsai records — previously it only
  cleared the folder reference. This is the trade-off of browser-local storage and
  matches how the app already behaved on reload.
- Data still never leaves the device, so the privacy promise is unchanged.

## Open Questions
- Should Android also move off the folder and persist records locally, so that both
  platforms actually save data? Today Android records are still in-memory only.
- Should Web offer an explicit export/import to a file, now that there is no folder
  to inspect?
