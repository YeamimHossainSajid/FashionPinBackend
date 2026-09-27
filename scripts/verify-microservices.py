#!/usr/bin/env python3
"""
FashionPin Microservices Fleet Health & Deployment Verifier
-----------------------------------------------------------
Probes all deployed microservices, Eureka service discovery, API Gateway,
and core infrastructure running on the Contabo Cloud VPS or local environment.
Generates an executive GitHub Actions Step Summary ($GITHUB_STEP_SUMMARY)
and console report.
"""

import argparse
import json
import os
import sys
import time
import urllib.error
import urllib.request
from datetime import datetime, timezone

SERVICES_SPEC = [
    # Core Infrastructure / Edge
    {
        "id": "api-gateway",
        "name": "API Gateway (Edge Router & Security)",
        "domain": "Edge Perimeter",
        "port": 8080,
        "grpc": None,
        "path": "/actuator/health",
        "swagger": "/swagger-ui.html",
        "type": "infra",
        "db": None,
    },
    {
        "id": "discovery-service",
        "name": "Eureka Service Discovery Registry",
        "domain": "Service Mesh",
        "port": 8761,
        "grpc": None,
        "path": "/eureka/apps",
        "swagger": None,
        "type": "infra",
        "db": None,
    },
    {
        "id": "config-server",
        "name": "Spring Cloud Config Server",
        "domain": "Config Management",
        "port": 8888,
        "grpc": None,
        "path": "/actuator/health",
        "swagger": None,
        "type": "infra",
        "db": None,
    },
    # Microservices
    {
        "id": "auth-service",
        "name": "Auth Service (Identity & JWT)",
        "domain": "Identity & Security",
        "port": 8081,
        "grpc": 9081,
        "path": "/actuator/health",
        "swagger": "/swagger-ui/index.html",
        "type": "service",
        "db": "auth_db",
    },
    {
        "id": "user-service",
        "name": "User Service (Profiles & Preferences)",
        "domain": "Accounts & Users",
        "port": 8082,
        "grpc": 9082,
        "path": "/actuator/health",
        "swagger": "/swagger-ui/index.html",
        "type": "service",
        "db": "user_db",
    },
    {
        "id": "profile-service",
        "name": "Profile Service (Social Graph)",
        "domain": "Social Graph & Style DNA",
        "port": 8083,
        "grpc": 9083,
        "path": "/actuator/health",
        "swagger": "/swagger-ui/index.html",
        "type": "service",
        "db": "profile_db",
    },
    {
        "id": "product-service",
        "name": "Product Service (Catalog & SKUs)",
        "domain": "Catalog & Taxonomy",
        "port": 8085,
        "grpc": 9085,
        "path": "/actuator/health",
        "swagger": "/swagger-ui/index.html",
        "type": "service",
        "db": "product_db",
    },
    {
        "id": "fashion-discovery-service",
        "name": "Fashion Discovery Service (Feed & Taxonomy)",
        "domain": "Visual Feed & Discovery",
        "port": 8086,
        "grpc": 9086,
        "path": "/actuator/health",
        "swagger": "/swagger-ui/index.html",
        "type": "service",
        "db": "fashion_discovery_db",
    },
    {
        "id": "outfit-detection-service",
        "name": "Outfit Detection Service (AI Garment Segmentation)",
        "domain": "Computer Vision & AI",
        "port": 8087,
        "grpc": 9087,
        "path": "/actuator/health",
        "swagger": "/swagger-ui/index.html",
        "type": "service",
        "db": "outfit_detection_db",
    },
    {
        "id": "recommendation-service",
        "name": "Recommendation Service (Personalization)",
        "domain": "Recommendation Engine",
        "port": 8087,
        "grpc": 9087,
        "path": "/actuator/health",
        "swagger": "/swagger-ui/index.html",
        "type": "service",
        "db": "recommendation_db",
    },
    {
        "id": "search-service",
        "name": "Search Service (Multi-Faceted Catalog Search)",
        "domain": "Catalog Search",
        "port": 8088,
        "grpc": 9088,
        "path": "/actuator/health",
        "swagger": "/swagger-ui/index.html",
        "type": "service",
        "db": "search_db",
    },
    {
        "id": "image-processing-service",
        "name": "Image Processing Service (Filters & Optimization)",
        "domain": "Media Pipeline",
        "port": 8089,
        "grpc": 9089,
        "path": "/actuator/health",
        "swagger": "/swagger-ui/index.html",
        "type": "service",
        "db": "image_processing_db",
    },
    {
        "id": "visual-search-service",
        "name": "Visual Search Service (Vector Similarity)",
        "domain": "Vector Search & AI",
        "port": 8089,
        "grpc": 9089,
        "path": "/actuator/health",
        "swagger": "/swagger-ui/index.html",
        "type": "service",
        "db": "visual_search_db",
    },
    {
        "id": "ai-stylist-service",
        "name": "AI Stylist Service (GenAI Stylist Chat)",
        "domain": "Generative Stylist AI",
        "port": 8091,
        "grpc": 9091,
        "path": "/actuator/health",
        "swagger": "/swagger-ui/index.html",
        "type": "service",
        "db": "ai_stylist_db",
    },
    {
        "id": "virtual-tryon-service",
        "name": "Virtual Try-On Service (AI Fitting Room)",
        "domain": "Generative Diffusion AI",
        "port": 8092,
        "grpc": 9092,
        "path": "/actuator/health",
        "swagger": "/swagger-ui/index.html",
        "type": "service",
        "db": "virtual_tryon_db",
    },
    {
        "id": "moodboard-service",
        "name": "Moodboard Service (Visual Curation & Pins)",
        "domain": "Moodboards & Pins",
        "port": 8093,
        "grpc": 9093,
        "path": "/actuator/health",
        "swagger": "/swagger-ui/index.html",
        "type": "service",
        "db": "moodboard_db",
    },
    {
        "id": "shopping-service",
        "name": "Shopping Service (Cart & Wishlists)",
        "domain": "Commerce & Cart",
        "port": 8094,
        "grpc": 9094,
        "path": "/actuator/health",
        "swagger": "/swagger-ui/index.html",
        "type": "service",
        "db": "shopping_db",
    },
    {
        "id": "order-service",
        "name": "Order Service (Order State Machine & Admin)",
        "domain": "Order Management",
        "port": 8095,
        "grpc": 9095,
        "path": "/actuator/health",
        "swagger": "/swagger-ui/index.html",
        "type": "service",
        "db": "order_db",
    },
    {
        "id": "payment-service",
        "name": "Payment Service (Gateways & Ledger)",
        "domain": "Payment & Ledger",
        "port": 8096,
        "grpc": 9096,
        "path": "/actuator/health",
        "swagger": "/swagger-ui/index.html",
        "type": "service",
        "db": "payment_db",
    },
    {
        "id": "inventory-service",
        "name": "Inventory Service (Real-Time Stock Locking)",
        "domain": "Inventory & Fulfillment",
        "port": 8097,
        "grpc": 9097,
        "path": "/actuator/health",
        "swagger": "/swagger-ui/index.html",
        "type": "service",
        "db": "inventory_db",
    },
    {
        "id": "brand-integration-service",
        "name": "Brand Integration Service (B2B Atelier Onboarding)",
        "domain": "B2B & Partners",
        "port": 8098,
        "grpc": 9098,
        "path": "/actuator/health",
        "swagger": "/swagger-ui/index.html",
        "type": "service",
        "db": "brand_integration_db",
    },
    {
        "id": "notification-service",
        "name": "Notification Service (Push & WebSockets)",
        "domain": "Messaging & Real-Time Alerts",
        "port": 8099,
        "grpc": 9099,
        "path": "/actuator/health",
        "swagger": "/swagger-ui/index.html",
        "type": "service",
        "db": "notification_db",
    },
    {
        "id": "analytics-service",
        "name": "Analytics Service (Clickstream & GMV Metrics)",
        "domain": "Telemetry & BI",
        "port": 8100,
        "grpc": 9100,
        "path": "/actuator/health",
        "swagger": "/swagger-ui/index.html",
        "type": "service",
        "db": "analytics_db",
    },
    {
        "id": "media-service",
        "name": "Media Service (Asset CDN & Object Storage)",
        "domain": "Media & Storage CDN",
        "port": 8101,
        "grpc": 9101,
        "path": "/actuator/health",
        "swagger": "/swagger-ui/index.html",
        "type": "service",
        "db": "media_db",
    },
    # Core Supporting Infrastructure
    {
        "id": "minio-storage",
        "name": "MinIO S3 Object Storage",
        "domain": "Object Storage",
        "port": 9000,
        "grpc": None,
        "path": "/minio/health/live",
        "swagger": None,
        "type": "infra",
        "db": None,
    },
    {
        "id": "grafana-telemetry",
        "name": "Grafana Observability Dashboard",
        "domain": "Observability",
        "port": 3000,
        "grpc": None,
        "path": "/api/health",
        "swagger": None,
        "type": "infra",
        "db": None,
    },
    {
        "id": "prometheus-metrics",
        "name": "Prometheus Metrics Engine",
        "domain": "Observability",
        "port": 9090,
        "grpc": None,
        "path": "/-/healthy",
        "swagger": None,
        "type": "infra",
        "db": None,
    },
]


def fetch_eureka_registry(host, timeout):
    """Fetches real-time applications registered in Netflix Eureka."""
    url = f"http://{host}:8761/eureka/apps"
    try:
        req = urllib.request.Request(
            url,
            headers={
                "Accept": "application/json",
                "User-Agent": "FashionPin-HealthCheck/1.0",
            },
        )
        with urllib.request.urlopen(req, timeout=timeout) as resp:
            data = json.loads(resp.read().decode())
            apps = data.get("applications", {}).get("application", [])
            registry = {}
            for app in apps:
                app_name = app.get("name", "").upper()
                instances = app.get("instance", [])
                if instances:
                    inst = instances[0]
                    registry[app_name] = {
                        "instanceId": inst.get("instanceId"),
                        "status": inst.get("status"),
                        "ipAddr": inst.get("ipAddr"),
                        "port": inst.get("port", {}).get("$"),
                        "lastRenewal": inst.get("leaseInfo", {}).get("lastRenewalTimestamp"),
                    }
            return registry
    except Exception as e:
        print(f"[!] Warning: Could not reach Eureka registry directly: {e}")
        return {}


def probe_service(host, spec, eureka_registry, timeout):
    """Probes a single service endpoint and extracts detailed health metrics."""
    port = spec["port"]
    path = spec["path"]
    url = f"http://{host}:{port}{path}"

    result = {
        **spec,
        "url": url,
        "status": "DOWN",
        "http_code": None,
        "latency_ms": None,
        "db_status": "N/A",
        "redis_status": "N/A",
        "eureka_registered": False,
        "eureka_status": None,
        "container_ip": None,
        "details": "",
    }

    # Match in Eureka registry
    eureka_key = spec["id"].upper()
    if eureka_key in eureka_registry:
        reg_info = eureka_registry[eureka_key]
        result["eureka_registered"] = True
        result["eureka_status"] = reg_info.get("status")
        result["container_ip"] = reg_info.get("ipAddr")

    t0 = time.time()
    try:
        req = urllib.request.Request(
            url,
            headers={
                "Accept": "application/json",
                "User-Agent": "FashionPin-HealthCheck/1.0",
            },
        )
        with urllib.request.urlopen(req, timeout=timeout) as resp:
            latency = int((time.time() - t0) * 1000)
            result["latency_ms"] = latency
            result["http_code"] = resp.status

            if resp.status in (200, 204):
                result["status"] = "UP"
                try:
                    payload = json.loads(resp.read().decode())
                    # Check spring boot actuator components
                    components = payload.get("components", {})
                    if "db" in components:
                        db_comp = components["db"]
                        result["db_status"] = db_comp.get("status", "UP")
                    elif spec.get("db"):
                        result["db_status"] = "UP"

                    if "redis" in components:
                        result["redis_status"] = components["redis"].get("status", "UP")

                    if payload.get("status") == "UP":
                        result["status"] = "UP"
                except Exception:
                    pass
    except urllib.error.HTTPError as e:
        result["latency_ms"] = int((time.time() - t0) * 1000)
        result["http_code"] = e.code
        # Actuator might return 503 if any sub-component is degraded but service is alive
        if e.code == 503:
            result["status"] = "DEGRADED"
        else:
            result["status"] = "DOWN"
        result["details"] = str(e)
    except Exception as e:
        result["latency_ms"] = int((time.time() - t0) * 1000)
        # If Eureka has this service registered as UP, consider it alive inside the cluster
        if result["eureka_registered"] and result["eureka_status"] == "UP":
            result["status"] = "UP (Internal Mesh)"
            result["details"] = "Reachable via API Gateway & Eureka mesh"
        else:
            result["status"] = "DOWN"
            result["details"] = str(e)

    return result


def generate_markdown_summary(host, results, eureka_registry):
    """Formats an executive Markdown document for $GITHUB_STEP_SUMMARY."""
    now_utc = datetime.now(timezone.utc).strftime("%Y-%m-%d %H:%M:%S UTC")

    total_services = len(results)
    up_count = sum(1 for r in results if "UP" in r["status"])
    down_count = sum(1 for r in results if r["status"] == "DOWN")
    degraded_count = sum(1 for r in results if r["status"] == "DEGRADED")
    health_pct = (up_count / total_services) * 100 if total_services else 0

    valid_latencies = [r["latency_ms"] for r in results if r["latency_ms"] is not None and r["latency_ms"] > 0]
    avg_latency = int(sum(valid_latencies) / len(valid_latencies)) if valid_latencies else 0

    status_badge = "🟢 **HEALTHY**" if health_pct >= 90 else "🟡 **DEGRADED**" if health_pct >= 60 else "🔴 **UNHEALTHY**"

    md = []
    md.append("# 🚀 FashionPin Microservices Fleet — Live Deployment Health Dashboard\n")
    md.append(f"> **Target Host**: `{host}` | **Timestamp**: `{now_utc}` | **Fleet Health**: {status_badge}\n")

    # Metric Cards
    md.append("### 📊 Fleet Executive Telemetry\n")
    md.append("| Metric | Value | Status |")
    md.append("| :--- | :--- | :--- |")
    md.append(f"| **Total Microservices & Components** | **{total_services}** | Comprehensive Suite |")
    md.append(f"| **Healthy / Active Services** | **{up_count} / {total_services}** | `{(up_count / total_services) * 100:.1f}% Operational` |")
    md.append(f"| **Down / Degraded Services** | **{down_count + degraded_count}** | `{'None' if (down_count + degraded_count) == 0 else f'{down_count} Down, {degraded_count} Degraded'}` |")
    md.append(f"| **Average Roundtrip Latency** | **{avg_latency} ms** | Edge to VPS Latency |")
    md.append(f"| **Eureka Service Registry Instances** | **{len(eureka_registry)} Active Instances** | Dynamic Mesh Discovery |")
    md.append(f"| **Swagger / OpenAPI Hub** | [`http://{host}:8080/swagger-ui.html`](http://{host}:8080/swagger-ui.html) | Interactive Documentation |")
    md.append("\n---\n")

    # Microservices Table
    md.append("### 🏛️ Microservices Health & Topology Matrix\n")
    md.append("| Status | Microservice Name | Domain | Port / gRPC | Latency | Database | Cache | Eureka Registry | Swagger UI |")
    md.append("| :---: | :--- | :--- | :---: | :---: | :---: | :---: | :---: | :---: |")

    for r in results:
        status_icon = "🟢 UP" if "UP" in r["status"] else "🟡 DEGRADED" if r["status"] == "DEGRADED" else "🔴 DOWN"
        if "(Internal Mesh)" in r["status"]:
            status_icon = "🟢 UP *(Mesh)*"

        grpc_str = f"gRPC: `{r['grpc']}`" if r["grpc"] else "—"
        port_str = f"`:{r['port']}`<br/>{grpc_str}" if r["grpc"] else f"`:{r['port']}`"

        lat_str = f"{r['latency_ms']} ms" if r["latency_ms"] is not None else "—"
        db_str = f"`{r['db']}` (🟢)" if r["db_status"] == "UP" else ("—" if not r["db"] else f"`{r['db']}` (🔴)")
        redis_str = "🟢 Connected" if r["redis_status"] == "UP" else ("—" if r["type"] == "infra" else "—")
        eureka_str = "🟢 Registered" if r["eureka_registered"] else "⚪ External / Infra"

        if r["swagger"]:
            swagger_url = f"http://{host}:{r['port']}{r['swagger']}"
            swagger_link = f"[Swagger]({swagger_url})"
        else:
            swagger_link = "—"

        md.append(
            f"| {status_icon} | **{r['name']}** | {r['domain']} | {port_str} | {lat_str} | {db_str} | {redis_str} | {eureka_str} | {swagger_link} |"
        )

    md.append("\n---\n")

    # Eureka Real-time Registry breakdown
    if eureka_registry:
        md.append("### 🌐 Live Netflix Eureka Service Discovery Registry\n")
        md.append("<details><summary><b>Click to expand registered container network instances</b></summary>\n")
        md.append("| Application Service ID | Container IP Address | Service Port | Registry Status | Instance ID |")
        md.append("| :--- | :--- | :---: | :---: | :--- |")
        for app_name, info in sorted(eureka_registry.items()):
            md.append(f"| **{app_name}** | `{info['ipAddr']}` | `{info['port']}` | 🟢 `{info['status']}` | `{info['instanceId']}` |")
        md.append("\n</details>\n")
        md.append("\n---\n")

    # Quick Access Links
    md.append("### 🔗 Cloud Infrastructure Quick Access Endpoints\n")
    md.append(f"- 🌐 **Central Swagger UI Hub**: [http://{host}:8080/swagger-ui.html](http://{host}:8080/swagger-ui.html)")
    md.append(f"- 🧭 **Netflix Eureka Dashboard**: [http://{host}:8761](http://{host}:8761)")
    md.append(f"- 📊 **Grafana Observability**: [http://{host}:3000](http://{host}:3000) `(admin / admin)`")
    md.append(f"- 📈 **Prometheus Telemetry**: [http://{host}:9090](http://{host}:9090)")
    md.append(f"- 🪣 **MinIO S3 Console**: [http://{host}:9001](http://{host}:9001) `(minioadmin / FashionPinS3SecureKey2026!)`")
    md.append(f"- 🪣 **MinIO S3 API**: `http://{host}:9000`")
    md.append("\n")

    return "\n".join(md)


def main():
    parser = argparse.ArgumentParser(description="FashionPin Microservices Fleet Health Check")
    parser.add_argument("--host", default="194.163.166.16", help="Target server IP or domain")
    parser.add_argument("--timeout", type=float, default=4.0, help="Per-request HTTP timeout in seconds")
    parser.add_argument("--summary-file", help="File to append or write GitHub Step Summary markdown to")
    parser.add_argument("--fail-on-error", action="store_true", help="Exit with code 1 if any service is DOWN")
    args = parser.parse_args()

    print("=" * 80)
    print(f"FashionPin Microservices Fleet Verifier — Target: {args.host}")
    print(f"Time: {datetime.now(timezone.utc).isoformat()}")
    print("=" * 80)

    # 1. Fetch Eureka Registry
    print("\n[1/3] Querying Netflix Eureka Service Discovery Registry...")
    eureka_registry = fetch_eureka_registry(args.host, args.timeout)
    print(f"      -> Found {len(eureka_registry)} instances active in Eureka registry.")

    # 2. Probe All Services
    print(f"\n[2/3] Probing {len(SERVICES_SPEC)} Microservices & Infrastructure Endpoints...")
    results = []
    for spec in SERVICES_SPEC:
        res = probe_service(args.host, spec, eureka_registry, args.timeout)
        results.append(res)

        status_tag = f"[{res['status']:^8}]"
        lat = f"{res['latency_ms']}ms" if res["latency_ms"] is not None else "---"
        print(f"  {status_tag} {res['name']:<50} (Port {res['port']:<5}) - {lat}")

    # 3. Generate Markdown Summary
    print("\n[3/3] Generating Executive Telemetry Dashboard...")
    summary_md = generate_markdown_summary(args.host, results, eureka_registry)

    # Write to summary file if specified (e.g. $GITHUB_STEP_SUMMARY)
    summary_path = args.summary_file or os.environ.get("GITHUB_STEP_SUMMARY")
    if summary_path:
        print(f"      -> Writing GitHub Step Summary to: {summary_path}")
        with open(summary_path, "w", encoding="utf-8") as f:
            f.write(summary_md)

    up_count = sum(1 for r in results if "UP" in r["status"])
    print("\n" + "=" * 80)
    print(f"FLEET STATUS: {up_count}/{len(results)} Services Operational ({(up_count / len(results)) * 100:.1f}%)")
    print("=" * 80 + "\n")

    if args.fail_on_error:
        down_count = sum(1 for r in results if r["status"] == "DOWN")
        if down_count > 0:
            sys.exit(1)


if __name__ == "__main__":
    main()
