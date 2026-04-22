#!/usr/bin/env bash
# Initialise Vault pour l'atelier CSFLE :
#   - active le moteur Transit
#   - crée la clé "csfle-kek"
#
# Vault tourne en mode dev, donc déjà unsealed et avec un root token fixe.
set -euo pipefail

VAULT_ADDR="${VAULT_ADDR:-http://localhost:8200}"
VAULT_TOKEN="${VAULT_TOKEN:-root-token-atelier}"
KEY_NAME="${KEY_NAME:-csfle-kek}"

hdr=(-H "X-Vault-Token: ${VAULT_TOKEN}")

echo "→ Activation du moteur Transit"
curl -fsS "${hdr[@]}" -X POST \
  -d '{"type":"transit"}' \
  "${VAULT_ADDR}/v1/sys/mounts/transit" \
  || echo "  (déjà activé)"

echo "→ Création de la clé ${KEY_NAME}"
curl -fsS "${hdr[@]}" -X POST \
  -d '{"type":"aes256-gcm96"}' \
  "${VAULT_ADDR}/v1/transit/keys/${KEY_NAME}" \
  || echo "  (déjà créée)"

echo "→ Vérification : liste des clés"
curl -fsS "${hdr[@]}" "${VAULT_ADDR}/v1/transit/keys?list=true" | sed 's/,/,\n  /g'

echo
echo "Vault prêt. KEK disponible à : ${VAULT_ADDR}/v1/transit/keys/${KEY_NAME}"
