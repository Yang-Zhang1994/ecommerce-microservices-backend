// k6 压测脚本 - gulimall API
// 运行: k6 run script.js
// 或指定并发/时长: k6 run --vus 20 --duration 30s script.js

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
