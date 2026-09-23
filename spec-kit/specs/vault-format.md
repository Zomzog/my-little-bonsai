# Spec: Vault Format

## Status
Accepted

## Goal
Define the on-disk format of the bonsai vault: which files exist, where they live, what
goes in them, and how they are named. Every storage and feature issue of the
[Bonsai Tracker v1 epic](bonsai-tracker-v1-plan.md) (#73) reads or writes this format, so
it is fixed here first (#74). The vault is plain Markdown + YAML so that the user owns
their data and can open the folder in [Obsidian](https://obsidian.md) or any text editor.

A complete example lives in [`spec-kit/samples/vault/`](../samples/vault/).

## Scope
- In scope:
  - Folder layout and file naming (bonsai folders, session notes, attachments).
  - Schemas of `metadata.yaml`, `settings/*.yaml`, `bonsai.md` and session notes.
  - Ids and references, dates, units, picture embeds.
  - `schemaVersion` and the rules for changing the format.
  - Rules every reader/writer follows (encoding, unknown files, atomic writes).
- Out of scope:
  - The storage code on Android (#75) and Web (#76, #77).
  - Validation UX and the invalid-vault report (#79). This spec only lists what is
    invalid.
  - The contents of the default catalogues (#81–#84). Ids shown here are examples.
  - Markdown rendering and the picture gallery (#99).

## Design

### Principles
- **Front-matter is app data.** The YAML in `bonsai.md` and session notes is structured
  data for the app, shaped for the domain model. It is not tuned for Obsidian's
  *Properties* panel: nested values are allowed, and Obsidian shows them as raw YAML in
  Source mode ([Obsidian Help — Properties](https://obsidian.md/help/properties)).
- **The Markdown body is the user's.** Free text and picture embeds. The app never
  generates text into the body, apart from inserting an embed when the user adds a
  picture.
- **Everything is referenced by id**, never by name or path. Only pictures are referenced
  by path, relative to the bonsai folder.

### Layout

```
<vault>/
├── metadata.yaml
├── settings/
│   ├── preferences.yaml
│   ├── species.yaml
│   ├── styles.yaml
│   ├── actions.yaml
│   ├── soils.yaml
│   ├── substrates.yaml
│   ├── fertilizers.yaml
│   ├── treatments.yaml
│   ├── pots.yaml
│   └── attachments/            # pictures of user-added styles
└── bonsais/
    └── <bonsai-slug>/
        ├── bonsai.md
        ├── sessions/
        │   └── YYYY-MM-DD-HHmm.md
        └── attachments/
            └── YYYYMMDD-HHmmss-<6 hex>.<ext>
```

Only `metadata.yaml` is mandatory. Every file under `settings/` is optional: a missing
file means "defaults" (preferences) or "empty overlay" (lists). `bonsais/`,
`sessions/` and `attachments/` may be missing when empty. The app creates files and
folders when it first needs to write to them.

### General file rules
| Rule | Value |
|---|---|
| Encoding | UTF-8 without BOM. The app writes `\n` line endings and accepts `\r\n`. |
| YAML | YAML 1.2, block style. Keys are `camelCase`. |
| Front-matter | Starts on the first line with `---`, ends with the next line that is exactly `---`. The body is everything after it and may be empty. |
| Dates | ISO-8601 calendar date `YYYY-MM-DD` ([ISO 8601](https://www.iso.org/iso-8601-date-and-time-format.html)). |
| Timestamps | ISO-8601 local date-time with offset, to the second: `2026-04-12T10:30:00+02:00`. |
| Partial dates | `YYYY-MM` (only for `age.birthday`, see below). |
| Numbers | Plain YAML numbers, decimal point `.`. |
| Units | Metric only (see [Units](#units)). Imperial is a display preference. |
| Ignored entries | Any file or folder whose name starts with `.` (for example `.obsidian/`, `.trash/`, the app's temp files), at any depth. Files the app does not know about are also ignored (a `README.md` at the root, a `.pdf` in `attachments/`…). The app never deletes or rewrites them. |

**Atomic writes.** The app writes `.<name>.tmp` next to the target and then renames it
over the target, where the platform allows it. The leading dot keeps a half-written
temp file out of the vault.

### Ids
- **User entities** (bonsais, sessions, user list entries) get a random UUID v4 in
  canonical lowercase form, for example `3f6c1e0a-9b7d-4e2a-8c51-0d4e7a2b9f10`
  ([RFC 9562 §5.4](https://www.rfc-editor.org/rfc/rfc9562#section-5.4)). Kotlin
  generates it with `kotlin.uuid.Uuid.random()` in common code
  ([Kotlin API — `Uuid`](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.uuid/-uuid/)).
- **Default list entries** that ship with the app get a stable kebab-case id,
  `[a-z0-9]+(-[a-z0-9]+)*`, for example `acer-palmatum`, `moyogi` or `repotting`. Default
  ids never change once released. Species ids are the Latin name as kebab-case (D15).
- The two forms never collide, so any id can be looked up in "defaults + overlay" without
  knowing where it came from.
- Ids are never shown to the user. Renaming an entity never changes its id.

### `metadata.yaml`

```yaml
schemaVersion: 1
creationDate: 2026-05-23
```

| Key | Type | Required | Notes |
|---|---|---|---|
| `schemaVersion` | integer ≥ 1 | yes | Version of this format. `1` for v1. |
| `creationDate` | date | yes | Date the vault was created. Already written by the current app. |

**Versioning rules**
- An app supports every version up to its own. If `schemaVersion` is greater than the
  app's, the vault is not opened and the report says "created by a newer version of
  the app".
- If it is lower, the app migrates the vault step by step (`n → n+1`) before opening it,
  then writes the new `schemaVersion` last. Each migration gets its own spec update.
- v1 validation is strict (D17), so any change an older app cannot read (a new key, a
  new action type, a new enum value) bumps `schemaVersion`.
- Until v1 is released, the format may still change through spec updates and stays at
  `schemaVersion: 1`. Pre-1.0 data is dropped (D20).
- **Legacy vaults.** Folders created by the current app hold only `metadata.yaml` with
  `creationDate` and no `schemaVersion`. Those are opened as empty v1 vaults and the app
  adds `schemaVersion: 1`.

### `settings/preferences.yaml`

```yaml
units: metric
photoImport:
  mode: resized
  maxLongEdge: 2048
  jpegQuality: 85
```

| Key | Type | Default | Notes |
|---|---|---|---|
| `units` | `metric` \| `imperial` | `metric` | Display only (D14). |
| `photoImport.mode` | `original` \| `resized` | `original` | D19. |
| `photoImport.maxLongEdge` | integer px, 320–8192 | `2048` | Used only in `resized` mode. |
| `photoImport.jpegQuality` | integer, 1–100 | `85` | Used only in `resized` mode. |

Every key is optional, and a missing key takes its default. The language follows the
device locale (D15), so there is no language key.

### List overlays: `settings/<list>.yaml`
Every managed list (D10) has the same overlay shape:

```yaml
added:
  - id: 8d0f5b52-2f3e-4c8e-9a61-5b7c3e1d2a40
    name: Olivier de Provence
removed:
  - ulmus-parvifolia
```

| Key | Type | Notes |
|---|---|---|
| `added` | list of entries | The user's own entries. They are editable and removable. |
| `removed` | list of default ids | Defaults the user removed (D9). |

- The effective list is `defaults − removed + added`.
- An id in `removed` that is not a default (for example a default dropped by a later app
  version) is ignored.
- Both keys are optional. An empty or missing file means no changes to the defaults.

**Entry fields per list.** Every `added` entry has `id` (UUID) and `name` (non-empty
string, the user's own label). Some lists have more fields:

| File | Extra fields |
|---|---|
| `species.yaml` | `latinName` (string, optional) |
| `styles.yaml` | `picture` (optional, path relative to `settings/`, e.g. `attachments/<file>`). No picture means the placeholder is shown (D12). |
| `actions.yaml` | none. User actions are untyped and carry only a note (see [Sessions](#session-note-sessionsyyyy-mm-dd-hhmmmd)). |
| `soils.yaml` | none |
| `substrates.yaml` | `components` (required, see [Substrate](#substrate)). Defaults can also be removed like any list. |
| `fertilizers.yaml` | none |
| `treatments.yaml` | none |
| `pots.yaml` | `boughtOn` (date, required; the app fills in the creation date), `dimensions` (optional, `length` / `width` / `depth` in cm) |

The typed actions `measuring`, `repotting`, `fertilizing` and `treatment` are defaults
that can never be removed (#84). Listing one in `actions.yaml#removed` makes the vault
invalid.

### Bonsai folder: `bonsais/<bonsai-slug>/`

#### Folder name (slug)
The folder name comes from the bonsai `name`:
1. Decompose Unicode (NFD) and drop combining marks (`é` → `e`), then map the
   remaining letters that do not decompose (`æ` → `ae`, `œ` → `oe`, `ß` → `ss`,
   `ø` → `o`).
2. Lowercase.
3. Replace every run of characters outside `[a-z0-9]` with a single `-`.
4. Trim `-` at both ends and cut to 60 characters (then trim `-` again).
5. If the result is empty, use `bonsai`.
6. If the slug is taken by another bonsai folder, or is a reserved Windows name (`con`,
   `prn`, `aux`, `nul`, `com1`–`com9`, `lpt1`–`lpt9`
   ([Microsoft — Naming files](https://learn.microsoft.com/en-us/windows/win32/fileio/naming-a-file#naming-conventions))),
   append `-2`, then `-3`… until it is free.

Examples: `Érable du Jardin` → `erable-du-jardin`; `Juniperus #1` → `juniperus-1`;
`盆栽` → `bonsai`.

The folder name is **not** an identity: the app finds a bonsai by the `id` in its
`bonsai.md`, and does not check that the folder name matches the slug. A folder renamed
by hand stays valid and the app keeps it until the bonsai is renamed in the app.

**Renaming** a bonsai in the app computes the new slug (skipping its own current folder
in the collision check). If the slug changes, the folder is renamed (D23). Nothing
else changes: references use ids and embeds are relative to the bonsai folder.

#### `bonsai.md`

```markdown
---
id: 3f6c1e0a-9b7d-4e2a-8c51-0d4e7a2b9f10
name: Érable du jardin
species: acer-palmatum
style: moyogi
status: active
addedOn: 2024-03-02
age:
  birthday: 2016-04
substrate:
  mix: 6a3f9e1d-2c4b-4d8a-b7e5-0f1a2b3c4d5e
pot: 5b2d9c77-1a4e-4f0b-b3d8-6e9f0a1c2d34
cover: attachments/20260412-103512-a1b2c3.jpg
---

Bought at the Saint-Jean nursery…

![[attachments/20240302-141005-9f8e7d.jpg]]
```

| Key | Type | Required | Notes |
|---|---|---|---|
| `id` | UUID | yes | |
| `name` | string, non-empty | yes | Display name. The folder slug is derived from it. |
| `species` | species id | no | |
| `style` | style id | no | |
| `status` | `active` \| `archived` | yes | |
| `archived` | object | if `status: archived` | `reason` (`dead` \| `sold` \| `gifted` \| `lost` \| `other`, required), `date` (date, required), `note` (string, optional). Not allowed when `status: active`. |
| `addedOn` | date | yes | Date the bonsai was added (D16 "date added"). |
| `age` | object | no | Exactly one of `birthday` (date `YYYY-MM-DD` or partial `YYYY-MM`, the 1st of the month is assumed) or `years` (integer ≥ 0, frozen, does not grow). D13. |
| `substrate` | [Substrate](#substrate) | no | The **current** substrate. A repotting action updates it. |
| `pot` | pot id | no | The **current** pot. A repotting action updates it. |
| `cover` | path | no | Relative to the bonsai folder, must be under `attachments/`. |

The body is the free description (Markdown) with picture embeds.

The current substrate and pot are kept in `bonsai.md`, so the profile can be read
without replaying sessions. The sessions are the history. The latest size is not stored
here: it is computed from the measuring actions.

#### Session note: `sessions/YYYY-MM-DD-HHmm.md`

```markdown
---
id: 0c7a4f3e-5d21-4b8a-9e6f-2a1b3c4d5e6f
date: 2026-04-12
createdAt: 2026-04-12T10:35:12+02:00
actions:
  - action: repotting
    substrate:
      mix: 6a3f9e1d-2c4b-4d8a-b7e5-0f1a2b3c4d5e
    pot: 5b2d9c77-1a4e-4f0b-b3d8-6e9f0a1c2d34
  - action: measuring
    height: 42.5
    width: 38
    nebari: 21
    weight: 1850
  - action: pruning
    note: Structural pruning of the apex
---

Roots were circling, cut back about a third.

![[attachments/20260412-103512-a1b2c3.jpg]]
```

| Key | Type | Required | Notes |
|---|---|---|---|
| `id` | UUID | yes | |
| `date` | date | yes | The day the work was done. The user picks it and it can be in the past. |
| `createdAt` | timestamp | yes | When the note was created. Used for the file name and as a tie-breaker when sorting. |
| `actions` | list, at least 1 | yes | See below. |

The session belongs to the bonsai whose folder contains it, so there is no bonsai id in
the note.

**Actions.** Each item has `action` (an action id) and an optional `note` (string).
Typed actions (D6) add a payload:

| `action` | Payload | Rules |
|---|---|---|
| `measuring` | `height`, `width`, `nebari` (cm), `weight` (g) | All numbers > 0 and optional, but at least one is required. `nebari` is the circumference. |
| `repotting` | `substrate` ([Substrate](#substrate)), `pot` (pot id) | Both optional, at least one is required. When this session is the latest repotting by `date`, the app copies them to `bonsai.md`. |
| `fertilizing` | `fertilizer` (fertilizer id, required), `dose` (optional: `amount` number > 0, `unit` in `g` \| `ml` \| `g/l` \| `ml/l`) | |
| `treatment` | `treatment` (treatment id, required), `target` (string, optional, e.g. "aphids") | |
| any other action id | none | Only `note`. |

The same action may appear more than once in a session (two treatments, for example).
Payload keys that do not belong to the action make the note invalid.

**File name.** `createdAt` in local time, to the minute: `2026-04-12T10:35:12+02:00` →
`2026-04-12-1035.md`. If that file exists, `-2`, `-3`… is appended
(`2026-04-12-1035-2.md`). The app finds a session by its `id`, and the file name is
never parsed back. Editing `date` does not rename the file.

**Order.** The timeline sorts by `date` (newest first), then by `createdAt`.

#### Substrate
A substrate value (in `bonsai.md`, in a repotting action) is exactly one of:

```yaml
substrate:
  mix: 6a3f9e1d-2c4b-4d8a-b7e5-0f1a2b3c4d5e   # a named mix (substrates list)
```

```yaml
substrate:
  components:                      # an inline mix (D11)
    - soil: akadama
      percent: 60
    - soil: pumice
      percent: 40
```

`components` rules (inline mixes and `substrates.yaml` entries alike):
- At least 1 component. Each `soil` is a soil id and appears once.
- `percent` is an integer from 1 to 100, and the percents add up to exactly 100.

When a named mix in use is edited and the user picks "keep history" (D11), every
existing `mix: <id>` reference is rewritten as the `components` of the old composition.

#### Attachments and picture embeds
- Pictures live in the bonsai's own `attachments/` folder (D2). Pictures of user-added
  styles live in `settings/attachments/`.
- **File name:** `YYYYMMDD-HHmmss-<6 lowercase hex>.<ext>`, from the import time in
  local time plus 6 random hex characters, for example `20260412-103512-a1b2c3.jpg`.
  The extension is lowercase: `jpg`, `jpeg`, `png` or `webp` (the resized mode always
  writes `jpg`). Names are unique across the whole vault on purpose: Obsidian resolves
  a wikilink by file name, and when two files share a name it can pick the wrong one
  ([Obsidian Forum — Relative path: link points to a wrong file with same file name](https://forum.obsidian.md/t/relative-path-link-points-to-a-wrong-file-with-same-file-name/41786)).
- **Embed syntax** in any Markdown body, `bonsai.md` and session notes alike:
  `![[attachments/<file>]]` ([Obsidian Help — Embed files](https://obsidian.md/help/embeds)).
  The path is always **relative to the bonsai folder**, even in `sessions/*.md`. The app
  resolves it that way, and Obsidian finds the file because the name is unique.
  An optional display size is allowed: `![[attachments/<file>|300]]`.
- The same picture may be embedded in several notes and used as the `cover`.
- Only wikilink embeds are treated as pictures by the app. Other Markdown (for example
  `![](…)`) stays as text. Rendering is covered by #99.

#### Units
| Quantity | Stored unit |
|---|---|
| Height, width, nebari, pot dimensions | cm |
| Weight | g |
| Fertilizer dose | `g`, `ml`, `g/l` or `ml/l` |

### Validation (what makes a vault invalid)
Strict validation is implemented in #79. A vault is **invalid** if any of these holds:
- `metadata.yaml` is missing, is not valid YAML, or breaks its schema.
- A known file (`settings/*.yaml` from the list above, `bonsais/*/bonsai.md`,
  `bonsais/*/sessions/*.md`) is not valid YAML/front-matter, is missing a required key,
  has an unknown key, a wrong type or a value out of range.
- A folder under `bonsais/` has no `bonsai.md`.
- Two entities share an `id`, or an `added` entry reuses a default id.
- A reference (species, style, action, soil, mix, fertilizer, treatment, pot) points to
  an id that is not in the effective list.
- `cover`, a style `picture` or an embed points to a file that does not exist, or to a
  path outside the allowed folder (for example a `..` segment).
- A typed action is listed in `actions.yaml#removed`.

Pictures that exist but are not referenced anywhere are valid (the gallery shows them).

### Platform differences
The format is the same on every platform. The platforms differ only in how they
write it:

| Concern | Android (SAF) | Web (File System Access, Chromium) |
|---|---|---|
| Rename a bonsai folder | `DocumentsContract.renameDocument` ([Android API](https://developer.android.com/reference/android/provider/DocumentsContract#renameDocument(android.content.ContentResolver,%20android.net.Uri,%20java.lang.String))) | `move()` is not available for directory handles yet ([Chromium issue 40198034](https://issues.chromium.org/issues/40198034)), so the app copies the folder to the new name, then deletes the old one once the copy is complete. |
| Atomic write | Write `.<name>.tmp`, then `renameDocument`. | Write `.<name>.tmp`, then `FileSystemFileHandle.move()` ([Chrome for Developers — File System Access](https://developer.chrome.com/docs/capabilities/web-apis/file-system-access)). If that fails, write in place through `createWritable()`, which only replaces the file on `close()`. |

The storage issues (#75, #77) confirm these choices.

## Sample vault
[`spec-kit/samples/vault/`](../samples/vault/) shows every part of this format:
- two bonsais, one active (`erable-du-jardin`, with 3 sessions, a cover and embeds in
  both the description and a session) and one archived (`genevrier-de-chine`);
- a named mix and an inline mix, all four typed actions, and a user-defined action;
- overlays that add entries and remove a default;
- a same-minute session (`-2` suffix).

Default ids used in the sample (`acer-palmatum`, `moyogi`, `akadama`…) are examples until
the default catalogues are settled (#81).

## Acceptance Criteria
- [x] Spec `spec-kit/specs/vault-format.md` with a sample vault
- [ ] The sample vault opens in Obsidian with readable properties and embedded photos
  (manual check)
- [ ] Renaming a bonsai renames its folder, and its embeds still resolve (manual check:
  rename `bonsais/erable-du-jardin/` in Obsidian, then check that the embeds in
  `bonsai.md` and the sessions still render. The app side is tested in #75 / #77.)

## Open Questions
- HEIC photos from Android cameras: accept as-is (Chromium cannot display them) or always
  convert to JPEG? To settle in the #96 spike.
