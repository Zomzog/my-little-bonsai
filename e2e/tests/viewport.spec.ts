import { test, expect, Page } from '@playwright/test';

/**
 * The Compose canvas is sized from `<body>` and cannot reflow. If the container
 * or the document can grow past the viewport, mobile browsers rescale the page
 * and the app renders clipped and off-centre.
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

/**
 * Chrome ignores `width=device-width` in "Desktop site" mode and lays the page
 * out at its 980 CSS px fallback. The app then has to fit a phone screen with a
 * scene meant for a desktop window, which is only readable if the user zooms —
 * and zooming clips a canvas that cannot reflow. On a touch viewport the layout
 * is therefore capped at MAX_TOUCH_LAYOUT_WIDTH_DP (480) logical pixels.
 */
test.describe('Layout scale', () => {
  /**
   * The accessibility overlay mirrors every semantics node with a DOM box sized
   * from the density that node was laid out with, so the box of the full-screen
   * home container reports the app's *logical* width — what the cap constrains.
   */
  async function logicalWidth(
    page: Page,
    width: number,
    height: number,
  ): Promise<number> {
    await page.setViewportSize({ width, height });
    const home = page.getByText('My Little Bonsai');
    await expect(home).toBeVisible({ timeout: 20_000 });

    // Compose re-measures asynchronously after a resize; poll until two reads agree.
    let previous = -1;
    let settled = -1;
    await expect
      .poll(async () => {
        const box = await home.boundingBox();
        const current = box ? Math.round(box.width) : -1;
        const isSettled = current > 0 && current === previous;
        previous = current;
        if (isSettled) settled = current;
        return isSettled;
      })
      .toBe(true);
    return settled;
  }

  function expectAbout(actual: number, expected: number): void {
    expect(Math.abs(actual - expected)).toBeLessThanOrEqual(2);
  }

  test.beforeEach(async ({ page }) => {
    await page.goto('/');
    await page.waitForSelector('canvas', { timeout: 20_000 });
  });

  test('a touch viewport is never laid out wider than the cap', async ({
    page,
    isMobile,
  }) => {
    test.skip(!isMobile, 'the cap only applies to coarse-pointer viewports');

    expectAbout(await logicalWidth(page, 400, 851), 400);
    expectAbout(await logicalWidth(page, 960, 851), 480);
  });

  test('a mouse-driven window is laid out at its own width', async ({
    page,
    isMobile,
  }) => {
    test.skip(isMobile, 'covered by the touch viewport test');

    expectAbout(await logicalWidth(page, 640, 800), 640);
    expectAbout(await logicalWidth(page, 1280, 800), 1280);
  });
});
