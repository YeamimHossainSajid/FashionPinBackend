/**
 * Soak / Endurance Test Scenario for FashionPin API Gateway
 * Steady moderate load over an extended period to reveal memory leaks,
 * database pool exhaustion, or JVM GC degradation.
 */

import { THRESHOLDS } from '../config/thresholds.js';
import { handleSummary } from '../helpers/report.js';
import executeE2EJourney from './gateway-e2e-journey.js';

export const options = {
  stages: [
    { duration: '2m', target: 30 },    // Ramp up to steady state (30 VUs)
    { duration: '30m', target: 30 },   // Sustain constant load for 30 minutes
    { duration: '2m', target: 0 },     // Ramp down
  ],
  thresholds: THRESHOLDS.soak,
};

export { handleSummary };

export default function () {
  executeE2EJourney();
}
