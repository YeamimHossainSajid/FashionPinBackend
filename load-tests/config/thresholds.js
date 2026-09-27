/**
 * Performance Thresholds (SLAs) for FashionPin API Gateway Load Tests
 * Defines latency percentiles and error tolerance rules.
 */

export const THRESHOLDS = {
  // Smoke testing thresholds (strict sanity rules)
  smoke: {
    http_req_failed: ['rate<0.01'],              // Less than 1% failure rate
    http_req_duration: ['p(95)<400', 'p(99)<800'],// 95% of requests under 400ms
  },

  // Standard load testing thresholds (realistic production SLA)
  load: {
    http_req_failed: ['rate<0.02'],              // Less than 2% failure rate
    http_req_duration: ['p(95)<600', 'p(99)<1200'],// 95% under 600ms, 99% under 1.2s
    'http_req_duration{endpoint:gateway_health}': ['p(95)<150'],
    'http_req_duration{endpoint:catalog_browse}': ['p(95)<500'],
    'http_req_duration{endpoint:search}': ['p(95)<700'],
    'http_req_duration{endpoint:auth_login}': ['p(95)<600'],
  },

  // Stress testing thresholds (discovering degradation limits)
  stress: {
    http_req_failed: ['rate<0.08'],              // Tolerate up to 8% failure under extreme load
    http_req_duration: ['p(95)<1500', 'p(99)<3000'],
  },

  // Spike testing thresholds (sudden burst absorption)
  spike: {
    http_req_failed: ['rate<0.05'],              // Under 5% drops during sudden spikes
    http_req_duration: ['p(95)<1200', 'p(99)<2500'],
  },

  // Soak / Endurance thresholds (monitoring drift & leaks)
  soak: {
    http_req_failed: ['rate<0.01'],              // Zero/low errors over long duration
    http_req_duration: ['p(95)<500', 'p(99)<1000'],
  },
};
