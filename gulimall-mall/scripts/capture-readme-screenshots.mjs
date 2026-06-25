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

const token = await adminToken();
const browser = await chromium.launch();
const page = await browser.newPage({ viewport: { width: 1440, height: 900 } });

// Storefront
await shot(page, "storefront-home.png", "http://localhost:3001/", {
  waitMs: 2500,
  fullPage: true,
});
await page.goto(`http://localhost:3001/item/${SKU_ID}`, { waitUntil: "networkidle", timeout: 60_000 });
await page.waitForTimeout(2500);
await page.screenshot({ path: path.join(outDir, "storefront-product-detail.png"), fullPage: false });
console.log("saved storefront-product-detail.png");
await page.evaluate(() => {
  const specs = [...document.querySelectorAll("h2")].find((el) =>
    /specification/i.test(el.textContent || "")
  );
  if (specs) specs.scrollIntoView({ block: "start" });
  else window.scrollBy(0, 720);
});
await page.waitForTimeout(800);
await page.screenshot({ path: path.join(outDir, "storefront-product-detail-specs.png"), fullPage: false });
console.log("saved storefront-product-detail-specs.png");
await shot(page, "storefront-search.png", "http://localhost:3001/search?q=iphone", {
  waitMs: 2000,
});

// Admin home (token + dynamic menu bootstrap via /home)
await page.goto("http://localhost:8001/");
await page.evaluate((t) => sessionStorage.setItem("token", t), token);
await page.goto("http://localhost:8001/#/home", { waitUntil: "networkidle" });
await page.waitForTimeout(4000);
await page.screenshot({ path: path.join(outDir, "admin-home.png"), fullPage: false });
console.log("saved admin-home.png");

await browser.close();
