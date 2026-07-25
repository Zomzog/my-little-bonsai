# Update: Replace Custom BonsaiDatePickerDialog with Official Material3 DatePickerDialog

## Date
2026-05-24

## Affected Spec
[bonsai-list-feature](../specs/bonsai-list-feature.md)

## Reason
The custom `BonsaiDatePickerDialog` used an `expect`/`actual` pattern with a poor fallback
implementation on Web/JVM (three manual text fields for year/month/day). The Material3
`DatePickerDialog` is available in Compose Multiplatform for all targets, making the
platform-split unnecessary.

## Change Description
- Removed the `expect` declaration and all three `actual` implementations (Android, JVM, WasmJS).
- Replaced with a single `@Composable fun BonsaiDatePickerDialog` in `commonMain` using
  `androidx.compose.material3.DatePickerDialog` and `DatePicker` directly — identical to what
  the Android actual was already doing.
- `AddBonsaiScreen` call-sites are unchanged.

## Migration / Impact
No breaking changes for users. All platforms now show the same official Material3 date picker
calendar UI instead of the JVM/Web text-field workaround.
