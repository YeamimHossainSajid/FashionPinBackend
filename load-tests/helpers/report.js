/**
 * Standalone HTML and JSON Summary Reporter for k6 Load Tests
 * 100% self-contained with ZERO external/network module dependencies.
 * Generates an interactive visual report dashboard and clean console summary.
 */

/**
 * Formats milliseconds into readable string
 */
function formatMs(val) {
  if (val === undefined || val === null) return 'N/A';
  return `${Number(val).toFixed(2)} ms`;
}

/**
 * Clean terminal/console summary formatted without any external libraries
 */
export function generateTextSummary(data) {
  const metrics = data.metrics || {};
  const httpDuration = metrics.http_req_duration ? metrics.http_req_duration.values : {};
  const httpReqs = metrics.http_reqs ? metrics.http_reqs.values : {};
  const httpFailed = metrics.http_req_failed ? metrics.http_req_failed.values : {};
  const vus = metrics.vus ? metrics.vus.values : {};

  const totalReqs = httpReqs.count || 0;
  const rate = httpReqs.rate ? httpReqs.rate.toFixed(1) : '0.0';
  const failRate = httpFailed.rate ? (httpFailed.rate * 100).toFixed(2) : '0.00';
  const p95 = formatMs(httpDuration['p(95)']);
  const p99 = formatMs(httpDuration['p(99)']);
  const avg = formatMs(httpDuration.avg);

  return [
    '',
    '======================================================================',
    '             FashionPin API Gateway — k6 Load Test Summary            ',
    '======================================================================',
    `  Total HTTP Requests:   ${totalReqs.toLocaleString()}`,
    `  Request Rate:          ${rate} req/s`,
    `  Failure Rate:          ${failRate}%`,
    `  P95 Response Latency:  ${p95}`,
    `  P99 Response Latency:  ${p99}`,
    `  Average Latency:       ${avg}`,
    `  Active Virtual Users:  ${vus.value || 0}`,
    '======================================================================',
    '  Detailed HTML report:  reports/summary.html',
    '  Raw JSON metrics:      reports/summary.json',
    '======================================================================',
    '',
  ].join('\n');
}

/**
 * Generates standalone HTML report
 */
export function generateHtmlReport(data) {
  const metrics = data.metrics || {};
  const httpDuration = metrics.http_req_duration ? metrics.http_req_duration.values : {};
  const httpReqs = metrics.http_reqs ? metrics.http_reqs.values : {};
  const httpFailed = metrics.http_req_failed ? metrics.http_req_failed.values : {};
  const vus = metrics.vus ? metrics.vus.values : {};

  const totalRequests = httpReqs.count || 0;
  const reqRate = httpReqs.rate ? httpReqs.rate.toFixed(1) : 0;
  const failureRate = httpFailed.rate ? (httpFailed.rate * 100).toFixed(2) : '0.00';
  const p95Latency = formatMs(httpDuration['p(95)']);
  const p99Latency = formatMs(httpDuration['p(99)']);
  const avgLatency = formatMs(httpDuration.avg);
  const minLatency = formatMs(httpDuration.min);
  const maxLatency = formatMs(httpDuration.max);
  const maxVUs = vus.value || (metrics.vus_max ? metrics.vus_max.values.value : 'N/A');

  // Check overall pass/fail status based on SLA thresholds
  let passed = true;
  for (const metricKey in metrics) {
    if (metrics[metricKey].thresholds) {
      for (const th in metrics[metricKey].thresholds) {
        if (!metrics[metricKey].thresholds[th].ok) {
          passed = false;
          break;
        }
      }
    }
  }

  const statusBadge = passed
    ? `<span style="background: #10B981; color: white; padding: 6px 16px; border-radius: 9999px; font-weight: bold;">PASSED</span>`
    : `<span style="background: #EF4444; color: white; padding: 6px 16px; border-radius: 9999px; font-weight: bold;">FAILED</span>`;

  return `<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>FashionPin — API Gateway Load Test Report</title>
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <style>
    :root {
      --bg: #0F172A;
      --card-bg: #1E293B;
      --border: #334155;
      --text: #F8FAFC;
      --text-muted: #94A3B8;
      --accent: #6366F1;
      --success: #10B981;
      --danger: #EF4444;
      --font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
    }
    body {
      margin: 0;
      padding: 32px 16px;
      background-color: var(--bg);
      color: var(--text);
      font-family: var(--font-family);
    }
    .container {
      max-width: 1100px;
      margin: 0 auto;
    }
    .header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 24px;
      padding-bottom: 16px;
      border-bottom: 1px solid var(--border);
    }
    .header h1 {
      margin: 0;
      font-size: 26px;
      letter-spacing: -0.5px;
    }
    .subtitle {
      color: var(--text-muted);
      margin-top: 6px;
      font-size: 14px;
    }
    .grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
      gap: 16px;
      margin-bottom: 32px;
    }
    .card {
      background: var(--card-bg);
      border: 1px solid var(--border);
      border-radius: 12px;
      padding: 20px;
    }
    .card-title {
      font-size: 13px;
      text-transform: uppercase;
      letter-spacing: 0.05em;
      color: var(--text-muted);
      margin-bottom: 8px;
    }
    .card-value {
      font-size: 28px;
      font-weight: 700;
      color: var(--text);
    }
    .table-container {
      background: var(--card-bg);
      border: 1px solid var(--border);
      border-radius: 12px;
      overflow: hidden;
      margin-bottom: 32px;
    }
    .table-title {
      padding: 16px 20px;
      font-size: 18px;
      font-weight: 600;
      border-bottom: 1px solid var(--border);
    }
    table {
      width: 100%;
      border-collapse: collapse;
      text-align: left;
    }
    th, td {
      padding: 12px 20px;
      font-size: 14px;
      border-bottom: 1px solid var(--border);
    }
    th {
      background: #182234;
      color: var(--text-muted);
      font-weight: 600;
    }
    tr:last-child td {
      border-bottom: none;
    }
    .footer {
      text-align: center;
      color: var(--text-muted);
      font-size: 12px;
      margin-top: 40px;
    }
  </style>
</head>
<body>
  <div class="container">
    <div class="header">
      <div>
        <h1>FashionPin &mdash; API Gateway Benchmark</h1>
        <div class="subtitle">Distributed Microservices Edge Gateway Load Test Results &bull; k6 Performance Engine</div>
      </div>
      <div>
        ${statusBadge}
      </div>
    </div>

    <div class="grid">
      <div class="card">
        <div class="card-title">Total Requests</div>
        <div class="card-value">${totalRequests.toLocaleString()}</div>
      </div>
      <div class="card">
        <div class="card-title">Throughput</div>
        <div class="card-value">${reqRate} <span style="font-size: 16px; font-weight: normal; color: var(--text-muted)">req/s</span></div>
      </div>
      <div class="card">
        <div class="card-title">Error Rate</div>
        <div class="card-value" style="color: ${Number(failureRate) > 1 ? 'var(--danger)' : 'var(--success)'}">${failureRate}%</div>
      </div>
      <div class="card">
        <div class="card-title">Max VUs</div>
        <div class="card-value">${maxVUs}</div>
      </div>
    </div>

    <div class="grid">
      <div class="card">
        <div class="card-title">P95 Latency</div>
        <div class="card-value" style="color: #38BDF8">${p95Latency}</div>
      </div>
      <div class="card">
        <div class="card-title">P99 Latency</div>
        <div class="card-value" style="color: #A78BFA">${p99Latency}</div>
      </div>
      <div class="card">
        <div class="card-title">Average Latency</div>
        <div class="card-value">${avgLatency}</div>
      </div>
      <div class="card">
        <div class="card-title">Min / Max Latency</div>
        <div class="card-value" style="font-size: 20px;">${minLatency} / ${maxLatency}</div>
      </div>
    </div>

    <div class="table-container">
      <div class="table-title">Gateway Performance Metrics Breakdown</div>
      <table>
        <thead>
          <tr>
            <th>Metric Name</th>
            <th>Type</th>
            <th>Avg</th>
            <th>Min</th>
            <th>Med (P50)</th>
            <th>P90</th>
            <th>P95</th>
            <th>Max</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td><strong>http_req_duration (Total)</strong></td>
            <td>HTTP Latency</td>
            <td>${formatMs(httpDuration.avg)}</td>
            <td>${formatMs(httpDuration.min)}</td>
            <td>${formatMs(httpDuration.med)}</td>
            <td>${formatMs(httpDuration['p(90)'])}</td>
            <td>${formatMs(httpDuration['p(95)'])}</td>
            <td>${formatMs(httpDuration.max)}</td>
          </tr>
          ${
            metrics.fashionpin_gateway_latency_ms
              ? `<tr>
                  <td><strong>fashionpin_gateway_latency_ms</strong></td>
                  <td>Custom Trend</td>
                  <td>${formatMs(metrics.fashionpin_gateway_latency_ms.values.avg)}</td>
                  <td>${formatMs(metrics.fashionpin_gateway_latency_ms.values.min)}</td>
                  <td>${formatMs(metrics.fashionpin_gateway_latency_ms.values.med)}</td>
                  <td>${formatMs(metrics.fashionpin_gateway_latency_ms.values['p(90)'])}</td>
                  <td>${formatMs(metrics.fashionpin_gateway_latency_ms.values['p(95)'])}</td>
                  <td>${formatMs(metrics.fashionpin_gateway_latency_ms.values.max)}</td>
                </tr>`
              : ''
          }
          <tr>
            <td><strong>http_req_connecting</strong></td>
            <td>TCP Handshake</td>
            <td>${formatMs(metrics.http_req_connecting ? metrics.http_req_connecting.values.avg : 0)}</td>
            <td>${formatMs(metrics.http_req_connecting ? metrics.http_req_connecting.values.min : 0)}</td>
            <td>${formatMs(metrics.http_req_connecting ? metrics.http_req_connecting.values.med : 0)}</td>
            <td>${formatMs(metrics.http_req_connecting ? metrics.http_req_connecting.values['p(90)'] : 0)}</td>
            <td>${formatMs(metrics.http_req_connecting ? metrics.http_req_connecting.values['p(95)'] : 0)}</td>
            <td>${formatMs(metrics.http_req_connecting ? metrics.http_req_connecting.values.max : 0)}</td>
          </tr>
          <tr>
            <td><strong>http_req_waiting (TTFB)</strong></td>
            <td>Gateway Processing</td>
            <td>${formatMs(metrics.http_req_waiting ? metrics.http_req_waiting.values.avg : 0)}</td>
            <td>${formatMs(metrics.http_req_waiting ? metrics.http_req_waiting.values.min : 0)}</td>
            <td>${formatMs(metrics.http_req_waiting ? metrics.http_req_waiting.values.med : 0)}</td>
            <td>${formatMs(metrics.http_req_waiting ? metrics.http_req_waiting.values['p(90)'] : 0)}</td>
            <td>${formatMs(metrics.http_req_waiting ? metrics.http_req_waiting.values['p(95)'] : 0)}</td>
            <td>${formatMs(metrics.http_req_waiting ? metrics.http_req_waiting.values.max : 0)}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="footer">
      Generated automatically by FashionPin k6 Load Test Suite &bull; Tested via Spring Cloud API Gateway (:8080)
    </div>
  </div>
</body>
</html>`;
}

/**
 * Hook invoked by k6 at completion to write reports
 */
export function handleSummary(data) {
  return {
    'stdout': generateTextSummary(data),
    'reports/summary.html': generateHtmlReport(data),
    'reports/summary.json': JSON.stringify(data, null, 2),
  };
}
