export const BASE_URL = __ENV.BASE_URL || 'https://www.yangzhangtech.online';

export const defaultThresholds = {
  http_req_failed: ['rate<0.02'],
  http_req_duration: ['p(95)<2500'],
};

export function checkOk(res, label) {
  return res.status === 200 && res.json('code') === 0;
}
