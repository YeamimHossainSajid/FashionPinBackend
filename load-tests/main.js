/**
 * Unified Main Entrypoint for FashionPin k6 Load Tests
 * Dynamically selects test profile (smoke, load, stress, spike, soak) via environment variable:
 *
 * Example:
 *   k6 run -e SCENARIO=smoke main.js
 *   k6 run -e SCENARIO=load -e GATEWAY_URL=http://194.163.166.16:8080 main.js
 *   k6 run -e SCENARIO=stress main.js
 */

import { THRESHOLDS } from './config/thresholds.js';
import { handleSummary } from './helpers/report.js';
import executeE2EJourney from './scenarios/gateway-e2e-journey.js';

// Select scenario configuration based on __ENV.SCENARIO (defaults to 'smoke')
const scenarioType = (__ENV.SCENARIO || 'smoke').toLowerCase();

let selectedOptions;

switch (scenarioType) {
  case 'load':
    selectedOptions = {
      stages: [
        { duration: '1m', target: 20 },
        { duration: '2m', target: 50 },
        { duration: '3m', target: 100 },
        { duration: '2m', target: 100 },
        { duration: '1m', target: 20 },
        { duration: '30s', target: 0 },
      ],
      thresholds: THRESHOLDS.load,
    };
    break;

  case 'stress':
    selectedOptions = {
      stages: [
        { duration: '1m', target: 50 },
        { duration: '2m', target: 150 },
        { duration: '3m', target: 300 },
        { duration: '2m', target: 500 },
        { duration: '2m', target: 500 },
        { duration: '2m', target: 100 },
        { duration: '1m', target: 0 },
      ],
      thresholds: THRESHOLDS.stress,
    };
    break;

  case 'spike':
    selectedOptions = {
      stages: [
        { duration: '30s', target: 10 },
        { duration: '15s', target: 350 },
        { duration: '1m', target: 350 },
        { duration: '30s', target: 20 },
        { duration: '1m', target: 20 },
        { duration: '15s', target: 0 },
      ],
      thresholds: THRESHOLDS.spike,
    };
    break;

  case 'soak':
    selectedOptions = {
      stages: [
        { duration: '2m', target: 30 },
        { duration: '30m', target: 30 },
        { duration: '2m', target: 0 },
      ],
      thresholds: THRESHOLDS.soak,
    };
    break;

  case 'smoke':
  default:
    selectedOptions = {
      vus: 2,
      duration: '30s',
      thresholds: THRESHOLDS.smoke,
    };
    break;
}

export const options = selectedOptions;

export { handleSummary };

export default function () {
  executeE2EJourney();
}
