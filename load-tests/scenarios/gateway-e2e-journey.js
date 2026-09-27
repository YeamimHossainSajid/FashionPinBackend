/**
 * End-to-End Realistic User Journey through API Gateway
 * Simulates real user patterns across catalog browsing, search, discovery, and authenticated actions.
 */

import { check, group, sleep } from 'k6';
import { getUrl, CONFIG } from '../config/env.js';
import { gatewayGet, gatewayPost } from '../helpers/http.js';
import { register, login, getOrCreateAuthToken } from '../helpers/auth.js';
import {
  SEARCH_KEYWORDS,
  CATEGORIES,
  randomItem,
  randomInt,
  generateGuestToken,
} from '../helpers/data.js';

/**
 * Sleeps for a realistic human think time between requests
 */
export function thinkTime() {
  const min = CONFIG.thinkTimeMin;
  const max = CONFIG.thinkTimeMax;
  const duration = Math.random() * (max - min) + min;
  sleep(duration);
}

/**
 * 1. Health and Gateway Operational Check
 */
export function gatewayHealthCheck() {
  group('Gateway Health & Edge Status', () => {
    const healthUrl = getUrl('/actuator/health');
    const res = gatewayGet(healthUrl, { endpoint: 'gateway_health' });

    check(res, {
      'gateway health status is 200': (r) => r.status === 200,
    });
  });
}

/**
 * 2. Anonymous User Browsing Catalog & Fashion Discovery
 */
export function anonymousBrowseJourney() {
  group('Anonymous Browsing & Discovery', () => {
    // A. Browse active categories
    const categoriesUrl = getUrl('/api/v1/categories');
    const catRes = gatewayGet(categoriesUrl, { endpoint: 'catalog_categories' });
    check(catRes, {
      'categories status is 200': (r) => r.status === 200,
    });
    thinkTime();

    // B. Browse paginated product catalog with category filter
    const selectedCategory = randomItem(CATEGORIES);
    const catalogUrl = getUrl(`/api/v1/product?category=${selectedCategory}&page=0&size=20`);
    const catalogRes = gatewayGet(catalogUrl, { endpoint: 'catalog_browse' });
    check(catalogRes, {
      'catalog browse status is 200': (r) => r.status === 200,
    });
    thinkTime();

    // C. Perform catalog search via Search Service through Gateway
    const keyword = randomItem(SEARCH_KEYWORDS);
    const searchUrl = getUrl(`/api/search/products?q=${encodeURIComponent(keyword)}&pageSize=15`);
    const searchRes = gatewayGet(searchUrl, { endpoint: 'search' });
    check(searchRes, {
      'search status is 200': (r) => r.status === 200,
    });
    thinkTime();

    // D. Discover visual fashion feed via Fashion Discovery Service through Gateway
    const discoveryUrl = getUrl('/api/fashion/discover?page=0&size=10');
    const discoveryRes = gatewayGet(discoveryUrl, { endpoint: 'discovery_feed' });
    check(discoveryRes, {
      'discovery status is 200': (r) => r.status === 200,
    });
    thinkTime();
  });
}

/**
 * 3. Guest Shopping Cart Flow (Header-based Session Tracking)
 */
export function guestCartJourney() {
  group('Guest Cart Operations', () => {
    const guestToken = generateGuestToken();
    const cartUrl = getUrl('/api/v1/cart');

    // Retrieve initial guest cart
    const res = gatewayGet(
      cartUrl,
      { endpoint: 'cart_guest' },
      null,
      { 'X-Guest-Session-Token': guestToken }
    );

    check(res, {
      'guest cart status is 200': (r) => r.status === 200,
    });
    thinkTime();
  });
}

/**
 * 4. Authenticated User Flow (JWT Verification at Gateway -> Downstream Services)
 */
export function authenticatedShopperJourney(existingToken = null) {
  group('Authenticated User Flow', () => {
    let token = existingToken;

    // Authenticate if no token passed
    if (!token) {
      token = getOrCreateAuthToken();
    }

    if (!token) {
      // In case auth service is down or rate limited, gracefully proceed
      return;
    }

    // A. Query Profile Me (Gateway verifies JWT and passes X-User-Id downstream)
    const profileUrl = getUrl('/api/v1/profile/me');
    const profileRes = gatewayGet(profileUrl, { endpoint: 'profile_me' }, token);
    check(profileRes, {
      'profile me status is 200': (r) => r.status === 200,
    });
    thinkTime();

    // B. Query User Cart with JWT Bearer Token
    const cartUrl = getUrl('/api/v1/cart');
    const cartRes = gatewayGet(cartUrl, { endpoint: 'cart_auth' }, token);
    check(cartRes, {
      'auth cart status is 200': (r) => r.status === 200,
    });
    thinkTime();
  });
}

/**
 * Unified Journey: Weighted execution simulating realistic real-world traffic mix
 */
export default function executeE2EJourney() {
  const roll = Math.random();

  if (roll < 0.05) {
    // 5% edge health checks
    gatewayHealthCheck();
  } else if (roll < 0.65) {
    // 60% anonymous browsing and visual discovery
    anonymousBrowseJourney();
  } else if (roll < 0.85) {
    // 20% guest shopping cart actions
    guestCartJourney();
  } else {
    // 15% authenticated customer journey
    authenticatedShopperJourney();
  }
}
