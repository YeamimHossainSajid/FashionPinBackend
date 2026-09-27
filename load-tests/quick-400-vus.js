/**
 * Quick 400 Concurrent VUs Instant Burst Test for FashionPin API Gateway
 * Launches 400 simultaneous virtual users at once hitting the Gateway.
 */

import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend, Counter } from 'k6/metrics';

// Custom Metrics
const gatewayLatency = new Trend('gateway_response_time_ms');
const successfulHits = new Counter('successful_hits_200');
const rateLimitedOrErrors = new Counter('gateway_errors');

const GATEWAY_URL = (__ENV.GATEWAY_URL || __ENV.BASE_URL || 'http://localhost:8080').replace(/\/+$/, '');

export const options = {
  // 400 simultaneous virtual users hitting AT ONCE
  scenarios: {
    instant_burst: {
      executor: 'per-vu-iterations',
      vus: 400,
      iterations: 1, // Every one of the 400 VUs executes immediately
      maxDuration: '30s',
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.05'], // Tolerate < 5% failure
    http_req_duration: ['p(95)<1500'],
  },
};

export default function () {
  const vuId = __VU;
  const iterId = __ITER;

  const endpoints = [
    '/api/v1/health',
    '/actuator/health',
    '/api/v1/categories',
    '/api/v1/product?page=0&size=10',
    '/api/search/products?q=dress',
    '/api/fashion/discover?page=0&size=5',
  ];

  // Distribute the 400 users across gateway endpoints
  const path = endpoints[vuId % endpoints.length];
  const url = `${GATEWAY_URL}${path}`;

  const headers = {
    'Accept': 'application/json',
    'User-Agent': `k6-burst-VU-${vuId}`,
    'X-Client-Platform': 'k6-burst-tester',
  };

  const res = http.get(url, { headers, timeout: '15s' });

  gatewayLatency.add(res.timings.duration);

  const isSuccess = check(res, {
    'status is 200 or 201': (r) => r.status === 200 || r.status === 201,
  });

  if (isSuccess) {
    successfulHits.add(1);
  } else {
    rateLimitedOrErrors.add(1);
  }
}

export function handleSummary(data) {
  const metrics = data.metrics || {};
  const httpDuration = metrics.http_req_duration ? metrics.http_req_duration.values : {};
  const httpReqs = metrics.http_reqs ? metrics.http_reqs.values : {};
  const httpFailed = metrics.http_req_failed ? metrics.http_req_failed.values : {};

  const total = httpReqs.count || 0;
  const p95 = httpDuration['p(95)'] ? `${httpDuration['p(95)'].toFixed(2)} ms` : 'N/A';
  const p99 = httpDuration['p(99)'] ? `${httpDuration['p(99)'].toFixed(2)} ms` : 'N/A';
  const avg = httpDuration.avg ? `${httpDuration.avg.toFixed(2)} ms` : 'N/A';
  const min = httpDuration.min ? `${httpDuration.min.toFixed(2)} ms` : 'N/A';
  const max = httpDuration.max ? `${httpDuration.max.toFixed(2)} ms` : 'N/A';
  const failRate = httpFailed.rate ? (httpFailed.rate * 100).toFixed(2) : '0.00';

  const output = [
    '',
    '======================================================================',
    '       ⚡ 400 CONCURRENT VIRTUAL USERS INSTANT BURST BENCHMARK ⚡       ',
    '======================================================================',
    `  Target Gateway:         ${GATEWAY_URL}`,
    `  Simultaneous Users:     400 VUs (all hitting at the exact same moment)`,
    `  Total Hits Dispatched:  ${total}`,
    `  P95 Latency:            ${p95}`,
    `  P99 Latency:            ${p99}`,
    `  Average Latency:        ${avg}`,
    `  Min / Max Latency:      ${min} / ${max}`,
    `  Failure / Drop Rate:    ${failRate}%`,
    '======================================================================',
    '',
  ].join('\n');

  return {
    'stdout': output,
    'reports/burst_400_summary.json': JSON.stringify(data, null, 2),
  };
}
