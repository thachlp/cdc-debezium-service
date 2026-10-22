#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
CONNECT_URL="${CONNECT_URL:-http://localhost:8083}"
CONNECTOR_NAME="${CONNECTOR_NAME:-source-connector}"
CONNECTOR_CONFIG_FILE="${CONNECTOR_CONFIG_FILE:-$SCRIPT_DIR/source-connector-config.json}"
INIT_SCHEMA_FILE="${INIT_SCHEMA_FILE:-$SCRIPT_DIR/init-schema.sql}"

for cmd in docker curl; do
  if ! command -v "$cmd" >/dev/null 2>&1; then
    echo "[ERROR] Missing required command: $cmd"
    exit 1
  fi
done

if [[ ! -f "$CONNECTOR_CONFIG_FILE" ]]; then
  echo "[ERROR] Connector config file not found: $CONNECTOR_CONFIG_FILE"
  exit 1
fi

if [[ ! -f "$INIT_SCHEMA_FILE" ]]; then
  echo "[ERROR] Schema file not found: $INIT_SCHEMA_FILE"
  exit 1
fi

echo "[1/5] Starting Docker services..."
docker compose -f "$ROOT_DIR/docker-compose.yml" up -d

echo "[2/5] Waiting for Kafka Connect at $CONNECT_URL ..."
for i in {1..60}; do
  if curl -fsS "$CONNECT_URL/connectors" >/dev/null 2>&1; then
    break
  fi
  if [[ "$i" -eq 60 ]]; then
    echo "[ERROR] Kafka Connect is not ready after 60 attempts"
    exit 1
  fi
  sleep 2
done

echo "[3/5] Initializing MySQL schema..."
docker compose -f "$ROOT_DIR/docker-compose.yml" exec -T mysql mysql -uroot -p123456 < "$INIT_SCHEMA_FILE"

echo "[4/5] Creating/updating connector: $CONNECTOR_NAME"
if curl -fsS "$CONNECT_URL/connectors/$CONNECTOR_NAME" >/dev/null 2>&1; then
  curl -fsS -X PUT \
    -H "Accept: application/json" \
    -H "Content-Type: application/json" \
    "$CONNECT_URL/connectors/$CONNECTOR_NAME/config" \
    --data @"$CONNECTOR_CONFIG_FILE" >/dev/null
  echo "[INFO] Connector updated"
else
  TMP_PAYLOAD="$(mktemp)"
  trap 'rm -f "$TMP_PAYLOAD"' EXIT
  {
    echo "{"
    echo "  \"name\": \"$CONNECTOR_NAME\","
    echo "  \"config\":"
    cat "$CONNECTOR_CONFIG_FILE"
    echo "}"
  } > "$TMP_PAYLOAD"

  curl -fsS -X POST \
    -H "Accept: application/json" \
    -H "Content-Type: application/json" \
    "$CONNECT_URL/connectors" \
    --data @"$TMP_PAYLOAD" >/dev/null
  echo "[INFO] Connector created"
fi

echo "[5/5] Connector status"
curl -fsS "$CONNECT_URL/connectors/$CONNECTOR_NAME/status"
echo

echo "[DONE] Demo infra is ready. Start consumer with:"
echo "  cd cdc-debezium-consumer && ./gradlew run"

