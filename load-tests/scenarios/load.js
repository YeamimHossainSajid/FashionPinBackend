/**
 * Production Load Test Scenario for FashionPin API Gateway
 * Simulates normal-to-peak expected production traffic with gradual ramp-up and ramp-down.
 */

import { THRESHOLDS } from '../config/thresholds.js';
import { handleSummary } from '../helpers/report.js';
import executeE2EJourney from './gateway-e2e-journey.js';

export const options = {
  stages: [
    { duration: '1m', target: 20 },   // Warm up to 20 VUs
    { duration: '2m', target: 50 },   // Ramp up to normal load (50 VUs)
    { duration: '3m', target: 100 },  // Scale to peak traffic (100 VUs)
    { duration: '2m', target: 100 },  // Sustain peak load
    { duration: '1m', target: 20 },   // Ramp down
    { duration: '30s', target: 0 },   // Recovery and drain
  ],
  thresholds: THRESHOLDS.load,
};

export { handleSummary };

export default function () {
  executeE2EJourney();
}
