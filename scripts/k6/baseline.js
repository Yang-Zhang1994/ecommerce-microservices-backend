/**
 * Gulimall production baseline — run against ALB or https://www.yangzhangtech.online
 *
 *   k6 run scripts/k6/baseline.js
 *   BASE_URL=https://www.yangzhangtech.online k6 run scripts/k6/baseline.js
 */
import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE = __ENV.BASE_URL || 'https://www.yangzhangtech.online';

export const options = {
  scenarios: {
    health_smoke: {
      executor: 'constant-arrival-rate',
      rate: Number(__ENV.K6_HEALTH_RPS || 20),
      timeUnit: '1s',
      duration: __ENV.K6_DURATION || '2m',
      preAllocatedVUs: 10,
      maxVUs: 50,
    },
    auth_ping: {
      executor: 'constant-arrival-rate',
      rate: Number(__ENV.K6_AUTH_RPS || 10),
      timeUnit: '1s',
      duration: __ENV.K6_DURATION || '2m',
      startTime: '10s',
      preAllocatedVUs: 5,
      maxVUs: 30,
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<800'],
    'http_req_duration{scenario:health_smoke}': ['p(95)<500'],
    'http_req_duration{scenario:auth_ping}': ['p(95)<1000'],
  },
};

export default function () {
  const health = http.get(`${BASE}/actuator/health`, { tags: { name: 'health' } });
  check(health, {
    'health status 200': (r) => r.status === 200,
  });
  sleep(0.05);

  const ping = http.get(`${BASE}/api/auth/ping`, { tags: { name: 'auth_ping' } });
  check(ping, {
    'auth ping 2xx/503': (r) => r.status === 200 || r.status === 503,
  });
  sleep(0.05);
}
