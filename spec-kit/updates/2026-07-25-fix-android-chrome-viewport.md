# Update: Fix Broken Web Layout on Android Chrome

## Date
2026-07-25

## Affected Spec
[home-page](../specs/home-page.md) — supersedes the `ComposeViewport(document.body!!)`
entrypoint described under *Platform differences*.

## Reason
On Android Chrome the app rendered against a surface much larger than the
screen: form fields ran off the right edge and the date picker dialog — which
Compose centres inside its own scene — appeared in the bottom-right corner,
clipped on two sides.

Measuring the reported screenshot (1080 × 2400 device pixels) puts the Compose
scene at roughly 980 × 1860 CSS pixels, about 1.7× the visible viewport in each
dimension, with the whole scene drawn at a scale below 1 device pixel per dp.
That is the signature of the viewport container reporting a size unrelated to
what is actually on screen.

Two properties of the host page allowed it:

- `ComposeViewport(document.body!!)` measured `<body>`, whose size depends on
  document layout — percentage heights, the page's own scroll state and the
  browser's viewport fallback all feed into it, and the canvas Compose appends
  is itself part of that layout.
- Nothing prevented the document from overflowing or being zoomed. Once content
  is wider than the viewport, mobile Chrome rescales the page, and a canvas
  cannot reflow in response.

Compose compounds this: `DefaultWindowState` measures `clientWidth`/
`clientHeight` once at start-up and only re-measures on a window `resize` or a
device-pixel-ratio change — there is no `ResizeObserver` — so a size sampled
before the mobile viewport settles is kept for the lifetime of the page.

## Change Description
- `composeApp/src/wasmJsMain/resources/index.html`
  - Added a dedicated `#composeTarget` container styled `position: fixed;
    inset: 0; overflow: hidden`, so the measured box is pinned to the viewport
    and cannot be influenced by the canvas it hosts.
  - `html, body` are now `overflow: hidden` with `overscroll-behavior: none`;
    the document can no longer scroll or overflow.
  - Viewport meta pins the scale: `maximum-scale=1.0, user-scalable=no`.
    Pinch-zoom is deliberately disabled — the Compose canvas renders at a fixed
    scale and cannot reflow when zoomed.
  - Added a small script that re-dispatches a `resize` event on `load`,
    `orientationchange` and `visualViewport` resize, so Compose re-measures once
    the mobile viewport has settled.
  - Page background now follows `prefers-color-scheme` (`#1C1B1F` in dark mode,
    matching `darkColorScheme().background`) instead of always being light, and
    declares `color-scheme: light dark`.
- `composeApp/src/wasmJsMain/kotlin/fr/zomzog/mylittlebonsai/main.kt`
  - `ComposeViewport(viewportContainerId = "composeTarget")` replaces
    `ComposeViewport(document.body!!)`.
- `e2e/tests/viewport.spec.ts` (new): asserts the canvas fills the viewport
  exactly, that the document does not overflow, and that the canvas follows a
  viewport resize.
- `e2e/playwright.config.ts`: added a `mobile-chrome` project (Pixel 5
  emulation) that runs the viewport spec, so the mobile viewport meta path is
  covered in CI.

### Platform differences
Web only. Android is unaffected — it never went through the HTML host page.

## Migration / Impact
No data or API changes. Pinch-zoom on the web build is no longer available;
this is an accessibility trade-off accepted because the canvas cannot reflow at
other scales and zooming was itself producing the broken rendering.
