import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './tests',
  timeout: 30_000,
  reporter: [['html', { open: 'never' }]],
  use: {
    baseURL: 'http://localhost:8080',
    screenshot: 'only-on-failure',
    trace: 'on-first-retry',
  },
  // Start a static file server pointing at the compiled Wasm distribution.
  // Run `./gradlew :composeApp:wasmJsBrowserDistribution` before running tests.
  webServer: {
    command:
      'npx serve ../composeApp/build/dist/wasmJs/productionExecutable -p 8080',
    url: 'http://localhost:8080',
    reuseExistingServer: !process.env.CI,
    timeout: 60_000,
  },
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
  ],
});
