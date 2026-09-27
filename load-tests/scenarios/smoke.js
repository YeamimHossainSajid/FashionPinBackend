/**
 * Smoke Test Scenario for FashionPin API Gateway
 * Minimal load (1-3 VUs for 30s) to quickly verify gateway routes and basic connectivity.
 */

import { THRESHOLDS } from '../config/thresholds.js';
import { handleSummary } from '../helpers/report.js';
import {
  gatewayHealthCheck,
  anonymousBrowseJourney,
  guestCartJourney,
  authenticatedShopperJourney,
} from './gateway-e2e-journey.js';

export const options = {
  vus: 2,
  duration: '30s',
  thresholds: THRESHOLDS.smoke,
};

export { handleSummary };

export default function () {
  gatewayHealthCheck();
  anonymousBrowseJourney();
  guestCartJourney();
  authenticatedShopperJourney();
}
