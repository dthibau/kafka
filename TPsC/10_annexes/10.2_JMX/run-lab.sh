#!/bin/bash
# run-lab.sh — Génère du trafic Kafka pour observer les métriques dans Grafana

TOPIC="test-monitoring"
BROKER="kafka-0:9092"
NUM_MESSAGES=500

echo "=== Atelier 10.2 — Monitoring Kafka ==="
echo ""

# 1. Créer le topic
echo ">>> Création du topic '$TOPIC' (3 partitions, RF 3)..."
/opt/kafka/bin/kafka-topics.sh \
  --bootstrap-server "$BROKER" \
  --create --topic "$TOPIC" \
  --partitions 3 --replication-factor 3 \
  --if-not-exists

echo ""

# 2. Lancer un consumer en background
echo ">>> Lancement du consumer en background..."
/opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server "$BROKER" \
  --topic "$TOPIC" --from-beginning &

# 3. Produire des messages en boucle
echo ">>> Production de $NUM_MESSAGES messages..."
seq 1 $NUM_MESSAGES | \
  /opt/kafka/bin/kafka-console-producer.sh \
  --bootstrap-server "$BROKER" \
  --topic "$TOPIC"

echo ""
echo ">>> $NUM_MESSAGES messages produits !"
echo ""
echo "=== URLs ==="
echo "  Grafana    : http://localhost:3000"
echo "  Prometheus : http://localhost:9091"
echo "  Redpanda   : http://localhost:8080"
echo ""
echo "Ouvrez Grafana et observez le dashboard 'Kafka Overview'."
