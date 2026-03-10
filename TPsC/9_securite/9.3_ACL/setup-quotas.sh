#!/bin/bash
# ── Atelier Quotas Kafka ─────────────────────────────────────────────────────
# Applique un quota producer_byte_rate très bas sur kafka-producer-client
# pour démontrer le throttling côté producer.
#
# Usage : docker exec kafka-0 bash /tmp/setup-quotas.sh
# ──────────────────────────────────────────────────────────────────────────────

KAFKA_BIN="/opt/kafka/bin"
BOOTSTRAP="localhost:9091"
CMD_CONFIG="/etc/kafka/admin.properties"
USER="kafka-producer-client"
RATE=1024

echo "========================================"
echo " Kafka Quotas — Atelier"
echo "========================================"
echo ""

# 1. Appliquer le quota
echo ">>> Application du quota producer_byte_rate=${RATE} bytes/sec sur User:${USER} ..."
${KAFKA_BIN}/kafka-configs.sh \
  --bootstrap-server ${BOOTSTRAP} \
  --command-config ${CMD_CONFIG} \
  --alter \
  --add-config "producer_byte_rate=${RATE}" \
  --entity-type users \
  --entity-name ${USER}

echo ""

# 2. Vérifier les quotas actuels
echo ">>> Quotas actuels pour User:${USER} :"
${KAFKA_BIN}/kafka-configs.sh \
  --bootstrap-server ${BOOTSTRAP} \
  --command-config ${CMD_CONFIG} \
  --describe \
  --entity-type users \
  --entity-name ${USER}

echo ""
echo "========================================"
echo " Pour SUPPRIMER le quota :"
echo "  docker exec kafka-0 /opt/kafka/bin/kafka-configs.sh \\"
echo "    --bootstrap-server localhost:9091 \\"
echo "    --command-config /etc/kafka/admin.properties \\"
echo "    --alter --delete-config producer_byte_rate \\"
echo "    --entity-type users --entity-name ${USER}"
echo "========================================"
