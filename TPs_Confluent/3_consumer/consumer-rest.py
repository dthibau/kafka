#!/usr/bin/env python3
"""
consumer-rest.py — Consommateur Kafka via Confluent REST Proxy
Atelier 3 (variante REST Proxy) — Formation Confluent Developer

Usage :
  python3 consumer-rest.py [--group GROUP] [--consumer-id ID]
                           [--proxy URL] [--topic TOPIC]
                           [--poll-interval MS]
"""

import argparse
import json
import signal
import sys
import time
import requests
import psycopg2

# ── Configuration base de données ────────────────────────────────
DB_CONFIG = {
    "host":     "localhost",
    "port":     5432,
    "dbname":   "consumer",
    "user":     "postgres",
    "password": "postgres",
}

# ── Parsing des arguments ─────────────────────────────────────────
parser = argparse.ArgumentParser(description="Consumer REST Proxy Kafka")
parser.add_argument("--group",         default="position-group",      help="Nom du consumer group")
parser.add_argument("--consumer-id",   default="consumer-1",          help="ID unique de l'instance consommateur")
parser.add_argument("--proxy",         default="http://localhost:8082",help="URL du REST Proxy")
parser.add_argument("--topic",         default="position",             help="Topic Kafka à consommer")
parser.add_argument("--poll-interval", type=int, default=500,          help="Délai entre deux polls en ms")
parser.add_argument("--crash-after",  type=int, default=0,            help="Crash brutal après N messages sans commiter (0=désactivé)")
args = parser.parse_args()

BASE_URL  = f"{args.proxy}/consumers/{args.group}/instances/{args.consumer_id}"
HEADERS_V2   = {"Content-Type": "application/vnd.kafka.v2+json"}
HEADERS_ACCEPT = {"Accept": "application/vnd.kafka.json.v2+json"}

# ── Connexion Postgres ────────────────────────────────────────────
def get_db():
    return psycopg2.connect(**DB_CONFIG)

def insert_position(conn, courier_id, kafka_offset, latitude, longitude):
    with conn.cursor() as cur:
        cur.execute(
            "INSERT INTO positions (courier_id, kafka_offset, latitude, longitude) "
            "VALUES (%s, %s, %s, %s)",
            (courier_id, kafka_offset, latitude, longitude)
        )
    conn.commit()

# ── Gestion du signal SIGINT (Ctrl+C) ────────────────────────────
running = True
def handle_sigint(sig, frame):
    global running
    print("\n[INFO] Arrêt demandé — suppression de l'instance consommateur...")
    running = False
signal.signal(signal.SIGINT, handle_sigint)

# ── Création de l'instance consommateur ──────────────────────────
def create_consumer():
    url = f"{args.proxy}/consumers/{args.group}"
    payload = {
        "name": args.consumer_id,
        "format": "json",
        "auto.offset.reset": "earliest",
        "auto.commit.enable": "false",
    }
    resp = requests.post(url, headers=HEADERS_V2, json=payload)
    if resp.status_code == 409:
        print(f"[INFO] Instance {args.consumer_id} déjà existante dans le groupe {args.group}")
    elif resp.status_code not in (200, 201):
        print(f"[ERREUR] Création consommateur : {resp.status_code} {resp.text}")
        sys.exit(1)
    else:
        print(f"[OK] Consommateur créé : {resp.json()['base_uri']}")

# ── Abonnement au topic ───────────────────────────────────────────
def subscribe():
    resp = requests.post(
        f"{BASE_URL}/subscription",
        headers=HEADERS_V2,
        json={"topics": [args.topic]}
    )
    if resp.status_code != 204:
        print(f"[ERREUR] Abonnement : {resp.status_code} {resp.text}")
        sys.exit(1)
    print(f"[OK] Abonné au topic '{args.topic}' — attente du rebalance initial...")
    time.sleep(5)  # laisser le temps au proxy d'affecter les partitions
    print("[OK] Prêt à consommer")

# ── Poll des messages ─────────────────────────────────────────────
def poll_records():
    resp = requests.get(f"{BASE_URL}/records?max_bytes=1048576", headers=HEADERS_ACCEPT)
    if resp.status_code != 200:
        print(f"[WARN] Poll : {resp.status_code} {resp.text}")
        return []
    return resp.json()

# ── Commit des offsets ────────────────────────────────────────────
def commit_offsets(records):
    if not records:
        return
    # Calculer le dernier offset par partition
    last_offsets = {}
    for r in records:
        key = (r["topic"], r["partition"])
        if key not in last_offsets or r["offset"] > last_offsets[key]:
            last_offsets[key] = r["offset"]

    offsets_payload = [
        {"topic": t, "partition": p, "offset": off + 1}
        for (t, p), off in last_offsets.items()
    ]
    resp = requests.post(
        f"{BASE_URL}/offsets",
        headers=HEADERS_V2,
        json={"offsets": offsets_payload}
    )
    if resp.status_code != 200:
        print(f"[WARN] Commit offsets : {resp.status_code} {resp.text}")

# ── Suppression de l'instance ─────────────────────────────────────
def delete_consumer():
    resp = requests.delete(BASE_URL, headers=HEADERS_V2)
    if resp.status_code in (200, 204):
        print("[OK] Instance consommateur supprimée")
    else:
        print(f"[WARN] Suppression : {resp.status_code} {resp.text}")

# ── Boucle principale ─────────────────────────────────────────────
def main():
    print(f"[INFO] Démarrage — group={args.group} consumer={args.consumer_id} topic={args.topic}")

    create_consumer()
    subscribe()

    try:
        conn = get_db()
        print("[OK] Connecté à Postgres")
    except Exception as e:
        print(f"[ERREUR] Connexion Postgres : {e}")
        delete_consumer()
        sys.exit(1)

    total = 0
    print(f"[INFO] Début du polling (intervalle {args.poll_interval} ms) — Ctrl+C pour arrêter\n")

    while running:
        records = poll_records()

        if records:
            for r in records:
                try:
                    value     = r["value"]
                    courier   = value.get("courierId", r.get("key", "unknown"))
                    latitude  = value.get("latitude",  0.0)
                    longitude = value.get("longitude", 0.0)
                    offset    = r["offset"]
                    partition = r["partition"]

                    insert_position(conn, courier, offset, latitude, longitude)
                    total += 1
                    print(f"  [MSG] partition={partition} offset={offset} "
                          f"courier={courier} lat={latitude} lon={longitude}")
                except Exception as e:
                    conn.rollback()
                    if "uq_courier_offset" in str(e):
                        print(f"  [DOUBLON] partition={partition} offset={offset} "
                              f"courier={courier} — déjà traité (At Least Once)")
                    else:
                        print(f"  [ERREUR] Traitement message : {e}")

            # Crash simulé AVANT le commit (pour tester At Least Once)
            if args.crash_after > 0 and total >= args.crash_after:
                print(f"\n[CRASH SIMULÉ] {total} messages insérés en base SANS commit Kafka")
                print("[CRASH SIMULÉ] Relancez le script avec le même consumer-id pour voir les doublons")
                conn.close()
                # On ne supprime PAS l'instance ni ne commite — simule un crash brutal
                import os; os._exit(1)

            # Commit après traitement du batch (At Least Once)
            commit_offsets(records)
            print(f"  [COMMIT] {len(records)} messages — total={total}\n")

        time.sleep(args.poll_interval / 1000.0)

    # Nettoyage
    conn.close()
    delete_consumer()
    print(f"[INFO] Terminé — {total} messages traités au total")

if __name__ == "__main__":
    main()
