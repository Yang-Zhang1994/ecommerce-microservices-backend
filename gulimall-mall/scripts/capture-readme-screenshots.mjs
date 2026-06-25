import { chromium } from "@playwright/test";
import path from "node:path";
import { fileURLToPath } from "node:url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const outDir = path.resolve(__dirname, "../../docs/images");
const SKU_ID = process.env.SCREENSHOT_SKU_ID || "4";

async function adminToken() {
  const res = await fetch("http://localhost:3088/api/sys/login", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      username: "admin",
      password: "admin",
      uuid: "screenshot",
      captcha: "screenshot",
    }),
  });
  const body = await res.json();
  if (!body.token) throw new Error(`Admin login failed: ${JSON.stringify(body)}`);
  return body.token;
}

async function shot(page, name, url, opts = {}) {
  await page.goto(url, { waitUntil: "networkidle", timeout: 60_000 });
  if (opts.waitMs) await page.waitForTimeout(opts.waitMs);
  await page.screenshot({
    path: path.join(outDir, name),
    fullPage: opts.fullPage ?? false,
  });
  console.log("saved", name);
}

async function adminPage(page, token, routePath, name) {
  await page.goto("http://localhost:8001/");
  await page.evaluate((t) => sessionStorage.setItem("token", t), token);
  await page.goto("http://localhost:8001/#/home", { waitUntil: "networkidle" });
  await page.waitForTimeout(4000);
  await page.goto(`http://localhost:8001/#/${routePath}`, { waitUntil: "networkidle" });
  await page.waitForTimeout(3500);
  if (page.url().includes("login")) {
    console.warn("skip", name, "- redirected to login");
    return false;
  }
  await page.screenshot({ path: path.join(outDir, name), fullPage: false });
  console.log("saved", name, page.url());
  return true;
}

const token = await adminToken();
const browser = await chromium.launch();
const page = await browser.newPage({ viewport: { width: 1440, height: 900 } });

// Storefront
await shot(page, "storefront-home.png", "http://localhost:3001/", {
  waitMs: 2500,
  fullPage: true,
});
await shot(page, "storefront-product-detail.png", `http://localhost:3001/item/${SKU_ID}`, {
  waitMs: 2500,
});
await shot(page, "storefront-search.png", "http://localhost:3001/search?q=iphone", {
  waitMs: 2000,
});
await shot(page, "storefront-sign-in.png", "http://localhost:3001/login", { waitMs: 2000 });
await shot(page, "storefront-order-confirm.png", "http://localhost:3001/order/confirm", {
  waitMs: 2000,
});

// Admin (token + dynamic menu bootstrap via /home)
  ["product-spu", "admin-products.png"],
  ["order-order", "admin-orders.png"],
  ["order-payment", "admin-payments.png"],
  ["ware-wareinfo", "admin-warehouse.png"],
  ["member-member", "admin-members.png"],
  ["coupon-memberprice", "admin-marketing.png"],
];

for (const [routePath, file] of adminRoutes) {
  await adminPage(page, token, routePath, file);
}

await browser.close();
