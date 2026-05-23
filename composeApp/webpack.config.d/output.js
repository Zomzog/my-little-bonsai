// Use a relative publicPath so assets (composeApp.wasm, etc.) are resolved
// relative to the page URL. Without this, webpack defaults to "/" which breaks
// GitHub Pages subdirectory deployments (e.g. /my-little-bonsai/).
config.output = config.output || {};
config.output.publicPath = "./";
