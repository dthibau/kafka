#!/bin/bash
# ── Atelier 9.3 — Configuration des ACLs Kafka ──────────────────────────────
#
# Exécuter depuis kafka-0 :
#   docker exec kafka-0 bash /tmp/setup-acls.sh
#
# Utilise le listener BROKER (9091, SASL_PLAINTEXT/OAUTHBEARER)
# avec le fichier admin.properties (credentials kafka-broker = super user).
# Les principaux correspondent aux client IDs Keycloak (claim azp).
#
# Ce script pose un DENY sur le producer pour le bloquer.
# L'exercice consiste ensuite à ajouter les ALLOW manuellement.
# ──────────────────────────────────────────────────────────────────────────────

KAFKA_BIN="/opt/kafka/bin"
BOOTSTRAP="localhost:9091"
CMD_CONFIG="--command-config /etc/kafka/admin.properties"

echo "============================================"
echo "  Blocage du producer via DENY ACL"
echo "============================================"

# DENY WRITE sur le topic 'position' pour kafka-producer-client
$KAFKA_BIN/kafka-acls.sh --bootstrap-server $BOOTSTRAP $CMD_CONFIG \
  --add --deny-principal User:kafka-producer-client \
  --operation Write \
  --topic position

echo ""
echo "============================================"
echo "  ACLs actuelles :"
echo "============================================"
$KAFKA_BIN/kafka-acls.sh --bootstrap-server $BOOTSTRAP $CMD_CONFIG --list

echo ""
echo "============================================"
echo "  Le producer est maintenant bloque."
echo "  Pour le debloquer, supprimez le DENY et ajoutez les ALLOW :"
echo ""
echo "  # Supprimer le DENY"
echo "  kafka-acls.sh --bootstrap-server $BOOTSTRAP $CMD_CONFIG \\"
echo "    --remove --deny-principal User:kafka-producer-client \\"
echo "    --operation Write --topic position"
echo ""
echo "  # Ajouter les ALLOW"
echo "  kafka-acls.sh --bootstrap-server $BOOTSTRAP $CMD_CONFIG \\"
echo "    --add --allow-principal User:kafka-producer-client \\"
echo "    --operation Write --operation Describe --operation Create \\"
echo "    --topic position"
echo "============================================"
