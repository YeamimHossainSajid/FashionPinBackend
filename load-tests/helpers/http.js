/**
 * Standardized HTTP Client Wrapper for k6 Tests
 * Wraps k6/http with metrics tagging, tracing headers, and gateway tracking.
 */

import http from 'k6/http';
import { Trend, Counter } from 'k6/metrics';
import { CONFIG } from '../config/env.js';

// Custom Prometheus-compatible metrics for Gateway tracking
export const gatewayLatency = new Trend('fashionpin_gateway_latency_ms');
export const gatewayRequestsTotal = new Counter('fashionpin_gateway_requests_total');
export const gatewayErrorsTotal = new Counter('fashionpin_gateway_errors_total');

/**
 * Generates W3C traceparent header for distributed tracing into Gateway & Zipkin
 */
function generateTraceparent() {
  const version = '00';
  const traceId = Array.from({ length: 32 }, () => Math.floor(Math.random() * 16).toString(16)).join('');
  const parentId = Array.from({ length: 16 }, () => Math.floor(Math.random() * 16).toString(16)).join('');
  const traceFlags = '01';
  return `${version}-${traceId}-${parentId}-${traceFlags}`;
}

/**
 * Builds standard request headers, merging optional auth token and custom overrides
 */
export function buildHeaders(token = null, extraHeaders = {}) {
  const headers = {
    ...CONFIG.defaultHeaders,
    'traceparent': generateTraceparent(),
    ...extraHeaders,
  };

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  return headers;
}

/**
 * HTTP GET through API Gateway with metrics tagging
 */
export function gatewayGet(url, tags = {}, token = null, extraHeaders = {}) {
  const params = {
    headers: buildHeaders(token, extraHeaders),
    tags: { target: 'api-gateway', ...tags },
    timeout: CONFIG.requestTimeout,
  };

  gatewayRequestsTotal.add(1);
  const response = http.get(url, params);

  gatewayLatency.add(response.timings.duration, tags);
  if (response.status >= 400) {
    gatewayErrorsTotal.add(1, tags);
  }

  return response;
}

/**
 * HTTP POST through API Gateway with metrics tagging
 */
export function gatewayPost(url, body, tags = {}, token = null, extraHeaders = {}) {
  const payload = typeof body === 'string' ? body : JSON.stringify(body);
  const params = {
    headers: buildHeaders(token, extraHeaders),
    tags: { target: 'api-gateway', ...tags },
    timeout: CONFIG.requestTimeout,
  };

  gatewayRequestsTotal.add(1);
  const response = http.post(url, payload, params);

  gatewayLatency.add(response.timings.duration, tags);
  if (response.status >= 400) {
    gatewayErrorsTotal.add(1, tags);
  }

  return response;
}

/**
 * HTTP PATCH through API Gateway with metrics tagging
 */
export function gatewayPatch(url, body, tags = {}, token = null, extraHeaders = {}) {
  const payload = typeof body === 'string' ? body : JSON.stringify(body);
  const params = {
    headers: buildHeaders(token, extraHeaders),
    tags: { target: 'api-gateway', ...tags },
    timeout: CONFIG.requestTimeout,
  };

  gatewayRequestsTotal.add(1);
  const response = http.patch(url, payload, params);

  gatewayLatency.add(response.timings.duration, tags);
  if (response.status >= 400) {
    gatewayErrorsTotal.add(1, tags);
  }

  return response;
}
