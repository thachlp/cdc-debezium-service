#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
CONNECT_URL="${CONNECT_URL:-http://localhost:8083}"
CONNECTOR_NAME="${CONNECTOR_NAME:-source-connector}"
PURGE_DATA="${PURGE_DATA:-0}"

if command -v curl >/dev/null 2>&1; then
  echo "[1/2] Deleting connector (if exists): $CONNECTOR_NAME"
  curl -fsS -X DELETE "$CONNECT_URL/connectors/$CONNECTOR_NAME" >/dev/null || true
else
  echo "[WARN] curl not found; skip connector deletion"
fi

echo "[2/2] Stopping Docker services"
if [[ "$PURGE_DATA" == "1" ]]; then
  docker compose -f "$ROOT_DIR/docker-compose.yml" down -v
else
  docker compose -f "$ROOT_DIR/docker-compose.yml" down
fi

echo "[DONE] Demo services stopped"

