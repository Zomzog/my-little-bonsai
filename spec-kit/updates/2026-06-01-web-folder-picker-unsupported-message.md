# Update: Web Folder Picker — Surface Unsupported/Blocked Browsers

## Date
2026-06-01

## Affected Spec
[local-folder-storage](../specs/local-folder-storage.md)

## Reason
The "Choose Folder" button did nothing in Brave, even with Shields / the
ad-blocker disabled. Brave ships with the File System Access API
(`window.showDirectoryPicker`) disabled by default — it is hidden behind
`brave://flags/#file-system-access-api`, and turning off Shields does not
re-enable it. The web picker code detected the missing API but then silently
invoked the cancel/denied path, so the user got no feedback and assumed the
button was broken.

## Change Description
- `FolderStorageManager` gains `fun isFolderPickerSupported(): Boolean = true`.
  This is a platform capability check, defaulting to `true` so Android and the
  JVM stub are unchanged.
- `WebFolderStorageManager` overrides `isFolderPickerSupported()` to return
  `typeof window.showDirectoryPicker === 'function'`, so it reports `false`
  when the browser blocks the API.
- `FolderSetupScreen` now checks `isFolderPickerSupported()` when the button is
  tapped. When unsupported it shows an actionable error card
  (`FOLDER_SETUP_UNSUPPORTED_MESSAGE`) telling the user how to enable the API in
  Brave, instead of silently doing nothing. When supported it launches the
  picker as before and clears any previous message.
- The screen body is split into a stateless `FolderSetupContent` composable
  (driven by `errorMessage` + `onChooseFolder`) so the message rendering is unit
  testable in `commonTest`.

## Migration / Impact
No breaking changes. Chrome/Edge behaviour is unchanged (the picker opens as
before). Brave (and any browser without the File System Access API) now shows a
clear explanation and the steps to enable folder access instead of an
unresponsive button. The underlying limitation — the web app genuinely requires
the File System Access API to read/write the chosen folder — is unchanged; this
update only makes the constraint visible to the user.

## Platform Differences
| Concern | Android | Web (Wasm/JS) |
|---|---|---|
| `isFolderPickerSupported()` | always `true` | `true` only when `window.showDirectoryPicker` exists |
| Unsupported feedback | n/a | error card with Brave flag instructions |
