# Update: Keep the Web Layout Phone-Sized on Mobile Chrome

## Date
2026-08-24

## Affected Spec
[home-page](../specs/home-page.md) — *Platform differences* (web entrypoint), and
[bonsai-list-feature](../specs/bonsai-list-feature.md) — `AddBonsaiScreen`.
Follows [2026-07-25-fix-android-chrome-viewport](./2026-07-25-fix-android-chrome-viewport.md)
and [2026-07-25-fix-web-black-screen](./2026-07-25-fix-web-black-screen.md).

## Reason
On https://zomzog.fr/my-little-bonsai/ on Android Chrome, typing a name into the
add form left the page zoomed in with the right-hand side of the form off
screen and no way back to a fitting view.

Reconstructing the reported screenshot from known dp sizes pins the cause. The
outlined text fields measure 97 device px for a 56 dp box and the outlined
buttons 68 px for 40 dp, i.e. the scene is drawn at 1.72 device px per dp; the
"Purchase date" label — centred in a `fillMaxWidth` button — sits 830 px right
of the button's left border, which puts the button at 967 dp and the scene at
**~980 dp wide**. 980 CSS px is Chrome's desktop fallback layout viewport, the
width it lays a page out at when it does not honour `width=device-width`
(“Desktop site” for the site, or a page cached from before the meta tag
existed). The same 980 × 1860 signature was measured in the July report, so the
viewport meta added then never took effect on that device.

From there the rest follows: `ComposeViewport(document.body!!)` measures a body
that is 980 CSS px wide, Compose sizes a scene of 980 dp for it, and the whole
app is drawn at roughly a third of its intended size — small enough that
reading or typing requires zooming, and zooming clips a canvas that cannot
reflow. The form made it worse by not scrolling: whatever the zoom or the
keyboard pushed below the fold was unreachable.

The host page cannot force a browser in desktop mode to honour the viewport
meta, so the app has to stay usable when it is ignored.

## Change Description
- `composeApp/src/commonMain/.../ui/layout/TouchLayoutScale.kt` (new):
  `touchLayoutScale()` caps the logical width of a coarse-pointer viewport at
  `MAX_TOUCH_LAYOUT_WIDTH_DP` (480 dp); the `TouchLayoutScale` composable
  applies the cap by scaling the density its content is composed with. The
  window density is untouched, so pointer and accessibility coordinates keep
  matching the canvas. A 980 CSS px viewport is laid out as 480 dp stretched
  across the screen — phone proportions, at twice the size.
- `composeApp/src/wasmJsMain/.../main.kt`: wraps `App()` in `TouchLayoutScale`.
  Touch is detected from the input device — `(pointer: coarse)`, or touch points
  plus `(hover: none)` — because a browser in desktop mode misreports the
  viewport width but not the hardware. A laptop with a touchscreen keeps
  `hover: hover` and is left alone.
- `composeApp/src/commonMain/.../ui/addbonsai/AddBonsaiScreen.kt`: the form
  column is `verticalScroll` + `imePadding`, so every field and the Add button
  stay reachable when the visible area is shorter than the form — a zoomed page
  on web, the keyboard on Android.
- `composeApp/src/wasmJsMain/resources/index.html`: the viewport meta adds
  `interactive-widget=resizes-content`, so the virtual keyboard shrinks the
  layout viewport and Compose re-measures and scrolls the focused field into
  view instead of the browser panning a canvas that cannot reflow.
- Tests: `TouchLayoutScaleTest` covers the cap and the density it composes with;
  `AddBonsaiScreenTest` reaches the Add button through a viewport too short for
  the form; `e2e/tests/viewport.spec.ts` asserts a desktop-width touch viewport
  is laid out at phone scale and that a mouse-driven window is not rescaled.

### Platform differences
The layout cap is web-only: Android windows report their real size, so nothing
about the Android build changes. The scrollable, IME-padded form is shared, and
is what the web build now mirrors.

## Migration / Impact
No data or API changes. A phone-sized web viewport (≤ 480 dp) renders exactly as
before. A coarse-pointer viewport wider than 480 dp — a browser in desktop mode,
or a tablet — now renders a scaled-up phone layout instead of a stretched one.

## Open Questions
- Why Chrome ignores `width=device-width` on the reporter's device is not
  confirmed from here; "Desktop site" enabled for the site and a stale cached
  page both produce the measured 980 dp. Turning "Desktop site" off for
  zomzog.fr, or a hard reload, is worth checking on-device — the change above
  makes the app usable either way.
