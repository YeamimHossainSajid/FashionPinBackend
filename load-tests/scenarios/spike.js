/**
 * Spike Test Scenario for FashionPin API Gateway
 * Simulates extreme traffic bursts (e.g. flash sales, celebrity drop, notification push)
 * to test auto-scaling, gateway queuing, and circuit recovery.
 */

import { THRESHOLDS } from '../config/thresholds.js';
import { handleSummary } from '../helpers/report.js';
import executeE2EJourney from './gateway-e2e-journey.js';

export const options = {
  stages: [
    { duration: '30s', target: 10 },   // Low baseline traffic
    { duration: '15s', target: 350 },  // Extreme instantaneous spike!
    { duration: '1m', target: 350 },   // Hold the spike
    { duration: '30s', target: 20 },   // Abrupt drop back down
    { duration: '1m', target: 20 },    // Observe system recovery
    { duration: '15s', target: 0 },    // Drain
  ],
  thresholds: THRESHOLDS.spike,
};

export { handleSummary };

export default function () {
  executeE2EJourney();
}
