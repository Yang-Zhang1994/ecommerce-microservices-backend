import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL, checkOk, defaultThresholds } from './lib.js';

export const options = {
  scenarios: {
    product_read: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '45s', target: 15 },
        { duration: '2m', target: 30 },
        { duration: '2m', target: 30 },
        { duration: '30s', target: 0 },
      ],
      gracefulRampDown: '15s',
    },
  },
  thresholds: defaultThresholds,
};

const skuIds = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10];

export default function () {
  const skuId = skuIds[Math.floor(Math.random() * skuIds.length)];

  const tree = http.get(`${BASE_URL}/api/product/category/list/tree`, {
    tags: { name: 'category_tree' },
  });
  check(tree, { 'category tree': (r) => checkOk(r, 'tree') });

  const item = http.get(`${BASE_URL}/api/product/item/${skuId}`, {
    tags: { name: 'sku_item' },
  });
  check(item, { 'sku item': (r) => checkOk(r, 'item') });

  sleep(0.1);
}
