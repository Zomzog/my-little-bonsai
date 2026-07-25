# Update: Fix Blank Page on GitHub Pages (wasmJs publicPath)

## Date
2026-05-23

## Affected Spec
[home-page](../specs/home-page.md)

## Reason
The deployed site at https://zomzog.fr/my-little-bonsai/ showed a blank page.
Webpack defaulted to `publicPath: "/"` (absolute root), so the browser
requested `composeApp.wasm` from the site root instead of from the
`/my-little-bonsai/` subdirectory — resulting in a 404 for the WASM binary
and a silent initialization failure.

## Change Description
- Added `composeApp/webpack.config.d/output.js` to override webpack's
  `output.publicPath` to `"./"` (relative). This ensures all build artifacts
  (`.wasm`, fonts, images) are resolved relative to the page URL, regardless
  of the subdirectory the site is hosted at.
- Added `actions/configure-pages@v5` step to `deploy-web.yml`. This properly
  configures the GitHub Pages environment before the artifact upload, which is
  required for the artifact-based deployment pipeline to function correctly.

## Migration / Impact
No breaking changes. The fix is transparent to users; the app will now render
correctly on GitHub Pages subdirectory deployments.
