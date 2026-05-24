# Spec: Bonsai List Feature

## Status
Accepted

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
    val id: String,           // UUID string, generated at creation time
    val name: String,
    val kind: String,         // free text, e.g. "Maple", "Pine"
    val purchaseDate: LocalDate,
    val lastMaintenanceDate: LocalDate? = null,
)
```
Dates use `kotlinx.datetime.LocalDate` (multiplatform, no platform-specific imports needed).

### Repository Interface
```kotlin
interface BonsaiRepository {
    fun getBonsaisStream(): Flow<List<Bonsai>>
    suspend fun addBonsai(bonsai: Bonsai)
}
```
The interface is async-ready: `Flow` for reads (naturally composable with `collectAsState`),
`suspend` for writes. Swapping to Room, SQLite, or a network backend only requires a new
`BonsaiRepository` implementation — no changes to the UI layer.

`InMemoryBonsaiRepository` uses `MutableStateFlow<List<Bonsai>>` internally so the list
screen updates reactively when a bonsai is added.

### Navigation
No third-party library. A sealed `Screen` class in `App.kt` combined with
`var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }` drives which
composable is rendered. Callbacks flow downward as lambdas.

```
Screen.Home     — landing page (click anywhere → BonsaiList)
Screen.BonsaiList — list of all bonsais (+ button top-left → AddBonsai)
Screen.AddBonsai  — form (Add button → back to BonsaiList)
```

### BonsaiListScreen
- `Scaffold` with `TopAppBar` (title "My Bonsais", "+" as navigation icon)
- `LazyColumn` with one `BonsaiCard` per bonsai
- Empty state: "No bonsais yet" centred text when the list is empty
- Reactive: collects `repository.getBonsaisStream()` as state — updates automatically after adding

### BonsaiCard
Displays: name (titleMedium), kind (bodyMedium), purchase date (bodySmall),
last maintenance date (bodySmall, only shown when non-null).

### AddBonsaiScreen
- `OutlinedTextField` for Name, Kind, Purchase Date (YYYY-MM-DD), Last Maintenance Date (YYYY-MM-DD, optional)
- Validation on "Add" button press: name and kind must be non-blank; purchase date must parse
  as a valid `LocalDate`; maintenance date is optional but must parse if non-blank
- Errors displayed as supporting text below each invalid field
- On success: bonsai created (UUID generated with `kotlin.uuid.Uuid.random()`), added to
  repository, `onBonsaiAdded()` called

### Platform differences
None in this feature. All code lives in `commonMain`. `kotlinx-datetime` and
`kotlin.uuid.Uuid` are multiplatform APIs available on both Android and wasmJs targets.

## Acceptance Criteria
- [ ] `Bonsai` data class compiles and is unit-tested on all platforms
- [ ] `InMemoryBonsaiRepository` passes unit tests (empty stream, add, accumulate)
- [ ] Clicking anywhere on HomeScreen navigates to BonsaiListScreen
- [ ] BonsaiListScreen shows "No bonsais yet" when empty
- [ ] BonsaiListScreen shows bonsai name, kind, and dates in cards
- [ ] "+" button in BonsaiListScreen navigates to AddBonsaiScreen
- [ ] AddBonsaiScreen validates all required fields and shows inline errors
- [ ] Submitting a valid form adds the bonsai and returns to BonsaiListScreen
- [ ] The new bonsai appears in the list after returning
- [ ] `./gradlew :composeApp:koverVerify` passes at 100% line coverage

## Open Questions
- None at this time.
