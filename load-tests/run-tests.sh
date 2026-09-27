#!/usr/bin/env bash

# ==============================================================================
# FashionPin API Gateway Load Testing Script
# Powered by Grafana k6
# ==============================================================================

set -eo pipefail

# ANSI Color Codes
CYAN='\033[0;36m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Ensure standard Homebrew paths are in PATH
export PATH="/opt/homebrew/bin:/usr/local/bin:$PATH"

# Default values
TARGET_URL="${GATEWAY_URL:-http://localhost:8080}"
SCENARIO="${SCENARIO:-smoke}"
USE_DOCKER=false
CUSTOM_VUS=""
CUSTOM_DURATION=""

print_banner() {
  echo -e "${CYAN}======================================================================${NC}"
  echo -e "${CYAN}         FashionPin Microservices API Gateway Load Testing           ${NC}"
  echo -e "${CYAN}======================================================================${NC}"
}

usage() {
  echo "Usage: ./run-tests.sh [OPTIONS]"
  echo ""
  echo "Options:"
  echo "  -s, --scenario SCENARIO   Test scenario: smoke, load, stress, spike, soak (default: smoke)"
  echo "  -t, --target URL          API Gateway target URL (default: http://localhost:8080)"
  echo "  -u, --vus NUMBER          Override Virtual Users (e.g. 50)"
  echo "  -d, --duration DURATION   Override test duration (e.g. 2m, 30s)"
  echo "  --docker                  Execute tests inside Grafana k6 Docker container"
  echo "  -h, --help                Show this help message"
  echo ""
  echo "Examples:"
  echo "  ./run-tests.sh --scenario smoke"
  echo "  ./run-tests.sh --scenario load --target http://194.163.166.16:8080"
  echo "  ./run-tests.sh --scenario stress --vus 200 --duration 3m"
  echo "  ./run-tests.sh --docker --scenario smoke"
  echo ""
  exit 0
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
  case $1 in
    -s|--scenario)
      SCENARIO="$2"
      shift 2
      ;;
    -t|--target)
      TARGET_URL="$2"
      shift 2
      ;;
    -u|--vus)
      CUSTOM_VUS="$2"
      shift 2
      ;;
    -d|--duration)
      CUSTOM_DURATION="$2"
      shift 2
      ;;
    --docker)
      USE_DOCKER=true
      shift
      ;;
    -h|--help)
      usage
      ;;
    *)
      echo -e "${RED}Unknown option: $1${NC}"
      usage
      ;;
  esac
done

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

mkdir -p reports

print_banner
echo -e "Scenario:    ${YELLOW}${SCENARIO}${NC}"
echo -e "Target URL:  ${YELLOW}${TARGET_URL}${NC}"

EXTRA_ARGS=()
if [ -n "$CUSTOM_VUS" ]; then
  EXTRA_ARGS+=(--vus "$CUSTOM_VUS")
  echo -e "Custom VUs:  ${YELLOW}${CUSTOM_VUS}${NC}"
fi
if [ -n "$CUSTOM_DURATION" ]; then
  EXTRA_ARGS+=(--duration "$CUSTOM_DURATION")
  echo -e "Duration:    ${YELLOW}${CUSTOM_DURATION}${NC}"
fi

echo ""

if [ "$USE_DOCKER" = true ]; then
  echo -e "${CYAN}Running k6 inside Docker container...${NC}"
  docker run --rm -i \
    -v "$SCRIPT_DIR:/load-tests" \
    -w /load-tests \
    -e GATEWAY_URL="$TARGET_URL" \
    -e SCENARIO="$SCENARIO" \
    grafana/k6:latest run ${EXTRA_ARGS[@]+"${EXTRA_ARGS[@]}"} main.js
else
  # Locate k6 executable
  K6_BIN="$(command -v k6 || echo "")"
  if [ -z "$K6_BIN" ]; then
    if [ -x "/opt/homebrew/bin/k6" ]; then
      K6_BIN="/opt/homebrew/bin/k6"
    elif [ -x "/usr/local/bin/k6" ]; then
      K6_BIN="/usr/local/bin/k6"
    fi
  fi

  if [ -z "$K6_BIN" ]; then
    echo -e "${RED}Error: k6 is not installed on your host system.${NC}"
    echo -e "Install k6 via Homebrew: ${YELLOW}brew install k6${NC}"
    echo -e "Or run this script with ${YELLOW}--docker${NC} to use containerized k6:"
    echo -e "  ${GREEN}./run-tests.sh --docker --scenario ${SCENARIO} --target ${TARGET_URL}${NC}"
    exit 1
  fi

  echo -e "${CYAN}Executing k6 test suite using ${K6_BIN}...${NC}"
  "$K6_BIN" run \
    -e GATEWAY_URL="$TARGET_URL" \
    -e SCENARIO="$SCENARIO" \
    ${EXTRA_ARGS[@]+"${EXTRA_ARGS[@]}"} \
    main.js
fi

echo ""
echo -e "${GREEN}Load test run completed!${NC}"
if [ -f "reports/summary.html" ]; then
  echo -e "Interactive HTML Report: ${YELLOW}file://$SCRIPT_DIR/reports/summary.html${NC}"
fi
if [ -f "reports/summary.json" ]; then
  echo -e "Raw JSON Metrics:       ${YELLOW}$SCRIPT_DIR/reports/summary.json${NC}"
fi
