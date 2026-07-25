import { test, expect, Page, Locator } from '@playwright/test';

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
 * Click at the visual centre of a Compose accessibility element.
 *
 * Compose Wasm renders to a <canvas> that intercepts all pointer events.
 * Accessibility companion DOM nodes are reachable by Playwright selectors but
 * cannot be clicked directly — the canvas always grabs the event first.
 * We obtain the element's bounding box and dispatch a raw mouse click at
 * those page coordinates so the canvas (and thereby Compose) receives the
 * event at the correct visual position.
 */
async function clickComposeElement(page: Page, locator: Locator): Promise<void> {
  const box = await locator.boundingBox();
  if (!box) throw new Error('Compose element not found or has no bounding box');
  await page.mouse.click(box.x + box.width / 2, box.y + box.height / 2);
}

/**
 * Waits for the Wasm runtime to boot and Compose to render the HomeScreen.
 */
async function waitForAppReady(page: Page): Promise<void> {
  await page.waitForSelector('canvas', { timeout: 20_000 });
  // Compose exposes accessibility DOM nodes — wait for the HomeScreen title
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
    // HomeScreen is a full-screen clickable column — click the canvas directly
    await page.locator('canvas').click();

    await expect(
      page.getByRole('button', { name: 'Choose Folder' }),
    ).toBeVisible({ timeout: 10_000 });
  });

  test('clicking Choose Folder opens the picker and navigates to Bonsai List', async ({
    page,
  }) => {
    // ① Navigate past the HomeScreen
    await page.locator('canvas').click();

    // ② FolderSetupScreen must appear with the Choose Folder button
    const chooseFolder = page.getByRole('button', { name: 'Choose Folder' });
    await expect(chooseFolder).toBeVisible({ timeout: 10_000 });

    // ③ Click via canvas coordinates — canvas intercepts all pointer events
    await clickComposeElement(page, chooseFolder);

    // ④ Mocked picker resolves immediately → app navigates to BonsaiList
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
        () =>
          Promise.reject(new DOMException('Aborted', 'AbortError'));
    });

    await page.locator('canvas').click();

    const chooseFolder = page.getByRole('button', { name: 'Choose Folder' });
    await expect(chooseFolder).toBeVisible({ timeout: 10_000 });

    await clickComposeElement(page, chooseFolder);

    // After cancellation the button must still be present — no navigation
    await expect(chooseFolder).toBeVisible({ timeout: 5_000 });
    await expect(page.getByText('My Bonsais')).not.toBeVisible();
  });
});
