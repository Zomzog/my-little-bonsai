# Update: Add Playwright E2E Tests for Folder Picker Flow

## Date
2026-05-28

## Affected Spec
[local-folder-storage](../specs/local-folder-storage.md)

## Reason
The folder-picker flow on web involves browser APIs (`window.showDirectoryPicker`)
that cannot be exercised by Compose UI unit tests (commonTest/jvmTest). End-to-end
tests running in a real browser give much stronger confidence that the full click →
picker → navigation flow works correctly after the transient-activation fix.

## Change Description
Added a Playwright test suite under `e2e/` covering:
- **FolderSetupScreen is reachable** from the HomeScreen click
- **Choosing a folder navigates to BonsaiList** — the mock picker resolves
  immediately; asserts "My Bonsais" is visible afterwards
- **Cancelling the picker stays on FolderSetupScreen** — mock rejects with
  `AbortError`; asserts the "Choose Folder" button is still visible and
  "My Bonsais" is not shown

`window.showDirectoryPicker` is replaced via Playwright's `addInitScript` before
the Wasm runtime loads, so the tests run without real OS dialogs and without
needing user-gesture activation.

A new `e2e-web` job was added to `.github/workflows/ci.yml`. It builds the
Wasm distribution and then runs the Playwright tests against a locally served
copy of the app.

## Migration / Impact
No production code changes. Tests run in Chromium only (Firefox does not
support the File System Access API; that is a known platform limitation, not
a bug to fix here).
