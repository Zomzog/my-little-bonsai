# Spec: Local Folder Storage

## Status
Implemented

## Goal
Store all bonsai data in a user-chosen folder on the device, similar to an Obsidian vault.
The user selects (or creates) a folder once; the app reads and writes files there with no
data ever leaving the device or touching a remote server.

## Scope

- In scope:
  - Onboarding screen asking the user to choose a storage folder
  - Platform folder picker (Android Storage Access Framework, Web File System Access API)
  - Persistent permission so the app can access the folder across restarts
  - `metadata.yaml` file creation on first vault setup with `creationDate`
  - Navigation gate: redirect to folder-setup if no access is configured, otherwise proceed normally
- Out of scope:
  - Sync between devices
  - Cloud backup
  - Migrating existing in-memory data to disk (future step)
  - Folder validation / integrity checks beyond presence of `metadata.yaml`

## Design

### Folder picker flow

```
App launch
    └─ HomeScreen (tap)
           ├─ hasStorageAccess? YES → BonsaiList
           └─ NO → FolderSetupScreen
                        └─ user taps "Choose Folder"
                               ├─ picker shown (platform-specific)
                               ├─ access granted → createMetadataFile() → BonsaiList
                               └─ access denied → stay on FolderSetupScreen
```

### Platform differences

| Concern | Android | Web (Wasm/JS) |
|---|---|---|
| Picker API | `ActivityResultContracts.OpenDocumentTree` (SAF) | `window.showDirectoryPicker()` (File System Access API) |
| Persistence | Persistent URI permission + SharedPreferences | In-memory handle (re-picker on reload; full persistence requires IndexedDB — future step) |
| File writing | `ContentResolver` + `DocumentsContract` | File System Access API write stream |
| Permissions | No manifest permission needed (SAF handles it) | Granted automatically via picker |

### Key data structures

```kotlin
interface FolderStorageManager {
    suspend fun hasStorageAccess(): Boolean
    suspend fun createMetadataFile()
}
```

`metadata.yaml` format:
```yaml
creationDate: 2026-05-23
```

### KMP architecture

- `FolderStorageManager` interface lives in `commonMain`
- Platform implementations in `androidMain` / `wasmJsMain` / `jvmMain` (JVM stub for tests)
- Composable helpers (`rememberFolderStorageManager`, `rememberFolderPickerLauncher`) use
  `expect`/`actual` so platform logic stays out of `commonMain`

## Acceptance Criteria

- [x] Tapping the HomeScreen when no folder is configured shows `FolderSetupScreen`
- [x] `FolderSetupScreen` explains that all data is local and nothing reaches the internet
- [x] `FolderSetupScreen` explains the user must choose an app folder or an empty folder
- [x] After granting access, `metadata.yaml` is created in the chosen folder with `creationDate`
- [x] After folder setup, the app navigates to BonsaiList
- [x] Tapping the HomeScreen when a folder is already configured goes straight to BonsaiList
- [x] Navigation tests pass for both paths (access granted / not granted)
- [x] `FolderSetupScreen` has UI tests covering explanation text and button

## Open Questions
- Should the Web implementation persist the directory handle across page reloads via IndexedDB?
  (Current: in-memory only — user must re-pick on reload.)
