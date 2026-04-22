#!/usr/bin/env bash
# Enregistre le schéma Avro Customer sur le subject customers-value,
# SANS rule d'encryption. La rule est ajoutée par le script 02.
#
# On fait ça via l'API REST Schema Registry plutôt que via le producer Java,
# pour que le subject existe avant que le producer tourne.
set -euo pipefail

SR_URL="${SR_URL:-http://localhost:8081}"
SUBJECT="${SUBJECT:-customers-value}"
SCHEMA_FILE="${SCHEMA_FILE:-$(dirname "$0")/../app/src/main/avro/customer.avsc}"

echo "→ Enregistrement du schéma depuis ${SCHEMA_FILE}"

# On encode le schéma en JSON string pour l'API
SCHEMA_JSON=$(jq -Rs . < "${SCHEMA_FILE}")

curl -fsS -X POST \
  -H "Content-Type: application/vnd.schemaregistry.v1+json" \
  "${SR_URL}/subjects/${SUBJECT}/versions" \
  -d "{\"schemaType\":\"AVRO\",\"schema\":${SCHEMA_JSON}}"
echo

echo "→ Version courante :"
curl -fsS "${SR_URL}/subjects/${SUBJECT}/versions/latest" | jq '.id, .version'
