# Update: Fix Web Folder Picker — Honour Browser Transient Activation

## Date
2026-05-27

## Affected Spec
[local-folder-storage](../specs/local-folder-storage.md)

## Reason
`window.showDirectoryPicker()` requires transient user activation: it must be
called synchronously within the call stack of a user gesture. The previous
implementation launched a coroutine before calling `startPickerJs()`, which
exited the gesture context before the picker API was invoked. The browser
silently blocked the call, so the "Choose Folder" button did nothing on web.

## Change Description
- `WebFolderStorageManager.pickFolder()` is removed and replaced with two
  separate methods:
  - `startPicker()` (non-suspend) — calls `startPickerJs()` synchronously and
    must be invoked on the user-gesture call stack.
  - `awaitPickerResult(): Boolean` (suspend) — polls `isPickerDoneJs()` with
    `delay()` and returns `true` on success, `false` on cancel or error.
- `wasmJsMain/FolderPickerSupport.kt` updated: in the `WebFolderStorageManager`
  branch of `launch()`, `startPicker()` is called synchronously first, then a
  coroutine is launched that calls `awaitPickerResult()` and dispatches
  `onGranted()` or `onDenied()` accordingly.

## Migration / Impact
No breaking changes for end users. The picker now appears correctly when the
user taps "Choose Folder" in the web app. No test changes are required:
`commonTest` uses `FakeFolderStorageManager` (the `else` branch in
`FolderPickerSupport.kt`) and is unaffected. There is no `wasmJsTest` source
set; the browser API is not unit-testable.
