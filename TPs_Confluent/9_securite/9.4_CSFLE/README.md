# Atelier CSFLE — Confluent Platform + HashiCorp Vault + Java Avro

Cet atelier illustre le **Client-Side Field Level Encryption** en action :

- Les champs `email` et `creditCard` sont taggés `PII` dans un schéma Avro.
- Une **rule ENCRYPT** attachée au subject déclenche le chiffrement côté producer.
- La **KEK** est dans Vault (moteur Transit). La DEK est générée par le client, wrappée par la KEK, attachée au message.
- Un consumer avec accès à Vault **déchiffre**. Un consumer sans accès **voit du chiffré** (ou échoue selon `onFailure`).

## Prérequis

- Ta stack Kafka existante opérationnelle (SASL_SSL sur EXTERNAL, `/etc/hosts` mappé si besoin).
- Le truststore `./ssl/mount/kafka.truststore.jks` utilisé par les clients Java.
- Java 17, Maven, `jq`, `curl`.

## Étape 1 — Ajouter Vault à la stack

Depuis la racine où est ton `docker-compose.yml` principal :

```bash
docker compose -f docker-compose.yml -f docker-compose.csfle.yml up -d vault
```

Vault tourne en **mode dev** : unsealed, root token `root-token-atelier`. Zéro config à faire. Ne jamais utiliser en prod.

UI disponible sur http://localhost:8200 (token : `root-token-atelier`).

## Étape 2 — Initialiser Vault (moteur Transit + clé KEK)

```bash
./scripts/01-init-vault.sh
```

## Étape 3 — Enregistrer le schéma Avro (sans rule pour l'instant)

```bash
./scripts/00-register-schema.sh
```

À ce stade, un producer pourrait produire en clair dans `customers`. Les tags `PII` sont dans le schéma mais **aucune rule ne les exploite**.

## Étape 4 — Poser la rule ENCRYPT avec params KMS inline

```bash
./scripts/02-register-kek-and-rule.sh
```

Ce script poste une nouvelle version du schéma avec un `ruleSet`. La rule dit : « chiffre tous les champs taggés `PII` via Vault Transit à l'URL indiquée ». Les paramètres KMS sont directement dans la rule, le serveur n'a rien à faire d'autre que stocker ce JSON. Pas de DEK Registry, pas de licence Enterprise requise.

## Étape 5 — Compiler le projet Java

```bash
cd app
mvn clean package
```

La compilation déclenche `avro-maven-plugin` qui génère la classe `Customer` à partir du `.avsc`.

## Étape 6 — Produire des messages chiffrés

```bash
cd app
mvn exec:java -Dexec.mainClass=fr.plb.csfle.ProducerApp
```

Le producer :

- Connecte en SASL_SSL sur les brokers (user `alice`).
- Récupère le schéma et sa rule depuis Schema Registry.
- Pour chaque message, demande à Vault de wrapper une DEK fraîche (premier message) ou réutilise la DEK en cache.
- Chiffre `email` et `creditCard` en AES-GCM avec la DEK en clair.
- Envoie le message avec la DEK wrappée en header.

**Ouvre Redpanda Console (http://localhost:9090)**, regarde le topic `customers` : les champs `email` et `creditCard` sont affichés en bytes base64 opaques. Les autres champs sont en clair.

## Étape 7 — Consommer (autorisé)

```bash
cd app
mvn exec:java -Dexec.mainClass=fr.plb.csfle.ConsumerAllowedApp
```

Le consumer a le bon token Vault → unwrap la DEK → déchiffre → affiche tout en clair.

## Étape 8 — Consommer (non autorisé)

```bash
cd app
mvn exec:java -Dexec.mainClass=fr.plb.csfle.ConsumerDeniedApp
```

Le consumer présente un mauvais token à Vault. Vault refuse la décryption de la DEK. Selon la rule :

- Avec `onFailure: "ERROR,NONE"` (paramétrage par défaut du script 02) : le consumer lève une exception à la désérialisation.
- Si on change en `"NONE,NONE"` : le consumer réussit la désérialisation mais les champs restent sous forme opaque.

## Scénarios à montrer en atelier

1. **Champs chiffrés au repos** — scroller dans Redpanda Console topic `customers` : montrer les champs PII en bytes, les autres en clair. Indépendant du consumer.

2. **Rotation de clé** — relancer la KEK Vault avec `POST /v1/transit/keys/csfle-kek/rotate`. Nouveau producer → nouvelle DEK wrappée avec la nouvelle version de KEK. Ancien consumer → toujours capable de lire les anciens messages car Vault garde les versions précédentes.

3. **Révocation d'accès** — invalider le token Vault du `ConsumerAllowedApp` → il ne peut plus déchiffrer ce qu'il lisait la veille. Ça montre que **la protection est indépendante des ACLs Kafka**.

4. **ksqlDB impuissant** — lancer une requête `SELECT email FROM customers` dans ksqlDB CLI : il renverra les bytes chiffrés. Illustre la limite « pas de stream processing sur champ chiffré ».

## Nettoyage

```bash
docker compose -f docker-compose.yml -f docker-compose.csfle.yml down vault
```

Le mode dev de Vault perd tout au redémarrage, donc il faudra rejouer les scripts `01` et `02` à chaque up.

## Dépannage rapide

- **`RuleException: Cannot find rule executor`** : la dépendance `kafka-schema-registry-client-encryption-hcvault` est absente du classpath, ou `rule.service.loader.enable=true` manque dans la config client.
- **Vault `403 permission denied`** : token invalide, moteur Transit non monté, ou la clé n'existe pas. Vérifier avec `curl -H "X-Vault-Token: root-token-atelier" http://localhost:8200/v1/transit/keys?list=true`.
- **`SSL handshake failed` côté producer** : vérifier la variable `TRUSTSTORE` et que `ssl.endpoint.identification.algorithm` est vide.
- **Le champ reste en clair dans le topic** : soit la rule n'est pas attachée au bon subject, soit le schéma posé ne contient pas les tags `PII` sur les champs. Vérifier avec `curl http://localhost:8081/subjects/customers-value/versions/latest | jq` — tu dois voir à la fois le schéma avec `confluent:tags` sur les champs PII et le `ruleSet` avec la rule `encryptPII`.

## Limites assumées pour cet atelier

- **Vault en mode dev** : clés en mémoire, root token public. À proscrire en prod.
- **Token Vault en clair dans le code** : en prod, passer par AppRole/JWT et injecter via env.
- **Pas de DEK Registry côté serveur** : on reste sur du CSFLE client-only. Les rules sont stockées telles quelles dans Schema Registry (data contract standard, dispo depuis CP 7.4). Chaque client appelle Vault directement pour wrapper/unwrapper la DEK. Conséquence : pas de cache de DEK partagé, chaque nouveau client rejoue un appel Vault sur son premier message. Négligeable pour une démo.
- **CSFLE côté serveur officiel** (Flink/ksqlDB capables de déchiffrer, DEK Registry, RBAC sur les KEKs) nécessite CP 8.0 + Add-On licencié. Hors scope de cet atelier.
