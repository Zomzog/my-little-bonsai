import { test, expect, Page } from '@playwright/test';

const BONSAI_LIST_KEY = 'bonsai.bonsais';
const CREATION_DATE_KEY = 'bonsai.creationDate';

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

/**
 * Clicks past the HomeScreen. It is a full-screen clickable column, so clicking
 * the canvas anywhere navigates onwards.
 */
async function enterApp(page: Page): Promise<void> {
  await page.locator('canvas').click();
}

test.describe('Browser storage', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/');
    await waitForAppReady(page);
  });

  test('goes straight to the bonsai list without asking for a folder', async ({
    page,
  }) => {
    await enterApp(page);

    await expect(page.getByText('My Bonsais')).toBeVisible({ timeout: 10_000 });
    await expect(
      page.getByRole('button', { name: 'Choose Folder' }),
    ).toHaveCount(0);
  });

  test('records the vault creation date on the first visit', async ({
    page,
  }) => {
    await enterApp(page);
    await expect(page.getByText('My Bonsais')).toBeVisible({ timeout: 10_000 });

    const creationDate = await page.evaluate(
      (key) => localStorage.getItem(key),
      CREATION_DATE_KEY,
    );
    expect(creationDate).toMatch(/^\d{4}-\d{2}-\d{2}$/);
  });

  test('restores bonsais held in browser storage after a reload', async ({
    page,
  }) => {
    await page.evaluate(
      ({ key, value }) => localStorage.setItem(key, value),
      {
        key: BONSAI_LIST_KEY,
        value: JSON.stringify([
          {
            id: 'id-a',
            name: 'Akira',
            kind: 'Maple',
            purchaseDate: '2024-03-10',
            lastMaintenanceDate: null,
          },
        ]),
      },
    );

    await page.reload();
    await waitForAppReady(page);
    await enterApp(page);

    await expect(page.getByText('Akira')).toBeVisible({ timeout: 10_000 });
    await expect(page.getByText('Maple')).toBeVisible();
  });

  test('a reload does not send the user back to onboarding', async ({
    page,
  }) => {
    await enterApp(page);
    await expect(page.getByText('My Bonsais')).toBeVisible({ timeout: 10_000 });

    await page.reload();
    await waitForAppReady(page);
    await enterApp(page);

    await expect(page.getByText('My Bonsais')).toBeVisible({ timeout: 10_000 });
    await expect(
      page.getByRole('button', { name: 'Choose Folder' }),
    ).toHaveCount(0);
  });
});
