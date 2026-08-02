#!/usr/bin/env bash
# Deploy Custom Logging Connector to Anypoint Exchange
#
# Usage:
#   ./scripts/deploy.sh
#
# Required environment variables (or set in .env):
#   ANYPOINT_USERNAME  — Anypoint Platform username
#   ANYPOINT_PASSWORD  — Anypoint Platform password
#
# The groupId in pom.xml must already be set to the target Anypoint Org ID
# before running this script.

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

# Load .env if present
if [ -f "$PROJECT_ROOT/.env" ]; then
  echo "Loading .env..."
  set -a
  # shellcheck disable=SC1091
  source "$PROJECT_ROOT/.env"
  set +a
fi

# Validate required vars
: "${ANYPOINT_USERNAME:?ANYPOINT_USERNAME is not set. Export it or add it to .env}"
: "${ANYPOINT_PASSWORD:?ANYPOINT_PASSWORD is not set. Export it or add it to .env}"

echo "Deploying Custom Logging Connector to Anypoint Exchange..."
echo "Project root: $PROJECT_ROOT"

mvn deploy \
  -f "$PROJECT_ROOT/pom.xml" \
  -DskipTests \
  -Danypoint.username="$ANYPOINT_USERNAME" \
  -Danypoint.password="$ANYPOINT_PASSWORD"

echo "Deploy complete."
