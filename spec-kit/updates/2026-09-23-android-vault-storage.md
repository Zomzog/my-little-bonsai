# Update: Android Vault Storage (Bonsai Profile + Sessions)

## Date
2026-09-23

## Affected Spec
[local-folder-storage](../specs/local-folder-storage.md), implementing the
[vault-format](../specs/vault-format.md) (#74) on Android for issue #75.

## Reason
`local-folder-storage.md` only covered onboarding and the initial `metadata.yaml`
write; Android still kept bonsais in memory (`InMemoryBonsaiRepository`), as flagged
in the "Open Questions" of
[2026-07-25-web-browser-storage.md](2026-07-25-web-browser-storage.md). Issue #75 asks
for the vault to actually back bonsai and session data on Android.

The full scope of #75 (bonsais, sessions, all eight settings lists, and attachments)
depends on pieces of the epic that are not built yet: the settings-lists
defaults-and-overlay engine (#80) and most default catalogues (#81/#83/#84) beyond
species (#82), and the picture-import pipeline. Loading the full `Bonsai` profile and
`Session`/`Action` model with a from-scratch domain rewrite, a YAML/front-matter
codec, and a settings-lists engine all at once was not realistic as one change, so
this update narrows #75 to the **bonsai profile and session history** — the two
entities with no other epic-issue dependency — and defers settings-list read/write
and attachments (picture embeds) to follow-up issues.

## Change Description

- **Domain model** (`commonMain/domain/`): `Bonsai` now carries the full `bonsai.md`
  profile shape (`species`, `style`, `status`/`archived`, `age`, `substrate`, `pot`,
  `cover`, free-text `description`) instead of the placeholder `kind`/`purchaseDate`/
  `lastMaintenanceDate` fields from `bonsai-list-feature.md`. New `Session`, `Action`
  (a sealed type per vault-format.md's typed actions: `Measuring`, `Repotting`,
  `Fertilizing`, `Treatment`, `Other`), `Substrate`, `BonsaiAge`, `ArchivedInfo` and
  `VaultTimestamp` (an offset-aware local date-time, since `kotlinx-datetime` has no
  built-in type for that) types were added. `BonsaiRepository` gained `getBonsai`/
  `updateBonsai`; a new `SessionRepository` interface was added.
- **Vault codec** (`commonMain/data/vault/`): a hand-rolled front-matter splitter and
  a small indentation-based YAML reader (`MiniYaml`) — not a general YAML library —
  scoped to exactly the shapes `bonsai.md`/session notes use (scalars, nested
  mappings, sequences of mappings). `BonsaiVaultCodec`/`SessionVaultCodec` read and
  write those two file types; `slugify` implements the folder-naming algorithm
  (Unicode fold via a per-platform `stripDiacritics`, ligature table, collision
  suffixing).
- **Storage abstraction**: `VaultFileSystem` (list/read/write-atomic by
  vault-relative path) is implemented by `SafVaultFileSystem` (Android, raw
  `ContentResolver`/`DocumentsContract` batched queries — not
  `androidx.documentfile`, whose per-child `DocumentFile` metadata queries are too
  slow for the "~200 bonsais / 5000 sessions" scale the issue calls out) and by
  `LocalVaultFileSystem` (JVM, `java.nio.file`; used to exercise the repository logic
  against a real filesystem in `jvmTest`, since there is no `androidTest` source set
  yet). `VaultBonsaiRepository`/`VaultSessionRepository` build an in-memory index on
  first use and implement the two repository interfaces over `VaultFileSystem`.
  `AndroidFolderStorageManager` gained `folderUri()` so the Android
  `BonsaiRepositoryProvider` can hand the persisted vault root to `SafVaultFileSystem`.
- **UI**: `AddBonsaiScreen` now collects `name` and `addedOn` (renamed from
  `purchaseDate`) only; the placeholder `Kind` and `Last maintenance` fields are
  gone, since neither has a vault-format equivalent. `BonsaiListScreen` shows
  `addedOn` instead. Every other profile field (species, style, substrate, pot,
  age, cover, archive) round-trips correctly through the vault codec but has no
  editing UI yet.

## Migration / Impact
- **Breaking domain change**: `Bonsai`'s shape changed; Web's `BonsaiSerialization`
  (`localStorage` JSON) was updated to the new fields. Per #75's requirement,
  existing in-memory Android data is dropped (pre-1.0); there is no migration for it.
- **Not in this change** (left for follow-up issues): settings-list read/write
  (species/styles/soils/substrates/fertilizers/treatments/pots — needs #80's
  defaults-and-overlay engine), attachments and picture embeds, folder rename when a
  bonsai's name changes (`updateBonsai` rewrites the bonsai's existing slug folder in
  place; it does not yet move the folder), any session-log UI (`SessionRepository`/
  `VaultSessionRepository` are implemented and tested but not wired into a screen),
  and `metadata.yaml` `schemaVersion` validation/migration.
- `SafVaultFileSystem` and the Android provider wiring are excluded from the JVM
  Kover run (the existing `excludedSourceSets.add("androidMain")` already covers
  them, matching `AndroidFolderStorageManager`'s precedent) since they need
  on-device SAF behaviour that JVM tests cannot reach; `VaultBonsaiRepository`/
  `VaultSessionRepository`/the codec/`slugify` are fully covered via `commonTest`/
  `jvmTest` against `LocalVaultFileSystem` instead.
