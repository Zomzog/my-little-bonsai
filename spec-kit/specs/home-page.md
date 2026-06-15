# Spec: Home Page

## Status
Implemented

## Goal
First screen of the app: centred bonsai illustration with title "My Little Bonsai" and tagline "The bonsai life tracker". Visual identity introduction and placeholder for the future bonsai-tracking dashboard.

## Scope
- In scope: `HomeScreen` composable, transparent bonsai PNG via Compose Resources, Material 3 light/dark theming, Android and Web entrypoints.
- Out of scope: persistence, bonsai data model, navigation, settings, custom palette/typography, localisation.

## Design
- `App()` wraps `HomeScreen()` in `MaterialTheme` with system light/dark color scheme.
- `HomeScreen()`: centred column with 280 dp bonsai image, 16 dp spacer, `displaySmall` title, 8 dp spacer, `titleMedium` tagline.
- Image: JPG converted to transparent PNG via ImageMagick (`-fuzz 15% -transparent white -trim`), committed as `composeApp/src/commonMain/composeResources/drawable/bonsai.png`.

### Platform differences
Same `App()` on both targets; only entrypoint glue differs (`MainActivity.setContent` on Android, `ComposeViewport(document.body!!)` on Web).

## Acceptance Criteria
- [x] Bonsai image renders with transparent background on both Android and Web
- [x] Title "My Little Bonsai" appears below the image
- [x] Subtitle "The bonsai life tracker" appears below the title
- [x] Layout adapts to system light/dark mode
- [x] `./gradlew :composeApp:allTests` passes
- [x] `./gradlew :composeApp:koverVerify` reports 100% coverage on non-excluded code
- [x] CI builds Android APK and wasmJs distribution successfully
- [x] `main` auto-deploys web build to GitHub Pages
