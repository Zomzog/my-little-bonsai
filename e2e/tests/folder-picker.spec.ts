import { test, expect, Page } from '@playwright/test';

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

/**
 * Installs a mock for `window.showDirectoryPicker` before the page scripts
 * run. The mock resolves immediately with a fake FileSystemDirectoryHandle
 * that satisfies the write path used by `WebFolderStorageManager`.
 */
async function mockDirectoryPicker(page: Page): Promise<void> {
  await page.addInitScript(() => {
    (window as Window & { showDirectoryPicker?: () => Promise<unknown> }).showDirectoryPicker =
      () => {
        const writableStream = {
          write: (_content: unknown) => Promise.resolve(),
          close: () => Promise.resolve(),
        };
        const fileHandle = {
          createWritable: () => Promise.resolve(writableStream),
        };
        const dirHandle = {
          getFileHandle: (_name: string, _opts: unknown) =>
            Promise.resolve(fileHandle),
        };
        return Promise.resolve(dirHandle);
      };
  });
}

/**
 * Waits for the Compose/Wasm runtime to boot and for Compose to render its
 * first frame. We wait for the canvas element to appear and then for a piece
 * of text that is always present on the HomeScreen.
 */
async function waitForAppReady(page: Page): Promise<void> {
  await page.waitForSelector('canvas', { timeout: 20_000 });
  await expect(page.getByText('My Little Bonsai')).toBeVisible({
    timeout: 20_000,
  });
}

// ---------------------------------------------------------------------------
// Tests
// ---------------------------------------------------------------------------

test.describe('Folder picker flow', () => {
  test.beforeEach(async ({ page }) => {
    await mockDirectoryPicker(page);
    await page.goto('/');
    await waitForAppReady(page);
  });

  test('Choose Folder button is visible on FolderSetupScreen', async ({
    page,
  }) => {
    // Navigate past the HomeScreen (the whole screen is clickable)
    await page.getByText('My Little Bonsai').click();

    await expect(
      page.getByRole('button', { name: 'Choose Folder' }),
    ).toBeVisible({ timeout: 10_000 });
  });

  test('clicking Choose Folder opens the picker and navigates to Bonsai List', async ({
    page,
  }) => {
    // ① HomeScreen → click anywhere to proceed
    await page.getByText('My Little Bonsai').click();

    // ② FolderSetupScreen must appear with the Choose Folder button
    const chooseFolder = page.getByRole('button', { name: 'Choose Folder' });
    await expect(chooseFolder).toBeVisible({ timeout: 10_000 });

    // ③ Click the button; the mocked picker resolves synchronously
    await chooseFolder.click();

    // ④ App should navigate to BonsaiList
    await expect(page.getByText('My Bonsais')).toBeVisible({
      timeout: 10_000,
    });
  });

  test('cancelling the picker keeps the user on FolderSetupScreen', async ({
    page,
  }) => {
    // Override the mock to simulate the user cancelling the picker
    await page.evaluate(() => {
      (window as Window & { showDirectoryPicker?: () => Promise<unknown> }).showDirectoryPicker =
        () => Promise.reject(new DOMException('Aborted', 'AbortError'));
    });

    await page.getByText('My Little Bonsai').click();

    const chooseFolder = page.getByRole('button', { name: 'Choose Folder' });
    await expect(chooseFolder).toBeVisible({ timeout: 10_000 });

    await chooseFolder.click();

    // After cancellation the button must still be there — no navigation
    await expect(chooseFolder).toBeVisible({ timeout: 5_000 });
    await expect(page.getByText('My Bonsais')).not.toBeVisible();
  });
});
