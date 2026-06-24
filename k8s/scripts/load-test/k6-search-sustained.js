import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL, checkOk, defaultThresholds } from './lib.js';

// Ramp to ~50 VUs with short think time → target ~40–80 RPS depending on latency.
export const options = {
  scenarios: {
    search_sustained: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '1m', target: 20 },
        { duration: '2m', target: 50 },
        { duration: '2m', target: 50 },
        { duration: '30s', target: 0 },
      ],
      gracefulRampDown: '15s',
    },
  },
  thresholds: defaultThresholds,
};

const keywords = ['Apple', 'Phone', 'Book', 'Shirt', 'Laptop'];

export default function () {
  const keyword = keywords[Math.floor(Math.random() * keywords.length)];
  const page = 1 + Math.floor(Math.random() * 3);
  const res = http.get(
    `${BASE_URL}/api/search/product/list?pageNum=${page}&pageSize=10&keyword=${keyword}`,
    { tags: { name: 'search_list' } },
  );
  check(res, { 'search 200 + code 0': (r) => checkOk(r, 'search') });
  sleep(0.05);
}
