#!/usr/bin/env bash
# Attache une rule ENCRYPT au subject customers-value, AVEC les params KMS inline.
# Cette approche ne requiert PAS le DEK Registry côté serveur.
# La KEK est entièrement décrite dans la rule, le client Java saura appeler Vault.
set -euo pipefail

SR_URL="${SR_URL:-http://localhost:8081}"
SUBJECT="${SUBJECT:-customers-value}"
KEK_NAME="${KEK_NAME:-customer-kek}"
VAULT_HOST="${VAULT_HOST:-http://localhost:8200}"   # vu depuis les clients Java lancés sur la machine hôte
KEY_NAME="${KEY_NAME:-csfle-kek}"

echo "→ Pose de la rule ENCRYPT sur ${SUBJECT} (params KMS inline)"
LATEST=$(curl -fsS "${SR_URL}/subjects/${SUBJECT}/versions/latest")
SCHEMA=$(echo "$LATEST" | jq -r '.schema')
SCHEMA_TYPE=$(echo "$LATEST" | jq -r '.schemaType // "AVRO"')

jq -n \
  --arg schema "$SCHEMA" \
  --arg schemaType "$SCHEMA_TYPE" \
  --arg kek "$KEK_NAME" \
  --arg keyId "${VAULT_HOST}/transit/keys/${KEY_NAME}" \
  '{
    schema: $schema,
    schemaType: $schemaType,
    ruleSet: {
      domainRules: [
        {
          name: "encryptPII",
          kind: "TRANSFORM",
          type: "ENCRYPT",
          mode: "WRITEREAD",
          tags: ["PII"],
          params: {
            "encrypt.kek.name": $kek,
            "encrypt.kms.type": "hcvault",
            "encrypt.kms.key.id": $keyId
          },
          onFailure: "ERROR,NONE"
        }
      ]
    }
  }' | curl -fsS -X POST \
    -H "Content-Type: application/vnd.schemaregistry.v1+json" \
    "${SR_URL}/subjects/${SUBJECT}/versions" \
    -d @-
echo

echo "→ Vérification : dernière version de ${SUBJECT}"
curl -fsS "${SR_URL}/subjects/${SUBJECT}/versions/latest" | jq '.ruleSet'
