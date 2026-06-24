import { test, expect } from '@playwright/test';

/**
 * Simulates the browser after Stripe redirects back: same host already has SESSION cookie.
 * Run with a live cookie from DevTools → Application → Cookies → SESSION value:
 *
 *   TEST_SESSION='your-session-id' npm run test:e2e
 *
 * Requires gateway :88, mall `npm run dev` on :3001, and Redis/auth services.
 */
test.describe('Payment success → user info → order list', () => {
  test.beforeEach(() => {
    test.skip(
      !process.env.TEST_SESSION || process.env.TEST_SESSION.trim() === '',
      'Set TEST_SESSION to the browser SESSION cookie value (see e2e/payment-return-flow.spec.ts)',
    );
  });

  test('success page shows Hi after sync; View Order List shows My Orders with data or empty row', async ({
    browser,
  }) => {
    const baseURL = process.env.PLAYWRIGHT_BASE_URL || 'http://localhost:3001';
    const session = process.env.TEST_SESSION!.trim();

    const context = await browser.newContext();
    await context.addCookies([
      {
        name: 'SESSION',
        value: session,
        url: baseURL,
      },
    ]);
    const page = await context.newPage();

    await page.goto(
      `${baseURL}/order/success?paid=1&orderSn=GL1778299761722`,
    );

    await expect(page.getByRole('heading', { name: /Payment Succeeded/i })).toBeVisible();

    // Client HeaderAuthNav + syncMemberSessionFromServer
    await expect(page.getByText(/^Hi,/)).toBeVisible();
    await expect(page.getByRole('link', { name: 'Sign in' })).toHaveCount(0);

    await page.getByRole('link', { name: 'View Order List' }).click();

    await expect(page).toHaveURL(/\/order(\?|$)/);
    await expect(page).not.toHaveURL(/\/login/);
    await expect(page.getByRole('heading', { name: 'My Orders', exact: true })).toBeVisible();

    const hasOrders = page.getByText(/Order No\.:/);
    const empty = page.getByText('No orders under this status.');
    await expect(hasOrders.or(empty).first()).toBeVisible();
  });
});
