# Update: Replace Date Text Inputs with Date Picker Dialog

## Date
2026-05-23

## Affected Spec
[bonsai-list-feature](../specs/bonsai-list-feature.md)

## Reason
Manual text entry for dates (YYYY-MM-DD) is error-prone and poor UX. A dialog-based
date picker eliminates format errors and provides a clearer interaction model on both
Android and Wasm/JS without any platform-specific code.

## Change Description

### Form State
- `AddBonsaiFormState.purchaseDateText: String` replaced by `purchaseDate: LocalDate?`
- `AddBonsaiFormState.lastMaintenanceDateText: String` replaced by `lastMaintenanceDate: LocalDate?`
- `AddBonsaiFormState.lastMaintenanceDateError: String?` removed — the picker guarantees a
  valid date on every confirmation; maintenance remains optional by leaving it unpicked

### Constants
- `LABEL_PURCHASE_DATE` updated: `"Purchase date (YYYY-MM-DD)"` → `"Purchase date"`
- `LABEL_LAST_MAINTENANCE` updated: `"Last maintenance (YYYY-MM-DD, optional)"` → `"Last maintenance (optional)"`
- `ERROR_DATE_FORMAT` removed — text parsing no longer exists
- `ERROR_PURCHASE_DATE_REQUIRED` added — shown when Add is clicked with no purchase date selected
- `ERROR_INVALID_DATE` added — shown inside the picker dialog when year/month/day do not form a valid date

### UI
- Both `OutlinedTextField` date widgets replaced by `OutlinedButton` components
- Button text shows the label when no date is selected; shows the ISO date string when one is picked
- Clicking a button opens `BonsaiDatePickerDialog` — a custom `AlertDialog` with three
  `OutlinedTextField` inputs (Year, Month, Day) that constructs `LocalDate(year, month, day)` directly
- No epoch milliseconds, no timezone conversions — the selected date is always exactly what the user typed
- Confirming with an invalid combination shows an inline error; typing any field clears it
- Cancelling leaves the form state unchanged

### validate()
- No longer parses text strings; reads `LocalDate?` directly from state
- `purchaseDateError` is set to `ERROR_PURCHASE_DATE_REQUIRED` when `purchaseDate == null`
- `lastMaintenanceDateError` logic removed entirely

## Migration / Impact
- No changes to the `Bonsai` domain model or `BonsaiRepository`
- `ValidateTest`: removed 3 tests (text-parsing tests), renamed 1, updated 5
- `AddBonsaiScreenTest`: removed 2 tests (invalid text input tests), added 4 new tests
  covering the picker dialog flows (invalid date, clear error, cancel, purchase date error clear)
- `AppNavigationTest`: updated 2 tests to use click + Year/Month/Day + OK instead of `performTextInput`
