# Spec: Default Species Catalogue

## Status
Implemented

## Goal
Ship a curated default list of ~40 common bonsai species so the species picker
(part of the managed reference lists engine, #80) has useful content out of
the box, without the user having to type every species by hand.

## Scope
- In scope: the default species data (Latin name, EN/FR common names, stable
  id) and an accent-insensitive alphabetical sort by the label in the current
  language.
- Out of scope: the managed-list add/remove/overlay mechanism itself (#80),
  the species picker UI (#87), and species categories (future work, #114).

## Design
- `Species(id, latinName, commonNameEn, commonNameFr)` in
  `domain/species/Species.kt`. `latinName` is nullable to also represent a
  user-added species with no Latin name (per #82's requirements); `id` is
  always present.
- `DefaultSpecies.all` in `domain/species/DefaultSpecies.kt` holds the 40
  curated entries. Each `id` is the Latin name lower-cased and kebab-cased
  (e.g. `Acer palmatum` → `acer-palmatum`), matching the vault format's id
  convention for default entries (stable kebab-case, never renamed).
- **No categories in v1.** The list has no grouping; consumers are expected to
  sort it with `List<Species>.sortedByLabel(language)` before display.
- `sortedByLabel` compares the label (`commonNameEn` or `commonNameFr`) after
  lower-casing and folding common Latin diacritics (à, é, ç, î, ô, ù, ÿ, ñ and
  their variants) to their base letter, so "Érable" sorts under "E" next to
  "Erable"-like entries rather than after "Z". The fold table is a fixed map
  rather than `java.text.Normalizer`, which isn't available on `wasmJs`.
- This module has no dependency on the vault storage layer or the managed
  lists engine: it is plain data plus a pure function, so it doesn't need to
  wait on the vault format (#74) or the lists engine (#80) to land.

## Acceptance Criteria
- [x] ~40 species curated with a Latin key and EN/FR common names
- [ ] List reviewed by the product owner
- [x] Shipped as in-code data (`DefaultSpecies.all`), ready to be exposed as
      app resources once the managed-list picker (#80) consumes it
- [x] Sort is alphabetical in EN and FR and accent-insensitive, with tests

## Open Questions
- Should the fold table grow to cover non-French/English accented species
  names as more locales are added, or should it be replaced by a proper
  Unicode normalization dependency once one is picked for wasmJs (tracked by
  the tech spike, #96)?
