# FashionPin — API Gateway k6 Load Testing Suite

High-performance, scenario-driven distributed load testing suite powered by **Grafana k6** for the **FashionPin Microservices Ecosystem**.

All traffic intentionally routes through the **Spring Cloud API Gateway (`:8080`)**, exercising Gateway edge filters, distributed JWT parsing and header injection (`X-User-Id`, `X-User-Roles`), Netflix Eureka service discovery routing, rate limiters, and downstream microservice databases.

---

## 🏛️ Architecture & Traffic Flow

```
                           ┌────────────────────────────────────────┐
                           │      Grafana k6 Load Test Engine       │
                           │    (Virtual Users: 1 -> 500 VUs)       │
                           └──────────────────┬─────────────────────┘
                                              │ HTTP/1.1 (JSON/REST)
                                              ▼
                           ┌────────────────────────────────────────┐
                           │   Spring Cloud API Gateway (:8080)     │
                           │   • W3C Distributed Trace Injection    │
                           │   • JWT Claim Parsing & Routing        │
                           │   • Dynamic Eureka Load Balancing      │
                           └──────┬───────┬───────┬─────────┬───────┘
                                  │       │       │         │
                 ┌────────────────┘       │       │         └────────────────┐
                 ▼                        ▼       ▼                          ▼
       ┌───────────────────┐    ┌───────────────────┐    ┌──────────────────────┐    ┌──────────────────┐
       │   auth-service    │    │  product-service  │    │fashion-discovery-svc │    │ shopping-service │
       │      (:8081)      │    │      (:8085)      │    │       (:8084)        │    │     (:8083)      │
       └───────────────────┘    └───────────────────┘    └──────────────────────┘    └──────────────────┘
```

---

## 📁 Directory Structure

```
load-tests/
├── README.md                      # Comprehensive guide and documentation
├── docker-compose.k6.yml          # Containerized k6 runner for Docker networks
├── run-tests.sh                   # Friendly executable runner script
├── main.js                        # Unified entrypoint with dynamic scenario selection
├── config/
│   ├── env.js                     # Target URLs, think times, timeouts, default headers
│   └── thresholds.js              # Performance SLA thresholds (p95, p99, error rates)
├── helpers/
│   ├── auth.js                    # Registration, JWT login, token caching per VU
│   ├── data.js                    # Synthetic test data (categories, styles, keywords)
│   ├── http.js                    # Custom k6 metrics and W3C traceparent headers
│   └── report.js                  # Standalone interactive HTML report generator
├── scenarios/
│   ├── smoke.js                   # Minimal verification test (2 VUs, 30s)
│   ├── load.js                    # Normal-to-peak production load (up to 100 VUs)
│   ├── stress.js                  # Breaking point discovery (up to 500 VUs)
│   ├── spike.js                   # Instant flash-sale surge (350 VUs)
│   ├── soak.js                    # Extended endurance test for memory/connection leaks
│   └── gateway-e2e-journey.js     # Realistic mixed shopper & browser behavior
└── reports/                       # Auto-generated HTML & JSON summaries
    ├── summary.html
    └── summary.json
```

---

## ⚡ Quick Start

### 1. Prerequisites (Choose one)

#### Option A: Native k6 CLI (Fastest)
- **macOS (Homebrew)**:
  ```bash
  brew install k6
  ```
- **Linux (Debian/Ubuntu)**:
  ```bash
  sudo gpg -k
  sudo gpg --no-default-keyring --keyring /usr/share/keyrings/k6-archive-keyring.gpg --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys C5AD17C747E3415A3642D57D77C6C491D34EE73D
  echo "deb [signed-by=/usr/share/keyrings/k6-archive-keyring.gpg] https://dl.k6.io/deb stable main" | sudo tee /etc/apt/sources.list.d/k6.list
  sudo apt-get update && sudo apt-get install k6
  ```
- **Windows (Chocolatey / Scoop)**:
  ```powershell
  choco install k6
  # or
  scoop install k6
  ```

#### Option B: Docker (Zero Installation)
If Docker is installed, you don't need `k6` on your host. You can use the `--docker` flag in `./run-tests.sh` or run the official `grafana/k6` container.

---

## 🚀 Running the Tests

### Using the Runner Script (`./run-tests.sh`)

The easiest way to execute any test scenario:

```bash
# 1. Run quick smoke test against local API Gateway (http://localhost:8080)
./run-tests.sh --scenario smoke

# 2. Instant 400 Concurrent Users Burst Test (hitting at the exact same moment)
k6 run quick-400-vus.js
# Or target local: k6 run -e GATEWAY_URL=http://localhost:8080 quick-400-vus.js

# 3. Run standard production load test
./run-tests.sh --scenario load

# 4. Run load test against Remote VPS deployment
./run-tests.sh --scenario load --target http://<YOUR_SERVER_IP>:8080

# 5. 400 Sustained Concurrent Users for 30 Seconds against live server
./run-tests.sh --scenario load --vus 400 --duration 30s --target http://<YOUR_SERVER_IP>:8080

# 6. Run stress test with custom virtual users and duration
./run-tests.sh --scenario stress --vus 250 --duration 5m

# 7. Run test inside Docker container
./run-tests.sh --docker --scenario smoke --target http://host.docker.internal:8080
```

---

### Using Native `k6` CLI directly

You can run individual scenario files or use the unified `main.js`:

```bash
# Smoke Test
k6 run main.js

# Target Remote Production VPS
k6 run -e GATEWAY_URL=http://<YOUR_SERVER_IP>:8080 -e SCENARIO=load main.js

# Stress Test
k6 run -e SCENARIO=stress main.js

# Spike Test (Flash Sale)
k6 run -e SCENARIO=spike main.js

# Soak / Endurance Test
k6 run -e SCENARIO=soak main.js

# Directly run individual scenario files:
k6 run scenarios/smoke.js
k6 run scenarios/load.js
k6 run scenarios/stress.js
k6 run scenarios/spike.js
k6 run scenarios/soak.js
```

---

### Running via Docker Compose inside the backend network

To test the backend when running inside the Docker network `fashionpin-net`:

```bash
docker compose -f docker-compose.k6.yml run --rm \
  -e GATEWAY_URL=http://api-gateway:8080 \
  -e SCENARIO=smoke \
  k6
```

---

## 🎯 Test Scenarios Overview

| Scenario | Target VUs | Duration | Purpose |
| :--- | :---: | :---: | :--- |
| **`smoke`** | 2 | 30s | Quick sanity check to verify API Gateway routes, Eureka lookup, and DB connectivity. |
| **`load`** | 20 ➔ 100 | ~10m | Simulates expected peak daily traffic with realistic gradual ramp-up and cool-down. |
| **`stress`** | 50 ➔ 500 | ~13m | Finds breaking points, tests rate-limiting, DB pool saturation (`HikariCP`), and graceful recovery. |
| **`spike`** | 10 ➔ 350 | ~4m | Simulates sudden traffic spikes (celebrity post, flash sale drop, push notification). |
| **`soak`** | 30 | 30m+ | Endurance test detecting memory leaks, thread starvation, and open database connections. |

---

## 🌐 Endpoints Exercised Through API Gateway

Each virtual user executes a realistic, weighted user journey:

1. **Edge & Health Perimeter**:
   - `GET /actuator/health`: Validates gateway status and internal health indicators.
2. **Catalog & Taxonomy (`product-service`)**:
   - `GET /api/v1/categories`: Dynamic fashion categories and taxonomy.
   - `GET /api/v1/product?category={cat}&page=0&size=20`: Paginated product catalog.
3. **Multi-Faceted Search (`search-service`)**:
   - `GET /api/search/products?q={keyword}&pageSize=15`: Luxury apparel query search.
4. **Fashion Discovery Feed (`fashion-discovery-service`)**:
   - `GET /api/fashion/discover?page=0&size=10`: Visual inspiration feed.
5. **Guest Shopping (`shopping-service`)**:
   - `GET /api/v1/cart` with header `X-Guest-Session-Token`: Anonymous cart management.
6. **Identity & Authentication (`auth-service`)**:
   - `POST /api/v1/auth/register`: Synthetic user registration.
   - `POST /api/v1/auth/login`: JWT Bearer authentication.
7. **Authenticated Shopper (`profile-service` & `shopping-service`)**:
   - `GET /api/v1/profile/me`: Validates Gateway JWT parsing and `X-User-Id` propagation.
   - `GET /api/v1/cart` with `Authorization: Bearer <token>`: Authenticated shopping bag.

---

## 📊 Performance Thresholds & SLAs

The suite enforces strict automated SLAs in `config/thresholds.js`:

```javascript
// Example Production Load SLAs
http_req_failed: ['rate<0.02'],               // Error rate must stay below 2%
http_req_duration: ['p(95)<600', 'p(99)<1200'],// 95% of requests < 600ms, 99% < 1.2s
'http_req_duration{endpoint:gateway_health}': ['p(95)<150'],
'http_req_duration{endpoint:catalog_browse}': ['p(95)<500'],
'http_req_duration{endpoint:search}':         ['p(95)<700'],
'http_req_duration{endpoint:auth_login}':     ['p(95)<600']
```

---

## 📈 Interactive HTML Test Reports

At the end of every test execution, k6 generates:
1. **Interactive HTML Dashboard**: `load-tests/reports/summary.html`
   - Overall SLA Pass/Fail status
   - Total requests, throughput (req/s), and error percentage
   - P95, P99, average, and min/max latency cards
   - Detailed metric table breakdown (TTFB, TCP handshake, Gateway processing)
2. **Raw JSON Metrics**: `load-tests/reports/summary.json`
   - Complete raw time-series data for CI/CD pipelines, Grafana, or archival.

To view the report in your browser:
- On macOS: `open reports/summary.html`
- On Linux: `xdg-open reports/summary.html`
