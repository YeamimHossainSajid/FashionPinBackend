#!/usr/bin/env bash
set -euo pipefail
if command -v mvn >/dev/null 2>&1; then
  exec mvn "$@"
fi
echo "Maven is required. Install Maven 3.9+ or add the Maven Wrapper jar under .mvn/wrapper." >&2
exit 1
