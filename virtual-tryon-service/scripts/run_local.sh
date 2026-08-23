#!/usr/bin/env bash
set -e

echo "Starting Fashion Pin Virtual Try-On AI Service locally..."
export SERVICE_PORT=${SERVICE_PORT:-8092}
export DEBUG=true

if [ -d "venv" ]; then
    source venv/bin/activate
fi

python3 -m app.main
