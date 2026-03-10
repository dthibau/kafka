# Atelier 10.2 — Monitoring Kafka avec JMX Exporter, Prometheus & Grafana

## Prérequis

- Docker et Docker Compose installés
- `jmx_prometheus_javaagent-1.5.0.jar` dans ce répertoire (télécharger depuis
  [GitHub Releases](https://github.com/prometheus/jmx_exporter/releases/tag/1.5.0))

## Démarrage du cluster

```bash
docker compose up -d
```

Attendez que tous les services soient healthy (~30s).

## URLs d'accès

| Service            | URL                          |
|--------------------|------------------------------|
| Grafana            | http://localhost:3000         |
| Prometheus         | http://localhost:9091         |
| Redpanda Console   | http://localhost:8080         |
| Schema Registry    | http://localhost:8081         |

## Générer du trafic

### Producer .NET

Depuis le répertoire `Producer` (bootstrap `localhost:19092`, PLAINTEXT) :

```bash
cd Producer
dotnet run 1 100 0
```

(1 thread, 100 messages, mode FIRE_AND_FORGET)

Les statistiques librdkafka s'affichent dans la console toutes les 5 secondes :
- Métriques globales : `msg_cnt`, `msg_size`, `tx`
- Par broker : `outbuf_cnt`, `txmsgs`, `txbytes`, `rtt.avg`, throttle

### Consumer .NET

Depuis le répertoire `Consumer` :

```bash
cd Consumer
dotnet run 1
```

Les statistiques librdkafka s'affichent dans la console toutes les 5 secondes :
- Par broker : `rxmsgs`, `rxbytes`, `rtt.avg`
- Consumer group : `rebalance_cnt`, `assignment_size`

### Producer CLI (alternative)

```bash
docker exec -it kafka-0 /opt/kafka/bin/kafka-console-producer.sh \
  --bootstrap-server kafka-0:9092 --topic test-monitoring
```

Tapez des messages puis `Ctrl+C` pour quitter.

### Consumer CLI (alternative)

```bash
docker exec -it kafka-0 /opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server kafka-0:9092 --topic test-monitoring --from-beginning
```

## Observer les métriques dans Grafana

1. Ouvrir http://localhost:3000
2. Aller dans le dashboard **"Kafka Overview"**

### Sections à observer

- **Throughput In/Out** : bytes/messages par topic et broker
- **Producer Performance** : RequestQueueTimeMs, LocalTimeMs, RemoteTimeMs
- **Consumer Performance** : idem côté fetch
- **Connections** : nombre de connexions actives, par version client
- **Request rate** : Produce/Fetch requests par seconde

> **Métriques client .NET** : les statistiques librdkafka sont affichées dans la
> console du Producer et du Consumer (toutes les 5s). Ces métriques ne sont pas
> visibles dans Grafana car librdkafka n'expose pas via JMX.

## Vérification

1. `docker compose up -d` — tous les services healthy
2. Grafana http://localhost:3000 — dashboard "Kafka Overview" visible
3. `cd Producer && dotnet run 1 100 0` — messages envoyés, stats affichées en console
4. `cd Consumer && dotnet run 1` — messages reçus, stats affichées en console
5. Prometheus http://localhost:9091 — targets kafka-0/1/2 UP

## Arrêt

```bash
docker compose down -v
```
