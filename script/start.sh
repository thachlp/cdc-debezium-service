#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
COMPOSE_FILE="$ROOT_DIR/docker-compose.yml"
CONNECT_URL="${CONNECT_URL:-http://localhost:8083}"
CONNECTOR_NAME="${CONNECTOR_NAME:-source-connector}"
CONNECTOR_CONFIG_FILE="${CONNECTOR_CONFIG_FILE:-$SCRIPT_DIR/source-connector-config.json}"

usage() {
  echo "Usage: bash script/start.sh"
  echo "Start infra + connector, verify connector RUNNING (connector + task), then run consumer logs."
  echo "Requires local MySQL ready with pre-created databases (e.g., coffee_shop, coffee_shop_v2)."
}

if [[ "${1:-}" == "--help" || "${1:-}" == "-h" ]]; then
  usage
  exit 0
fi

if [[ -n "${1:-}" ]]; then
  echo "[ERROR] Unknown option: $1"
  usage
  exit 1
fi

for cmd in docker curl; do
  if ! command -v "$cmd" >/dev/null 2>&1; then
    echo "[ERROR] Missing required command: $cmd"
    exit 1
  fi
done

if [[ ! -f "$CONNECTOR_CONFIG_FILE" ]]; then
  echo "[ERROR] Connector config not found: $CONNECTOR_CONFIG_FILE"
  exit 1
fi

request_connect() {
  local method="$1"
  local url="$2"
  local data_file="${3:-}"
  local response_file
  local status_code

  response_file="$(mktemp)"
  if [[ -n "$data_file" ]]; then
    status_code="$(curl -sS -o "$response_file" -w "%{http_code}" -X "$method" \
      -H "Accept: application/json" \
      -H "Content-Type: application/json" \
      "$url" \
      --data @"$data_file")"
  else
    status_code="$(curl -sS -o "$response_file" -w "%{http_code}" -X "$method" \
      -H "Accept: application/json" \
      "$url")"
  fi

  if (( status_code < 200 || status_code >= 300 )); then
    echo "[ERROR] Kafka Connect API failed: $method $url (HTTP $status_code)" >&2
    cat "$response_file" >&2
    rm -f "$response_file"
    return 1
  fi

  cat "$response_file"
  rm -f "$response_file"
}

if pgrep -f "com.thachlp.cdc.consumer.CdcConsumerApplication" >/dev/null 2>&1; then
  echo "[ERROR] A consumer process is already running."
  echo "Stop it first to avoid split consumption during demo:"
  echo "  pkill -f com.thachlp.cdc.consumer.CdcConsumerApplication"
  exit 1
fi

echo "[1/5] Starting Docker services"
docker compose -f "$COMPOSE_FILE" up -d

echo "[2/5] Waiting for Kafka Connect at $CONNECT_URL"
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

echo "[3/5] Waiting for Debezium MySQL connector plugin"
for i in {1..60}; do
  plugins_json="$(curl -fsS "$CONNECT_URL/connector-plugins" || true)"
  if echo "$plugins_json" | grep -q '"class":"io.debezium.connector.mysql.MySqlConnector"'; then
    break
  fi

  if [[ "$i" -eq 60 ]]; then
    echo "[ERROR] Debezium MySQL connector plugin is not ready"
    echo "$plugins_json"
    exit 1
  fi
  sleep 2
done

echo "[4/5] Creating/updating connector: $CONNECTOR_NAME"
if curl -fsS "$CONNECT_URL/connectors/$CONNECTOR_NAME" >/dev/null 2>&1; then
  request_connect PUT "$CONNECT_URL/connectors/$CONNECTOR_NAME/config" "$CONNECTOR_CONFIG_FILE" >/dev/null
else
  payload_file="$(mktemp)"
  trap 'rm -f "$payload_file"' EXIT
  {
    echo "{"
    echo "  \"name\": \"$CONNECTOR_NAME\","
    echo "  \"config\":"
    cat "$CONNECTOR_CONFIG_FILE"
    echo "}"
  } > "$payload_file"

  request_connect POST "$CONNECT_URL/connectors" "$payload_file" >/dev/null
fi

echo "[5/5] Checking connector status: $CONNECTOR_NAME"
STATUS_JSON=""
for i in {1..60}; do
  STATUS_JSON="$(curl -fsS "$CONNECT_URL/connectors/$CONNECTOR_NAME/status")"
  RUNNING_COUNT="$(echo "$STATUS_JSON" | grep -o '"state":"RUNNING"' | wc -l | tr -d ' ')"
  if [[ "$RUNNING_COUNT" -ge 2 ]]; then
    break
  fi

  if [[ "$i" -eq 60 ]]; then
    echo "$STATUS_JSON"
    echo "[ERROR] Connector is not fully RUNNING (connector/task)."
    exit 1
  fi
  sleep 2
done
echo "$STATUS_JSON"

echo "[START] Consumer logs (foreground)"
cd "$ROOT_DIR/cdc-debezium-consumer"
exec ./gradlew run











