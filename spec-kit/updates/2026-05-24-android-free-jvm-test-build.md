# Update: Android-Free JVM Test Build

## Date
2026-05-24

## Affected Spec
N/A — build infrastructure change only.

## Reason
The Claude Code remote execution environment does not have an Android SDK installed
(`ANDROID_HOME` / `ANDROID_SDK_ROOT` are unset) and its network policy blocks Google
Maven (`dl.google.com`, HTTP 403). The Android Gradle Plugin and the Compose Multiplatform
JVM runtime (`androidx.compose.runtime:runtime`, published by JetBrains to Google Maven
exclusively) are both unreachable. This made it impossible to compile or test anything
in the remote session.

## Change Description

### Auto-detection
`settings.gradle.kts` and every `build.gradle.kts` already auto-detect the SDK via
`ANDROID_HOME` / `ANDROID_SDK_ROOT`. No manual flags are needed.

### When `ANDROID_HOME` / `ANDROID_SDK_ROOT` are **absent** (e.g. Claude Code remote)

| What changes | How |
|---|---|
| `google()` repo removed | `settings.gradle.kts` — both `pluginManagement` and `dependencyResolutionManagement` |
| Android plugins not declared | root `build.gradle.kts` |
| `:androidApp` module not included | `settings.gradle.kts` |
| `androidMultiplatformLibrary`, `composeMultiplatform`, `composeCompiler` plugins not applied | `composeApp/build.gradle.kts` |
| Compose source files excluded from `commonMain` | `App.kt`, `HomeScreen.kt`, `AddBonsaiScreen.kt`, `BonsaiDatePickerDialog.kt` (expect), `BonsaiListScreen.kt` |
| Compose test files excluded from `commonTest` | `AppNavigationTest.kt`, `HomeScreenTest.kt`, `AddBonsaiScreenTest.kt`, `BonsaiListScreenTest.kt` |
| `BonsaiDatePickerDialog.kt` actual excluded from `jvmMain` | avoids orphaned `actual` without an `expect` |
| Compose dependencies removed from `commonMain` / `commonTest` / `jvmMain` | `compose.runtime`, `compose.foundation`, etc. |

### Pure-Kotlin extraction
`AddBonsaiFormModel.kt` (new) contains the constants, `AddBonsaiFormState`, `ValidationResult`,
and `validate()` that were previously embedded in `AddBonsaiScreen.kt`. This lets `ValidateTest`
compile and run without the Compose dependency. `AddBonsaiScreen.kt` now contains only the
`@Composable` UI function.

### Groovy shims
* `composeApp/android-target.gradle` — Android KMP target config (types not available when AGP absent).
* `composeApp/jvm-compose-dependencies.gradle` — `compose.desktop.currentOs` for jvmMain (type not available when `composeMultiplatform` absent).

### Result
`./gradlew jvmTest` runs 14 pure-Kotlin tests (5 × `InMemoryBonsaiRepositoryTest`, 9 × `ValidateTest`)
in the restricted environment, with no network access to Google Maven required.

### When `ANDROID_HOME` / `ANDROID_SDK_ROOT` are **present** (CI, local dev with SDK)
All targets, plugins, and dependencies are included as before. Full Compose + Android build
runs normally.

## Migration / Impact
No breaking changes to app behaviour or the public API surface. CI (GitHub Actions ubuntu-latest)
has `ANDROID_HOME` set, so the full build path is exercised there.
