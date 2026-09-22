# Spec: Bonsai Tracker v1 — Global Plan (Epic)

## Status
Draft

## Goal
Turn My Little Bonsai into a real bonsai life tracker. The user keeps a collection of
bonsais and records everything done to them: work sessions, measurements, repotting and
substrates, photos. All data lives in a **Markdown + YAML vault** that the user owns and
can open with tools like [Obsidian](https://obsidian.md). Nothing leaves the device. Sync
between devices is the user's job, done with their own tool (Syncthing, Obsidian Sync, a
cloud drive folder…).

This document is the functional plan behind the epic issue. It lists the functional
and non-functional requirements and the decisions taken, and it flags where the
technology may limit us. Each child issue gets its own detailed spec (or update) when
work starts on it, as `.claude/CLAUDE.md` requires.

## Scope
- In scope (v1): everything in the [issue breakdown](#issue-breakdown) except the
  *Future work* issue.
- Out of scope (v1), each item is tracked under *Future work*:
  - Reminders / notifications (watering, repotting season…)
  - Tolerant vault parsing (v1 is strict)
  - Referencing external pictures instead of copying them into the vault
  - Web support beyond Chromium browsers
  - Real store link instead of the placeholder (see existing issue #15)
  - Versioning of substrate compositions
  - "Last activity" filter by action type, species categories, pot inventory
- Non-goals: built-in sync, accounts, any server or cloud component, runtime AI generation.

## Decisions log (from the planning Q&A)

| # | Topic | Decision |
|---|---|---|
| D1 | Web storage | Web writes to a real vault folder with the File System Access API. It is **Chromium only** (Chrome/Edge desktop). Other browsers get an "unsupported" page listing the supported browsers and a link to the mobile app. Until the app is published, that link is a placeholder (a Rick roll). |
| D2 | Pictures | Copied **into the vault**. Referencing external pictures is future work. |
| D3 | Sync | Not the app's job. |
| D4 | Vault layout | One folder per bonsai, one note per work session, attachments next to them. |
| D5 | Event model | A **work session** (one date) holds **several actions**. |
| D6 | Typed actions | Measuring → size, Repotting → substrate + pot, Fertilizing → product + dose, Treatment → product + target. |
| D7 | Lifecycle | Archive with a reason (dead / sold / gifted / lost / other) **and** hard delete. An archive list lets the user restore or delete archived bonsais. |
| D8 | Reference lists | Defaults ship inside the app. The vault holds an overlay YAML with the user's additions and removed defaults. |
| D9 | Removing items | To the user it is always "remove". For a default entry, "remove" means adding it to the overlay's removed list, because defaults can't be edited. Removing is **blocked while the item is in use**: the screen shows a usage count, and tapping it lists the bonsais that use the item. |
| D10 | Managed lists | Species, actions, soils, styles, fertilizers, treatments, pots (and substrates, D11). |
| D11 | Substrates | Compositions of soils with percentages that add up to 100%. There are two forms: **named reusable mixes** (a managed list) and **inline mixes** typed for one bonsai or one repotting, so the user doesn't have to create a named mix every time. When a named mix that is in use changes, the app offers to **keep history** (existing usages become inline copies of the old composition) or **apply everywhere**. Full versioning of compositions is future work. |
| D12 | Style picture | Pre-made illustrations are bundled for the default styles. A user-added style gets a picture uploaded by the user or a placeholder. No runtime AI. |
| D13 | Age | Either a **birthday** (computed age) or a **frozen number in years only**, entered by the user, which does not grow. Anything more precise than years is a birthday. Display: days if under 2 months, months if under 24 months, then years. |
| D14 | Units | Stored as metric. Metric or imperial display is a preference. |
| D15 | Language | English and French, following the device locale. Defaults are translated. Species are keyed by Latin name. |
| D16 | Main page | Cards with cover photo, name, species, age, last size and last activity. Fuzzy search on name. Filters: species, style, height range, age range, last activity. Sort: name, age, height, last activity, date added. "Last activity" means any action in v1. |
| D17 | External edits | **Strict** in v1: an invalid vault is not opened, and the app shows a report of the invalid files. The app reloads when it is resumed or focused. Tolerant mode is future work. |
| D18 | Reminders | Out of scope, tracked as future work. |
| D19 | Photo import | User preference. **Original** by default, optional resize and re-encode. |
| D20 | Existing data | Dropped (pre-1.0). The Web `localStorage` data and the Android in-memory data are not migrated. |
| D21 | Default species | About 40 common bonsai species, each with EN/FR names. No categories in v1: the list is shown in alphabetical order. |
| D22 | Pots | A catalogue entry, not a unique object: several bonsais can reference the same pot. Each pot has a bought date that defaults to its creation date. |
| D23 | File naming | Renaming a bonsai renames its folder. Sessions are named `YYYY-MM-DD-HHmm.md`. |

## Design (functional)

### Vault layout (target, detailed in the Vault format spec)

```
<vault>/
├── metadata.yaml               # creationDate, schemaVersion
├── settings/
│   ├── preferences.yaml        # units, photo import mode, …
│   ├── species.yaml            # overlay: added entries + removed default ids
│   ├── actions.yaml
│   ├── soils.yaml
│   ├── substrates.yaml
│   ├── styles.yaml
│   ├── fertilizers.yaml
│   ├── treatments.yaml
│   └── pots.yaml
└── bonsais/
    └── <bonsai-slug>/
        ├── bonsai.md           # front-matter = profile, body = free description
        ├── sessions/
        │   └── YYYY-MM-DD-HHmm.md  # front-matter = actions, body = notes
        └── attachments/
            └── *.jpg|png|…
```

- Structured data goes in YAML front-matter, which Obsidian shows as note *Properties*
  ([Obsidian help — Properties](https://help.obsidian.md/properties)). Free text is the
  Markdown body.
- Pictures are embedded as `![[attachments/…]]`, so they render in Obsidian
  ([Obsidian help — Embed files](https://help.obsidian.md/embeds)).
- Every entity has a stable `id` in its front-matter, and references between entities
  use ids. Renaming a bonsai renames its folder (D23).

### Managed reference lists (shared behaviour)
Species, actions, soils, substrates, styles, fertilizers, treatments and pots all behave
the same way:
- The effective list is the app's defaults, plus the user's additions, minus the removed
  defaults.
- Each list has a dedicated management page or pop-up with add, edit (user entries only)
  and remove.
- Remove is blocked while the item is referenced. The page shows "used by N" and tapping
  it lists the bonsais. A soil used by a named or an inline substrate mix counts as in use.
- Default entries have stable ids and EN/FR labels. User entries have the user's label only.
- App updates can add new defaults without touching the user's overlay.

### Bonsai profile
Name (required), species, style (with its illustration), current substrate, current pot,
age (birthday or frozen number), cover photo, free Markdown description with photos,
date added, status (active / archived + reason + date).

### Work sessions & timeline
- A session has a date, one or more actions, optional notes and optional photos.
- Typed actions carry structured data (D6). Other actions are only a label, with optional notes.
- A repotting session updates the bonsai's current substrate and pot. A measuring
  session adds a size record.
- The bonsai page shows a timeline of its sessions, newest first. Tapping a session
  opens its details.

### Size
Height, width, nebari circumference and weight, **all optional**, recorded by a
measuring action. The main page shows the latest value of each field. A size history view
is available on the bonsai page. Values are stored metric (cm, g).

### Age display rule
Let `d` be the age in days, computed from the birthday or taken from the frozen number
(converted to days):
- under 2 months → "N days"
- under 24 months → "N months"
- otherwise → "N years"

### Pictures
- Pictures can be added to the bonsai description or to a session.
- Sources: file upload on every platform, and the camera on a phone.
- Stored in the bonsai's `attachments/` folder. Import mode is a preference: *original*
  (default) or *resized* (long edge limit + JPEG re-encode).

## Non-functional requirements
- **Offline and private**: no network calls, and data never leaves the device (keeps the
  current Local Folder Storage promise).
- **Portability**: the vault is plain files and readable without the app. Obsidian must
  show profiles, sessions and photos meaningfully.
- **Data safety**: writes are atomic where the platform allows (write to a temp file,
  then rename). The app never deletes user files it does not own, and deletion always
  asks for confirmation.
- **Strict validation (v1)**: files with an invalid schema block the vault from opening,
  and the report names each file and the problem.
- **Performance**: the main page stays usable with about 200 bonsais and 5,000 sessions.
  Target: search and filtering respond in under 100 ms after the vault is loaded.
- **Quality**: 100% line coverage target (Kover minimum 95%), per `.claude/CLAUDE.md`.
- **i18n**: EN and FR, and no hard-coded user-facing strings.
- **Accessibility**: content descriptions on images, and touch targets of at least 48 dp.

## Technology risks (to verify in spikes)

| Risk | Why | Mitigation |
|---|---|---|
| File System Access API limited to Chromium desktop | `showDirectoryPicker()` is not implemented in Firefox or Safari, nor on Chrome for Android ([MDN — showDirectoryPicker](https://developer.mozilla.org/en-US/docs/Web/API/Window/showDirectoryPicker), [caniuse](https://caniuse.com/native-filesystem-api)) | D1: unsupported-browser page + future-work spike |
| No file-change notifications | SAF has no directory watcher. The web `FileSystemObserver` is experimental | Reload on resume / focus (D17) |
| SAF performance on many files | `DocumentsContract` queries are slow on large trees | Spike: in-memory index built at load, with incremental updates |
| YAML on wasmJs | Needs a multiplatform YAML library that also targets wasmJs | Spike: evaluate kaml / other libs, or fall back to a small in-house front-matter parser |
| Camera on Web (Wasm) | Compose Multiplatform has no camera API | `<input type="file" accept="image/*" capture>` through JS interop |
| Image resize / EXIF on both targets | Android: `Bitmap`/`ExifInterface`. Web: Canvas through interop | Spike, only needed for the optional *resized* mode (D19) |
| Markdown rendering in Compose (Android + wasm) | A renderer library must support wasmJs | Spike: evaluate a multiplatform Markdown renderer, or render only a plain-text subset in v1 |

## Issue breakdown

The epic issue groups the child issues below. Sub-issues are used only where a child is
too large for one PR.

Epic: #73

1. #74 **Vault format specification**: layout, front-matter schemas, ids, `schemaVersion`,
   file naming.
2. #75 **Android vault storage**: repository on SAF, replacing the in-memory repository.
3. #76 **Web vault storage (Chromium)**
   - #77 File System Access vault repository (replaces `localStorage`, drops old data)
   - #78 Unsupported-browser page (supported browsers + store link placeholder)
4. #79 **Strict vault validation & reload**: invalid-vault report, reload on resume/focus.
5. #80 **Managed reference lists engine**: overlay YAML, add/remove, in-use check, usage
   list, generic management page.
6. #81 **Default catalogues**
   - #82 Species (~40, EN/FR, alphabetical)
   - #83 Styles + bundled illustrations
   - #84 Actions, soils, fertilizers, treatments, pots
7. #85 **Substrates**: named and inline soil mixes that total 100%, and what happens when a used mix is edited.
8. #86 **Bonsai profile & lifecycle**
   - #87 Profile create/edit page
   - #88 Age (birthday / frozen) + display rule
   - #89 Archive with reason, archive list, restore, hard delete
9. #90 **Work sessions & timeline**
   - #91 Session model + editor (multi-action)
   - #92 Typed actions (measuring, repotting, fertilizing, treatment)
   - #93 Timeline + session details
10. #94 **Size tracking**: last size, size history, units preference.
11. #95 **Pictures**
    - #96 Spike: camera, image processing, YAML & Markdown libs on Android + Web
    - #97 Import into the vault (upload) + import-mode preference
    - #98 Camera capture on phone
    - #99 Display: cover photo, gallery, photos in description and sessions
12. #100 **Collection main page**
    - #101 Cards + responsive grid/list
    - #102 Fuzzy search by name
    - #103 Filters & sort
13. #104 **Preferences & i18n (EN/FR)**: `preferences.yaml`, settings page, translations.
14. #105 **Future work (out of scope for v1)**
    - #106 Tolerant vault parsing
    - #107 External picture references (user choice)
    - #108 Web support beyond Chromium
    - #109 Reminders & notifications
    - #110 Replace the store placeholder link (relates to #15)
    - #112 Versioning of substrate compositions
    - #113 "Last activity" filter by action type
    - #114 Species categories
    - #115 Pot inventory: unused pots & quantities

### Suggested order
#74 → (#75, #76) → #79 → #80 → #81 → #85 → #86 → #104 → #90 → #94 → #95 → #100.
Start the #96 spike early, because its findings may change other issues.
#74 blocks everything that touches files. #80 blocks #81, #85, #86 and #90.

## Acceptance Criteria
- [x] Epic issue and child issues created on GitHub and linked as sub-issues
- [ ] Every child issue has its own spec or update in `spec-kit/` before implementation
- [ ] All v1 child issues closed
- [ ] A vault created by the app opens in Obsidian and shows profiles, sessions and photos
- [ ] Coverage stays at the 100% target

## Resolved Questions (PR #111 review)
- **Renaming a bonsai renames its folder**, so the vault stays clean (Obsidian renames the
  file when a note is renamed). References use ids and embeds are relative, so nothing breaks.
- **Session file name**: `YYYY-MM-DD-HHmm.md` (creation time to the minute). Only in the
  rare case of two sessions created in the same minute is a `-2` suffix added.
- **Frozen age**: years only. Anything more precise (a month or a day) is a birthday.
- **"Last activity" filter**: any action in v1. Filtering by action type is future work (#113).
- **Species categories**: none in v1, and the species list is alphabetical. Categories
  are future work (#114).
- **Pots**: a catalogue entry, not a unique object, and several bonsais can reference the
  same pot. Each pot has a bought date that defaults to the date it was created. Unused-pot
  tracking and quantities are future work (#115).

## Open Questions
- Birthday: can it be partial (year + month without a day)? If so, what day is assumed?
