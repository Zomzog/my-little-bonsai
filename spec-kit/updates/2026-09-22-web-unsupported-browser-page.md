# Update: Web Shows an Unsupported-Browser Page Outside Chromium

## Date
2026-09-22

## Affected Spec
[local-folder-storage](../specs/local-folder-storage.md)

## Reason
[#76](https://github.com/Zomzog/my-little-bonsai/issues/76) moves Web storage to a real
vault folder via the File System Access API, and that API only exists in Chromium-based
desktop browsers ([MDN](https://developer.mozilla.org/en-US/docs/Web/API/Window/showDirectoryPicker),
[caniuse](https://caniuse.com/native-filesystem-api)). Rather than let the app boot and
fail confusingly on Firefox, Safari, or Chrome for Android, it should say so up front.
This update covers the gate itself ([#78](https://github.com/Zomzog/my-little-bonsai/issues/78));
the File System Access vault repository behind it is tracked separately in
[#77](https://github.com/Zomzog/my-little-bonsai/issues/77).

## Change Description

### Web
- `wasmJsMain/main.kt` checks `'showDirectoryPicker' in window` before composing the
  app. Detection is by feature, never by user-agent string, so any current or future
  Chromium-based browser that ships the API works automatically.
- When the API is missing, `ComposeViewport` renders `UnsupportedBrowserScreen` instead
  of `App()`: it explains that the browser lacks a required feature, lists the supported
  browsers (Chrome, Edge, other Chromium desktop browsers), and links to the mobile app.
- The mobile app link is a placeholder (`https://www.youtube.com/watch?v=dQw4w9WgXcQ`)
  until the app is published ([#15](https://github.com/Zomzog/my-little-bonsai/issues/15));
  replacing it is tracked in Future work.
- The page is bilingual: `navigator.language` picks between English and French strings
  (`UnsupportedBrowserScreen.kt`, `commonMain`), falling back to English for any other tag.

### Shared
- `UnsupportedBrowserScreen`/`UnsupportedBrowserContent` live in `commonMain` so the
  stateless content composable can be unit-tested like the rest of the app's screens
  (`FolderSetupScreen` is the template followed). Android is unaffected — SAF is
  available on every Android version the app targets, so the gate is wired up in
  `wasmJsMain/main.kt` only.

### Platform differences (revised)

| Concern | Android | Web (Wasm/JS) |
|---|---|---|
| Support check | None — SAF is always available | `'showDirectoryPicker' in window` at startup |
| Unsupported outcome | N/A | Dedicated `UnsupportedBrowserScreen`, app never composes |

## Migration / Impact
- Firefox, Safari, and mobile Chrome users on Web now see the unsupported-browser page
  instead of the app. Until now Web ran everywhere via `localStorage`
  ([2026-07-25 update](2026-07-25-web-browser-storage.md)); any data such a user already
  stored there becomes unreachable through the app UI. This is accepted as part of the
  pre-1.0 move to a real vault (no migration path is offered, matching the "no migration"
  decision already made for that change).
- No impact on Android or on Chromium-based desktop browsers.

## Open Questions
- None — remaining work (the actual vault read/write) is tracked in #77.
