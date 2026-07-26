# Update: Fix Black Screen on the Deployed Web Build

## Date
2026-07-25

## Affected Spec
[home-page](../specs/home-page.md), amending
[2026-07-25-fix-android-chrome-viewport](./2026-07-25-fix-android-chrome-viewport.md).

## Reason
The viewport fix deployed to https://zomzog.fr/my-little-bonsai/ left the site
showing a black screen on Android Chrome — a regression worse than the clipped
layout it was meant to fix.

That change introduced two things: host-page CSS (no scrolling, no zooming,
theme-aware background) and a structural change — mounting Compose into a new
`#composeTarget` element styled `position: fixed; inset: 0` instead of into
`<body>`.

CI could not reproduce the failure: the Playwright suite, including the Pixel 5
project added by the same change, passed against the exact deployed build, so
the bundle mounts and renders under Chromium emulation. The failure only
appears on a physical device, which rules out the CSS that merely constrains
the document and leaves the mount path — a WebGL canvas moved inside a
fixed-position, shadow-DOM subtree — as the only change that can produce an
empty app.

The root cause on-device is not confirmed; this change removes the only
candidate rather than proving it.

## Change Description
- `main.kt`: back to `ComposeViewport(document.body!!)`.
- `index.html`: the `#composeTarget` container is gone. `<body>` is still held
  at exactly the viewport (`width`/`height: 100%`, `overflow: hidden`,
  `overscroll-behavior: none`), which keeps the guarantee the container was
  introduced for — Compose measures a box pinned to the viewport, and the
  canvas can no longer make the document scroll — without relocating the
  canvas into a fixed-position layer.
- `index.html`: added a `#bootstrap` "Loading…" element, which Compose removes
  when it clears its container on mount, plus a `window.onerror` handler that
  writes the failure into it. A failed start-up now says so instead of showing
  an empty page.
- Retained from the previous update: pinned viewport scale, no document
  scrolling or overflow, the `resize` re-dispatch on `load`/`orientationchange`/
  `visualViewport` resize, and the `prefers-color-scheme` background.

### Platform differences
Web only.

## Migration / Impact
No data or API changes. The e2e viewport assertions are unchanged and still
hold: the canvas fills the viewport exactly, the document does not overflow,
and the canvas follows a viewport resize.

## Open Questions
- Why the fixed-position container blanked the canvas on-device is unverified.
  Confirming it needs a physical Android device, which CI does not have.
