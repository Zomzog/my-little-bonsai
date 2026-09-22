import { test, expect, Page } from '@playwright/test';

const MOBILE_APP_URL = 'https://www.youtube.com/watch?v=dQw4w9WgXcQ';

/**
 * Support is feature-detected (`'showDirectoryPicker' in window`), not
 * user-agent sniffed. Deleting the API before the page loads reproduces what
 * Firefox/Safari see today without needing those engines installed here.
 */
async function makeFileSystemAccessUnsupported(page: Page) {
  await page.addInitScript(() => {
    // @ts-expect-error - simulating a browser without the File System Access API
    delete window.showDirectoryPicker;
  });
}

test.describe('Unsupported browser page', () => {
  test('shows the unsupported page when the File System Access API is missing', async ({
    page,
  }) => {
    await makeFileSystemAccessUnsupported(page);
    await page.goto('/');

    await expect(page.getByText("This browser isn't supported")).toBeVisible({
      timeout: 20_000,
    });
    await expect(page.getByText('Chrome, Edge, and other Chromium-based desktop browsers.')).toBeVisible();
    await expect(
      page.getByRole('button', { name: 'Get the mobile app instead' }),
    ).toBeVisible();
  });

  test('opens the mobile app link in a new tab', async ({ page, context }) => {
    await makeFileSystemAccessUnsupported(page);
    await page.goto('/');
    const mobileAppButton = page.getByRole('button', {
      name: 'Get the mobile app instead',
    });
    await expect(mobileAppButton).toBeVisible({ timeout: 20_000 });

    const [popup] = await Promise.all([
      context.waitForEvent('page'),
      mobileAppButton.click(),
    ]);
    await popup.waitForLoadState('domcontentloaded');
    expect(popup.url()).toBe(MOBILE_APP_URL);
  });

  test('shows the app instead of the unsupported page when the API is available', async ({
    page,
  }) => {
    await page.goto('/');

    await expect(page.getByText('My Little Bonsai')).toBeVisible({
      timeout: 20_000,
    });
    await expect(page.getByText("This browser isn't supported")).toHaveCount(0);
  });
});
