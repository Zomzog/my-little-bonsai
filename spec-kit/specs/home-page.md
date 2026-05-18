# Spec: Home Page

## Status
Draft

## Goal
Provide the first screen of My Little Bonsai: a centred bonsai illustration with the app name "My Little Bonsai" and the tagline "The bonsai life tracker" beneath it. This is the visual identity introduction for the app and the placeholder for the future bonsai-tracking dashboard.

## Scope
- In scope:
  - A single `HomeScreen` composable in `commonMain` rendering the bonsai PNG and the two text lines.
  - A transparent bonsai PNG resource embedded via Compose Resources.
  - Material 3 theming with system light/dark support.
  - Android shell (`androidApp`) that hosts the shared `App()` composable.
  - Web (wasmJs) entrypoint serving the same `App()` composable.
- Out of scope:
  - Persistence, bonsai data model, navigation, settings — these come in later specs.
  - Custom palette/typography/launcher icon.
  - Localisation.

## Design
### UI
- `App()` wraps `HomeScreen()` in a `MaterialTheme`. Color scheme is selected from `isSystemInDarkTheme()` — light → `lightColorScheme()`, dark → `darkColorScheme()`. A `Surface(Modifier.fillMaxSize())` provides the themed background.
- `HomeScreen()` is a `Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally)` containing:
  - `Image(painterResource(Res.drawable.bonsai), contentDescription = "Bonsai illustration", modifier = Modifier.size(280.dp))`.
  - `Spacer(Modifier.height(16.dp))`.
  - `Text("My Little Bonsai", style = MaterialTheme.typography.displaySmall)`.
  - `Spacer(Modifier.height(8.dp))`.
  - `Text("The bonsai life tracker", style = MaterialTheme.typography.titleMedium)`.

### Image
The supplied bonsai illustration is a JPG with a white background. It is converted at build/asset-prep time to a transparent PNG via ImageMagick (`-fuzz 15% -transparent white -trim`) and committed as `composeApp/src/commonMain/composeResources/drawable/bonsai.png`. Compose Resources generates `Res.drawable.bonsai`.

### Platform differences
None. The same `App()` composable renders on Android and Web. Only the entrypoint glue differs:
- Android: `MainActivity` calls `setContent { App() }`.
- Web: `main.kt` calls `ComposeViewport(document.body!!) { App() }`.

## Acceptance Criteria
- [ ] Bonsai image renders with a transparent background (no white rectangle) on both Android and Web.
- [ ] Title "My Little Bonsai" appears below the image.
- [ ] Subtitle "The bonsai life tracker" appears below the title.
- [ ] Layout adapts to system light/dark mode.
- [ ] `./gradlew :composeApp:allTests` passes.
- [ ] `./gradlew :composeApp:koverVerify` reports 100% coverage on non-excluded code.
- [ ] CI (GitHub Actions) builds the Android APK and the wasmJs distribution successfully.
- [ ] `main` auto-deploys the web build to GitHub Pages.

## Open Questions
- None. (GitHub Pages must be enabled with "GitHub Actions" as the source in repo settings — operational, not design.)
