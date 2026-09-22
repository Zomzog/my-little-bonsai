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

    // The accessibility node isn't the real click target: Compose for Web
    // renders and handles pointer input on the <canvas> on top of it, the
    // same reason other specs click the canvas rather than a locator
    // (see `enterApp` in browser-storage.spec.ts). Click the canvas at the
    // button's on-screen position instead of the (intercepted) locator.
    const box = await mobileAppButton.boundingBox();
    if (!box) throw new Error('mobile app button has no bounding box');

    const [popup] = await Promise.all([
      context.waitForEvent('page'),
      page.mouse.click(box.x + box.width / 2, box.y + box.height / 2),
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
