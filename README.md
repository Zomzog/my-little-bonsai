# My Little Bonsai

[![CI](https://github.com/zomzog/my-little-bonsai/actions/workflows/ci.yml/badge.svg)](https://github.com/zomzog/my-little-bonsai/actions/workflows/ci.yml)
[![Deploy Web](https://github.com/zomzog/my-little-bonsai/actions/workflows/deploy-web.yml/badge.svg)](https://github.com/zomzog/my-little-bonsai/actions/workflows/deploy-web.yml)
[![GitHub release](https://img.shields.io/github/v/release/zomzog/my-little-bonsai?include_prereleases)](https://github.com/zomzog/my-little-bonsai/releases)

A Kotlin Multiplatform Compose application targeting **Android** and **WebAssembly (wasmJs)**.

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin 2.3.21 |
| UI | Compose Multiplatform 1.10.3 |
| Platforms | Android (minSdk 29) · Web (wasmJs) |
| Build | Gradle 9.5 |
| JDK | 25 (Temurin) |
| Tests | kotlin.test · [assertk](https://github.com/willowtreeapps/assertk) · [mockk](https://mockk.io) (Android unit tests) |

## Prerequisites

- **JDK 25** — [Temurin distribution](https://adoptium.net/) recommended
- **Android SDK** — set `sdk.dir` in `local.properties` (see `local.properties.example`)

## Running Locally

```bash
# Clone with submodules
git clone --recurse-submodules https://github.com/zomzog/my-little-bonsai.git
cd my-little-bonsai

# Copy and fill in your Android SDK path
cp local.properties.example local.properties

# Run the web version in the browser (http://localhost:8080)
./gradlew :composeApp:wasmJsBrowserDevelopmentRun

# Build the Android debug APK
./gradlew :composeApp:assembleDebug
# Output: composeApp/build/outputs/apk/debug/

# Build the production web distribution
./gradlew :composeApp:wasmJsBrowserDistribution
# Output: composeApp/build/dist/wasmJs/productionExecutable/
```

## Running Tests

```bash
./gradlew :composeApp:allTests --continue
# Reports: composeApp/build/reports/tests/
```

Tests use **assertk** for assertions (multiplatform-compatible) and **mockk** for mocking in Android-specific unit tests.

## CI / CD

GitHub Actions runs on every push and pull request — see [`.github/workflows/ci.yml`](.github/workflows/ci.yml).

| Job | What it does |
|---|---|
| `Run Tests` | Runs `allTests` on all platforms, uploads test reports |
| `Build Android APK` | Assembles a debug APK, uploads the artifact |
| `Build Web (wasmJs)` | Builds the wasmJs distribution, uploads the artifact |

Web deployments to **GitHub Pages** are triggered on every push to `main` via [`.github/workflows/deploy-web.yml`](.github/workflows/deploy-web.yml).

## Agent / Claude Code Integration

The repository contains a `.agents/` directory with Claude Code skills stored as git submodules:

```
.agents/
└── compose-skill/   ← Compose-specific Claude skill (submodule)
```

`.claude` is a symlink pointing to `.agents/`, so Claude Code picks up all skills automatically without duplicating files:

```
.claude -> .agents
```

This means any skill placed inside `.agents/` is visible to both the Claude Code CLI (via `.claude`) and any other agent tooling that reads `.agents/`.

To add a new skill, add it as a submodule under `.agents/`:

```bash
git submodule add <skill-repo-url> .agents/<skill-name>
```
