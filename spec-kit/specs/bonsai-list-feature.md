# Spec: Bonsai List Feature

## Status
Implemented

## Goal
Introduce the core bonsai management flow: a domain model for tracking individual bonsai trees,
an in-memory repository abstraction, and three screens (Home → BonsaiList → AddBonsai) connected
by a lightweight navigation system.

## Scope
- In scope:
  - `Bonsai` data model (id, name, kind, purchaseDate, lastMaintenanceDate)
  - `BonsaiRepository` interface with in-memory implementation
  - `BonsaiListScreen` — scrollable card list with empty state
  - `AddBonsaiScreen` — form to create a bonsai with field validation
  - Lightweight navigation via sealed `Screen` class + `mutableStateOf` in `App.kt`
- Out of scope:
  - Persistent storage (database, cloud) — covered by future specs
  - Editing or deleting bonsais
  - Search or filter
  - Image upload / bonsai photos
  - Back-stack / back navigation (screens are one-way for now)

## Design

### Domain Model
```kotlin
data class Bonsai(
    val id: String,
    val name: String,
    val kind: String,
    val purchaseDate: LocalDate,
    val lastMaintenanceDate: LocalDate? = null,
)
```
Uses `kotlinx.datetime.LocalDate` (multiplatform).

### Repository
```kotlin
interface BonsaiRepository {
    fun getBonsaisStream(): Flow<List<Bonsai>>
    suspend fun addBonsai(bonsai: Bonsai)
}
```
`InMemoryBonsaiRepository` uses `MutableStateFlow<List<Bonsai>>` for reactive updates.

### Navigation
Sealed `Screen` class in `App.kt` with `mutableStateOf`. No third-party library.

```
Home → BonsaiList (+ button) → AddBonsai (form submit) → BonsaiList
```

### Screens
- **BonsaiListScreen**: `Scaffold` + `TopAppBar` + `LazyColumn` of `BonsaiCard`s; "No bonsais yet" empty state.
- **BonsaiCard**: name (titleMedium), kind (bodyMedium), dates (bodySmall).
- **AddBonsaiScreen**: fields for Name, Kind, Purchase Date, Last Maintenance Date (optional); validates on submit; shows inline errors.

### Platform differences
None. All code in `commonMain`.

## Acceptance Criteria
- [x] `Bonsai` data class compiles and is unit-tested on all platforms
- [x] `InMemoryBonsaiRepository` passes unit tests (empty stream, add, accumulate)
- [x] Clicking anywhere on HomeScreen navigates to BonsaiListScreen
- [x] BonsaiListScreen shows "No bonsais yet" when empty
- [x] BonsaiListScreen shows bonsai name, kind, and dates in cards
- [x] "+" button in BonsaiListScreen navigates to AddBonsaiScreen
- [x] AddBonsaiScreen validates all required fields and shows inline errors
- [x] Submitting a valid form adds the bonsai and returns to BonsaiListScreen
- [x] The new bonsai appears in the list after returning
- [x] `./gradlew :composeApp:koverVerify` passes at 100% line coverage
