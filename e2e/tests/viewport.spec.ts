import { test, expect } from '@playwright/test';

/**
 * The Compose canvas is sized from the `#composeTarget` container and cannot
 * reflow. If the container or the document can grow past the viewport, mobile
 * browsers rescale the page and the app renders clipped and off-centre.
 */
test.describe('Viewport fit', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/');
    await page.waitForSelector('canvas', { timeout: 20_000 });
    await expect(page.getByText('My Little Bonsai')).toBeVisible({
      timeout: 20_000,
    });
  });

  test('canvas fills the viewport exactly', async ({ page }) => {
    const viewport = page.viewportSize();
    if (!viewport) throw new Error('no viewport size');

    const box = await page.locator('canvas').boundingBox();
    if (!box) throw new Error('canvas has no bounding box');

    expect(box.x).toBe(0);
    expect(box.y).toBe(0);
    expect(Math.round(box.width)).toBe(viewport.width);
    expect(Math.round(box.height)).toBe(viewport.height);
  });

  test('document does not overflow the viewport', async ({ page }) => {
    const overflow = await page.evaluate(() => {
      const root = document.documentElement;
      return {
        scrollWidth: root.scrollWidth,
        scrollHeight: root.scrollHeight,
        innerWidth: window.innerWidth,
        innerHeight: window.innerHeight,
      };
    });

    expect(overflow.scrollWidth).toBeLessThanOrEqual(overflow.innerWidth);
    expect(overflow.scrollHeight).toBeLessThanOrEqual(overflow.innerHeight);
  });

  test('canvas is resized when the viewport changes', async ({ page }) => {
    await page.setViewportSize({ width: 320, height: 640 });

    await expect
      .poll(async () => {
        const box = await page.locator('canvas').boundingBox();
        return box ? [Math.round(box.width), Math.round(box.height)] : null;
      })
      .toEqual([320, 640]);
  });
});
