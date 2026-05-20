#!/usr/bin/env bash
# Build and run all services: backend-eaas, backend-daas, frontend (dev)
# Usage: ./run-all.sh

set -e

BASE_DIR="$(cd "$(dirname "$0")" && pwd)"
TMP_ROOT="$BASE_DIR/.tmp"
TMP_HOME="$TMP_ROOT/home"
TMP_SYSTEM="$TMP_ROOT/system"
TMP_NPM_CACHE="$TMP_ROOT/npm-cache"
TMP_XDG_CACHE="$TMP_ROOT/xdg-cache"
TMP_XDG_CONFIG="$TMP_ROOT/xdg-config"

mkdir -p "$TMP_HOME" "$TMP_SYSTEM" "$TMP_NPM_CACHE" "$TMP_XDG_CACHE" "$TMP_XDG_CONFIG"

# Keep shell/cache/temp artifacts under .tmp instead of project root/home.
export HOME="$TMP_HOME"
export TMPDIR="$TMP_SYSTEM"
export NPM_CONFIG_CACHE="$TMP_NPM_CACHE"
export XDG_CACHE_HOME="$TMP_XDG_CACHE"
export XDG_CONFIG_HOME="$TMP_XDG_CONFIG"
export HISTFILE="$TMP_ROOT/.bash_history"

fail() {
  echo "ERROR: $1" >&2
  exit 1
}

command -v mvn >/dev/null 2>&1 || fail "Maven (mvn) non trovato. Installa Maven e riprova."
command -v node >/dev/null 2>&1 || fail "Node.js non trovato. Installa Node.js e riprova."

# Build phase
echo "======================================"
echo "Building EaaS (backend-eaas)"
echo "======================================"
(cd "$BASE_DIR/backend-eaas" && mvn clean package -q) || fail "EaaS build failed"

echo "======================================"
echo "Building DaaS (backend-daas)"
echo "======================================"
(cd "$BASE_DIR/backend-daas" && mvn clean package -q) || fail "DaaS build failed"

echo "======================================"
echo "Preparing Frontend"
echo "======================================"
(cd "$BASE_DIR/frontend" && npm install --no-audit --no-fund) || fail "Frontend npm install failed"

# Run phase
echo ""
echo "======================================"
echo "Starting services..."
echo "======================================"

EAAS_JAR="$(ls -1 "$BASE_DIR"/backend-eaas/target/backend-eaas-*.jar | head -n 1)"
DAAS_JAR="$(ls -1 "$BASE_DIR"/backend-daas/target/backend-daas-*.jar | head -n 1)"
EAAS_TMP_DIR="$BASE_DIR/backend-eaas/target/tomcat-eaas"
DAAS_TMP_DIR="$BASE_DIR/backend-daas/target/tomcat-daas"

[[ -f "$EAAS_JAR" ]] || fail "Jar EaaS non trovato in backend-eaas/target"
[[ -f "$DAAS_JAR" ]] || fail "Jar DaaS non trovato in backend-daas/target"

mkdir -p "$EAAS_TMP_DIR" "$DAAS_TMP_DIR"

echo "Starting EaaS on port 8081"
java -Djava.io.tmpdir="$EAAS_TMP_DIR" -jar "$EAAS_JAR" > "$BASE_DIR/eaas.log" 2>&1 &
EAAS_PID=$!
sleep 3

echo "Starting DaaS on port 8080"
java -Djava.io.tmpdir="$DAAS_TMP_DIR" -jar "$DAAS_JAR" > "$BASE_DIR/daas.log" 2>&1 &
DAAS_PID=$!
sleep 3

echo "Starting Frontend on port 5173"
(cd "$BASE_DIR/frontend" && npm run dev) > "$BASE_DIR/frontend.log" 2>&1 &
FRONT_PID=$!

echo ""
echo "======================================"
echo "✓ All services launched"
echo "======================================"
echo "EaaS:     http://localhost:8081/eaas"
echo "DaaS:     http://localhost:8080/daas"
echo "Frontend: http://localhost:5173"
echo ""
echo "Log files:"
echo "  EaaS:     $BASE_DIR/eaas.log"
echo "  DaaS:     $BASE_DIR/daas.log"
echo "  Frontend: $BASE_DIR/frontend.log"
echo ""
echo "To stop all services:"
echo "  kill $EAAS_PID $DAAS_PID $FRONT_PID"
echo ""

if ! kill -0 "$EAAS_PID" 2>/dev/null; then
  echo "ERROR: EaaS non in esecuzione. Controlla $BASE_DIR/eaas.log"
  exit 1
fi

if ! kill -0 "$DAAS_PID" 2>/dev/null; then
  echo "ERROR: DaaS non in esecuzione. Controlla $BASE_DIR/daas.log"
  exit 1
fi

if ! kill -0 "$FRONT_PID" 2>/dev/null; then
  echo "ERROR: Frontend non in esecuzione. Controlla $BASE_DIR/frontend.log"
  exit 1
fi

# Keep script running and trap signals
trap "kill $EAAS_PID $DAAS_PID $FRONT_PID 2>/dev/null; exit 0" SIGINT SIGTERM

wait
