import { chromium } from "@playwright/test";
import path from "node:path";
import { fileURLToPath } from "node:url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const outDir = path.resolve(__dirname, "../../docs/images");
const BASE = process.env.MALL_ORIGIN || "http://localhost:3001";
const USER = process.env.MALL_USER || "a1234";
const PASS = process.env.MALL_PASS || "123456";
/** SKUs to show in cart; checkout uses CHECKOUT_SKU only. */
const CART_SKUS = (process.env.CART_SKUS || "1,2,4").split(",").map((s) => Number(s.trim()));
const CHECKOUT_SKU = Number(process.env.CHECKOUT_SKU || "4");

async function login(page) {
  await page.goto(`${BASE}/login`, { waitUntil: "networkidle", timeout: 60_000 });
  await page.locator('input[autocomplete="username"]').fill(USER);
  await page.locator('input[autocomplete="current-password"]').fill(PASS);
  await page.locator('button[type="submit"]').click();
  await page.waitForURL((url) => !url.pathname.includes("/login"), { timeout: 60_000 });
}

async function clearCart(request) {
  const res = await request.get(`${BASE}/api/cart/current`);
  const body = await res.json();
  const items = body?.data?.items ?? [];
  for (const it of items) {
    const skuId = it.skuId;
    if (skuId == null) continue;
    await request.post(`${BASE}/api/cart/item/delete?skuId=${skuId}`);
  }
}

async function addSku(request, skuId, num = 1) {
  await request.get(`${BASE}/api/cart/add?skuId=${skuId}&num=${num}`);
}

async function setChecked(request, skuId, checked) {
  await request.post(`${BASE}/api/cart/item/check?skuId=${skuId}&checked=${checked}`);
}

const browser = await chromium.launch();
const context = await browser.newContext({ viewport: { width: 1440, height: 900 } });
const page = await context.newPage();
const request = context.request;

await login(page);
await clearCart(request);
for (const skuId of CART_SKUS) {
  await addSku(request, skuId, 1);
}

await page.goto(`${BASE}/cart/list`, { waitUntil: "networkidle", timeout: 60_000 });
await page.waitForTimeout(2000);
await page.screenshot({ path: path.join(outDir, "storefront-cart.png"), fullPage: false });
console.log("saved storefront-cart.png");

for (const skuId of CART_SKUS) {
  await setChecked(request, skuId, skuId === CHECKOUT_SKU);
}
await page.goto(`${BASE}/order/confirm`, { waitUntil: "networkidle", timeout: 60_000 });
await page.waitForTimeout(2000);
await page.screenshot({ path: path.join(outDir, "storefront-order-confirm.png"), fullPage: false });
console.log("saved storefront-order-confirm.png");

await page.getByRole("button", { name: "Submit Order" }).click();
await page.waitForURL(/\/order\/pay/, { timeout: 60_000 });
await page.waitForTimeout(1500);
await page.screenshot({ path: path.join(outDir, "storefront-payment.png"), fullPage: false });
console.log("saved storefront-payment.png");

await browser.close();
