# My Little Bonsai — Agent Guide

## Project

**My Little Bonsai** is a Kotlin Compose Multiplatform (KMP) app targeting:

- **Android** — native Android application module (`androidApp/`)
- **Web** — Wasm/JS build served as a browser app (`composeApp/`)

The shared UI and business logic live in `composeApp/` as a KMP library.

## Repository Layout

```
my-little-bonsai/
├── composeApp/          # KMP shared module (UI, domain, data)
│   └── src/
│       ├── commonMain/  # shared Kotlin / Compose code
│       ├── androidMain/ # Android-specific actuals
│       └── wasmJsMain/  # Web-specific actuals
├── androidApp/          # Android application shell (depends on composeApp)
├── spec-kit/            # Living design record (see below)
│   ├── specs/           # Feature specifications
│   └── updates/         # Change records for existing specs
└── .agents/             # Agent skills (git submodules)
    └── compose-skill/
```

## Tech Stack

| Layer | Choice |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose / Compose Multiplatform |
| Build | Gradle (Kotlin DSL) |
| Test | JUnit + assertk + mockk |
| Targets | Android (AGP), Wasm/JS (browser) |

## Spec Kit — How We Work

Every feature or non-trivial change must be documented in `spec-kit/` **before or alongside** the code change.

### Adding a new feature → create a spec

Create `spec-kit/specs/<feature-slug>.md` using this template:

```markdown
# Spec: <Feature Name>

## Status
Draft | Accepted | Implemented | Deprecated

## Goal
One-paragraph description of what and why.

## Scope
- In scope: …
- Out of scope: …

## Design
Describe the approach, key data structures, UI behaviour, platform differences.

## Acceptance Criteria
- [ ] criterion 1
- [ ] criterion 2

## Open Questions
- …
```

### Changing existing behaviour → create an update

Create `spec-kit/updates/<YYYY-MM-DD>-<slug>.md` using this template:

```markdown
# Update: <Short Title>

## Date
YYYY-MM-DD

## Affected Spec
Link to the spec being updated (e.g. [bonsai-list](../specs/bonsai-list.md))

## Reason
Why is this change being made?

## Change Description
What exactly changes (behaviour, API, UI, data model).

## Migration / Impact
Any breaking changes or steps required for existing data / users.
```

### Rules

1. **No undocumented features.** If it ships, it has a spec.
2. **Specs are living documents.** Update the status field when implementation is complete.
3. **Updates, not rewrites.** Don't silently edit an accepted spec; create an update file instead.
4. **Platform differences belong in the spec.** If Android and Web behave differently, call it out explicitly.

## Development Workflow

1. Fetch `main` and branch off (`feat/<slug>`, `fix/<slug>`, etc.).
2. Write or update a spec/update file in `spec-kit/`.
3. Implement the change in `composeApp/` (and `androidApp/` if needed).
4. Run tests: `./gradlew test`.
5. Commit with a clear message and push to origin.

## Coding Conventions

- Shared logic goes in `commonMain`; platform-specific code uses `expect`/`actual`.
- Keep `androidMain` and `wasmJsMain` as thin as possible.
- No comments unless the *why* is non-obvious.
- UI composables are stateless; state lives in ViewModels or state hoisting.
