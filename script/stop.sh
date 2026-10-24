#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
COMPOSE_FILE="$ROOT_DIR/docker-compose.yml"
GRADLE_RUN_PATTERN="$ROOT_DIR/cdc-debezium-consumer/gradle/wrapper/gradle-wrapper.jar org.gradle.wrapper.GradleWrapperMain run"

stop_processes_by_pattern() {
  local pattern="$1"
  local description="$2"
  local pids

  pids="$(pgrep -f "$pattern" || true)"
  if [[ -z "$pids" ]]; then
    echo "[INFO] $description is not running"
    return
  fi

  echo "[INFO] Stopping $description (PIDs: $pids)"
  echo "$pids" | xargs kill || true
  sleep 1

  pids="$(pgrep -f "$pattern" || true)"
  if [[ -n "$pids" ]]; then
    echo "[WARN] Force stopping $description (PIDs: $pids)"
    echo "$pids" | xargs kill -9 || true
  fi
}

echo "[1/2] Stopping consumer process"
stop_processes_by_pattern "com.thachlp.cdc.consumer.CdcConsumerApplication" "CdcConsumerApplication"
stop_processes_by_pattern "$GRADLE_RUN_PATTERN" "Gradle wrapper run"

echo "[2/2] Stopping services"
docker compose -f "$COMPOSE_FILE" down
echo "[DONE] Clean up completed"

