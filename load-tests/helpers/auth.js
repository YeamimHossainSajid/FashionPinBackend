/**
 * Authentication Helper for FashionPin Load Tests
 * Manages user registration and JWT authentication through API Gateway.
 */

import { check } from 'k6';
import { getUrl } from '../config/env.js';
import { gatewayPost } from './http.js';
import { generateUser } from './data.js';

// Cache tokens per VU (Virtual User)
const tokenCache = new Map();

/**
 * Registers a new user through the API Gateway
 * @param {object} user - Optional user credentials object
 * @returns {object} { success: boolean, user: object, response: object }
 */
export function register(user = null) {
  const payload = user || generateUser();
  const url = getUrl('/api/v1/auth/register');

  const res = gatewayPost(url, payload, { endpoint: 'auth_register' });

  const isSuccess = check(res, {
    'register status is 201 or 200 or 409 (already exists)': (r) =>
      r.status === 201 || r.status === 200 || r.status === 409,
  });

  let token = null;
  try {
    const json = res.json();
    if (json && json.data && json.data.accessToken) {
      token = json.data.accessToken;
    }
  } catch (e) {
    // Ignore parse errors on conflict
  }

  return {
    success: isSuccess,
    user: payload,
    token,
    response: res,
  };
}

/**
 * Authenticates user through the API Gateway and retrieves JWT access token
 * @param {string} email
 * @param {string} password
 * @returns {string|null} JWT access token or null on failure
 */
export function login(email, password) {
  const cacheKey = `${email}:${password}`;
  if (tokenCache.has(cacheKey)) {
    return tokenCache.get(cacheKey);
  }

  const url = getUrl('/api/v1/auth/login');
  const payload = { email, password };

  const res = gatewayPost(url, payload, { endpoint: 'auth_login' });

  let accessToken = null;
  const isOk = check(res, {
    'login status is 200': (r) => r.status === 200,
  });

  if (isOk) {
    try {
      const json = res.json();
      if (json && json.data && json.data.accessToken) {
        accessToken = json.data.accessToken;
        tokenCache.set(cacheKey, accessToken);
      }
    } catch (e) {
      console.warn(`Failed to parse login response: ${e.message}`);
    }
  }

  return accessToken;
}

/**
 * Ensures a valid JWT token exists: registers a user or logs in with fallback
 * @returns {string|null} JWT token
 */
export function getOrCreateAuthToken() {
  const newUser = generateUser();
  const regResult = register(newUser);

  if (regResult.token) {
    return regResult.token;
  }

  // Fallback to login if registration returns standard user or requires separate login
  const token = login(newUser.email, newUser.password);
  return token;
}
