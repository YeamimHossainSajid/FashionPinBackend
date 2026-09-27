/**
 * Stress Test Scenario for FashionPin API Gateway
 * Pushes system past typical capacity to identify bottleneck limits and breaking points.
 */

import { THRESHOLDS } from '../config/thresholds.js';
import { handleSummary } from '../helpers/report.js';
import executeE2EJourney from './gateway-e2e-journey.js';

export const options = {
  stages: [
    { duration: '1m', target: 50 },   // Warm up
    { duration: '2m', target: 150 },  // Moderate heavy load
    { duration: '3m', target: 300 },  // High stress
    { duration: '2m', target: 500 },  // Maximum stress / capacity limit
    { duration: '2m', target: 500 },  // Sustain peak stress
    { duration: '2m', target: 100 },  // Cool down
    { duration: '1m', target: 0 },    // Drain
  ],
  thresholds: THRESHOLDS.stress,
};

export { handleSummary };

export default function () {
  executeE2EJourney();
}
