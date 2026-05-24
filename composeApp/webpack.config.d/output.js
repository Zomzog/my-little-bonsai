// Use a relative publicPath so assets (composeApp.wasm, etc.) are resolved
// relative to the page URL. Without this, webpack defaults to "/" which breaks
// GitHub Pages subdirectory deployments (e.g. /my-little-bonsai/).
// Restricted to production mode: the Karma test runner uses development mode
// and manages its own output paths via webpack-dev-server middleware.
if (config.mode === "production") {
    config.output = config.output || {};
    config.output.publicPath = "./";
}
