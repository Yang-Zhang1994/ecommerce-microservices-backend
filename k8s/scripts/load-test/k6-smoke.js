import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL, checkOk } from './lib.js';

export const options = {
  vus: 5,
  duration: '30s',
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<3000'],
  },
};

export default function () {
  const health = http.get(`${BASE_URL}/actuator/health`);
  check(health, { 'gateway health 200': (r) => r.status === 200 });

  const search = http.get(
    `${BASE_URL}/api/search/product/list?pageNum=1&pageSize=10&keyword=Apple`,
  );
  check(search, { 'search ok': (r) => checkOk(r, 'search') });

  const tree = http.get(`${BASE_URL}/api/product/category/list/tree`);
  check(tree, { 'category tree ok': (r) => checkOk(r, 'tree') });

  sleep(0.5);
}
