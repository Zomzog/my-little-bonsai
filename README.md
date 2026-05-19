# My Little Bonsai

The bonsai life tracker — a Kotlin Compose Multiplatform app for Android and Web.

[![CI](https://github.com/zomzog/my-little-bonsai/actions/workflows/ci.yml/badge.svg)](https://github.com/zomzog/my-little-bonsai/actions/workflows/ci.yml)
[![Deploy Web](https://github.com/zomzog/my-little-bonsai/actions/workflows/deploy-web.yml/badge.svg)](https://github.com/zomzog/my-little-bonsai/actions/workflows/deploy-web.yml)

## Stack

- Kotlin 2.3.20, Compose Multiplatform 1.10.1, AGP 9.2.1
- JDK 25 (toolchain & bytecode target)
- Targets: Android (`androidApp`) + Web/wasmJs (`composeApp`)
- Tests: Compose UI test + assertk; mockk on JVM where useful
- Coverage: Kover (enforced 100% via `koverVerify`)
- CI: GitHub Actions (tests + coverage + Android APK + wasmJs distribution + Pages deploy)
- Dependencies: Dependabot (gradle + github-actions, weekly)

## Module layout

```
my-little-bonsai/
├── composeApp/   # Shared KMP library: commonMain UI/logic, androidMain glue, wasmJsMain entry, jvm test backend
├── androidApp/   # Thin Android application shell
├── spec-kit/     # Living design records (one spec per feature, updates as deltas)
└── .agents/      # Agent skills (git submodule)
```

## Running

### Web (local dev)

```bash
./gradlew :composeApp:wasmJsBrowserDevelopmentRun
```

### Android (debug APK)

```bash
./gradlew :androidApp:assembleDebug
```

### Tests & coverage

```bash
./gradlew :composeApp:allTests
./gradlew :composeApp:koverXmlReport :composeApp:koverVerify
```

The Kover verify task fails the build if coverage drops below 100% on non-excluded code (entry points, generated Compose Resources, and Compose synthetic classes are excluded — see `composeApp/build.gradle.kts`).

## Web deployment

The `deploy-web.yml` workflow publishes the wasmJs distribution to GitHub Pages on every push to `main`. Enable Pages in the repository settings with "GitHub Actions" as the source for this to work.

## Specs

Every feature has a spec under `spec-kit/specs/`. See `.agents/AGENTS.md` for the project workflow and templates.
