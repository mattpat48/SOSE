#!/usr/bin/env bash
# Run all services: backend-eaas, backend-daas, frontend (dev)
# Usage: ./run-all.sh

BASE_DIR="$(cd "$(dirname "$0")" && pwd)"

fail() {
  echo "ERROR: $1" >&2
  exit 1
}

command -v mvn >/dev/null 2>&1 || fail "Maven (mvn) non trovato. Installa Maven e riprova."
command -v node >/dev/null 2>&1 || fail "Node.js non trovato. Installa Node.js e riprova."

echo "Starting EaaS (backend-eaas)"
(cd "$BASE_DIR/backend-eaas" && mvn spring-boot:run) &
EAAS_PID=$!
sleep 2

echo "Starting DaaS (backend-daas)"
(cd "$BASE_DIR/backend-daas" && mvn spring-boot:run) &
DAAS_PID=$!
sleep 2

echo "Starting Frontend (Vite)"
(cd "$BASE_DIR/frontend" && npm install --no-audit --no-fund && npm run dev) &
FRONT_PID=$!

echo
echo "Launched services. PIDs: EaaS=$EAAS_PID, DaaS=$DAAS_PID, FRONTEND=$FRONT_PID"
echo "Attendi i log nelle rispettive cartelle. Per stoppare:"
echo "  kill $EAAS_PID $DAAS_PID $FRONT_PID"

wait
