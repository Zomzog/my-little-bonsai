# Update: HomeScreen Click Navigation

## Date
2026-05-20

## Affected Spec
[home-page](../specs/home-page.md)

## Reason
The bonsai list feature requires users to navigate from the landing page to the bonsai list.
The HomeScreen becomes the entry point into the app rather than the final destination.

## Change Description
- `HomeScreen` gains an `onNavigate: () -> Unit = {}` parameter
- The root `Column` is wrapped with `.clickable { onNavigate() }` so clicking anywhere on
  the screen triggers navigation
- `App.kt` passes `onNavigate = { currentScreen = Screen.BonsaiList }` when rendering
  `HomeScreen`
- Existing tests continue to work unchanged because the parameter defaults to a no-op lambda

## Migration / Impact
No breaking changes. Callers that omit `onNavigate` (including all existing tests) get the
original no-op behaviour. The visual appearance of HomeScreen is unchanged.
