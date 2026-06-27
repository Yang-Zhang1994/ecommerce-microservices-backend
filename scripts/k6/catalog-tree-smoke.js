// k6 smoke test — category tree API
// Run: k6 run scripts/k6/catalog-tree-smoke.js
// Or:  k6 run --vus 20 --duration 30s scripts/k6/catalog-tree-smoke.js

import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE = __ENV.BASE_URL || 'http://ecommerce.com';

export const options = {
  vus: 10,
  duration: '30s',
};

export default function () {
  const res = http.get(`${BASE}/api/product/category/list/tree`);
  check(res, { 'category API status 200': (r) => r.status === 200 });
  sleep(0.5);
}
