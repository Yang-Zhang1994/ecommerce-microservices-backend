import { test, expect } from '@playwright/test';

/**
 * Completes Stripe Checkout with test card for a session created via API.
 * Usage:
 *   CHECKOUT_URL='https://checkout.stripe.com/...' npx playwright test e2e/stripe-checkout-once.spec.ts
 */
test.describe('Stripe Checkout test card', () => {
  test.beforeEach(() => {
    test.skip(
      !process.env.CHECKOUT_URL?.trim(),
      'Set CHECKOUT_URL from POST /api/order/pay/stripe/checkout-session',
    );
  });

  test('pay with 4242 test card', async ({ page }) => {
    test.setTimeout(120_000);
    const checkoutUrl = process.env.CHECKOUT_URL!.trim();
    await page.goto(checkoutUrl, { waitUntil: 'domcontentloaded', timeout: 60_000 });

    await page.getByRole('textbox', { name: 'Email' }).fill('a1234@test.example');
    await page.getByRole('textbox', { name: 'Card number' }).fill('4242 4242 4242 4242');
    await page.getByRole('textbox', { name: 'Expiration' }).fill('12 / 34');
    await page.getByRole('textbox', { name: 'CVC' }).fill('123');
    await page.getByRole('textbox', { name: 'Cardholder name' }).fill('Test User');

    await page.getByRole('button', { name: /^Pay$/ }).click();

    await page.waitForURL(/\/order\/success/, { timeout: 90_000 });
    await expect(
      page.getByRole('heading', { name: /Payment Succeeded|支付成功/i }),
    ).toBeVisible({ timeout: 30_000 });
  });
});
