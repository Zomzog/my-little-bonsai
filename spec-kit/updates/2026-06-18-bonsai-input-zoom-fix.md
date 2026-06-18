# Update: Fix Mobile Input Zoom and Increase Field Font Size

## Date
2026-06-18

## Affected Spec
[bonsai-list-feature](../specs/bonsai-list-feature.md)

## Reason
On Android Chrome, tapping a text field in the "Add Bonsai" form caused the browser to zoom in and never zoom back out. Input fields also felt too small for comfortable mobile use.

## Change Description
- Added CSS `input, select, textarea { font-size: 16px !important; }` to `index.html` so the hidden Compose MP keyboard-capture input element never triggers browser auto-zoom (mobile browsers zoom in when an input has font-size < 16px).
- Increased `OutlinedTextField` input text size to 18sp on the Name and Kind fields.

## Migration / Impact
Visual-only change; no data model or API changes. Both Android and Web builds are affected by the font-size increase.
