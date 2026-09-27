/**
 * FashionPin API Gateway Load Testing Configuration
 * Defines target URLs, timeouts, headers, and environment variables.
 */

export const CONFIG = {
  // Target API Gateway URL. Can be overridden via environment variables:
  // e.g. k6 run -e GATEWAY_URL=http://194.163.166.16:8080 main.js
  gatewayUrl: (__ENV.GATEWAY_URL || __ENV.BASE_URL || 'http://localhost:8080').replace(/\/+$/, ''),

  // Default Request Timeout in milliseconds
  requestTimeout: __ENV.REQUEST_TIMEOUT || '10s',

  // Think time between simulated user actions (in seconds)
  thinkTimeMin: parseFloat(__ENV.THINK_TIME_MIN || '0.5'),
  thinkTimeMax: parseFloat(__ENV.THINK_TIME_MAX || '2.0'),

  // Default headers sent through API Gateway
  defaultHeaders: {
    'Accept': 'application/json',
    'Content-Type': 'application/json',
    'X-Client-Platform': 'k6-load-test',
    'X-Client-Version': '1.0.0',
    'User-Agent': 'FashionPin-LoadTest-k6/1.0',
  },

  // Test credentials pool or fallback
  defaultTestUser: {
    email: __ENV.TEST_USER_EMAIL || 'loadtest@fashionpin.com',
    password: __ENV.TEST_USER_PASSWORD || 'Password123!',
  },

  // Active scenario if running main.js
  scenario: (__ENV.SCENARIO || 'smoke').toLowerCase(),
};

/**
 * Returns formatted URL for endpoint going through the API Gateway
 * @param {string} path - API endpoint path (e.g. '/api/v1/product')
 * @returns {string} full Gateway URL
 */
export function getUrl(path) {
  const cleanPath = path.startsWith('/') ? path : `/${path}`;
  return `${CONFIG.gatewayUrl}${cleanPath}`;
}
