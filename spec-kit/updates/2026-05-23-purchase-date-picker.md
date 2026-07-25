# Update: Replace Date Text Inputs with Material 3 DatePickerDialog

## Date
2026-05-23

## Affected Spec
[bonsai-list-feature](../specs/bonsai-list-feature.md)

## Reason
Manual text entry for dates (YYYY-MM-DD) is error-prone and poor UX. Using Material 3
`DatePickerDialog` eliminates format errors entirely and provides a native calendar UI
that works on both Android and Wasm/JS without any platform-specific code.

## Change Description

### Form State
- `AddBonsaiFormState.purchaseDateText: String` replaced by `purchaseDate: LocalDate?`
- `AddBonsaiFormState.lastMaintenanceDateText: String` replaced by `lastMaintenanceDate: LocalDate?`
- `AddBonsaiFormState.lastMaintenanceDateError: String?` removed — picker prevents invalid input

### Constants
- `LABEL_PURCHASE_DATE` updated: `"Purchase date (YYYY-MM-DD)"` → `"Purchase date"`
- `LABEL_LAST_MAINTENANCE` updated: `"Last maintenance (YYYY-MM-DD, optional)"` → `"Last maintenance (optional)"`
- `ERROR_DATE_FORMAT` removed — text parsing no longer exists
- `ERROR_PURCHASE_DATE_REQUIRED` added — shown when Add is clicked with no purchase date selected

### UI
- Both `OutlinedTextField` date widgets replaced by `OutlinedButton` components
- Button text shows the label when no date is selected; shows the ISO date string when picked
- Clicking a button opens a Material 3 `DatePickerDialog` calendar pre-selected to today's local date
- Confirming the dialog sets the `LocalDate` on form state; cancelling leaves state unchanged

### Epoch conversion (internal implementation detail)
`DatePickerState.selectedDateMillis` is always UTC midnight, so `millis / 86_400_000`
is an exact integer number of days since the Unix epoch — no timezone conversion required:
- `LocalDate.toPickerMillis()` — pure integer arithmetic, no timezone dependency
- `Long.toLocalDate()` — pure integer arithmetic, no timezone dependency

### validate()
- No longer parses text strings; reads `LocalDate?` directly from state
- `purchaseDateError` set to `ERROR_PURCHASE_DATE_REQUIRED` when `purchaseDate == null`
- `lastMaintenanceDateError` logic removed entirely

## Migration / Impact
- No changes to the `Bonsai` domain model, `BonsaiRepository`, or display screens
- `ValidateTest`: removed 3 tests (text-parsing tests), renamed 1, updated 5
- `AddBonsaiScreenTest`: removed invalid-text-input tests; added cancel and error-clear tests
- `AppNavigationTest`: updated 2 tests to use click + OK instead of `performTextInput`
