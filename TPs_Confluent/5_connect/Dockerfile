# Image Kafka Connect Confluent avec connecteur Elasticsearch
FROM confluentinc/cp-kafka-connect:7.7.8

ENV ELASTICSEARCH_CONNECT_VERSION=15.1.0

# Installer le connecteur Elasticsearch depuis Confluent Hub
RUN confluent-hub install --no-prompt \
    confluentinc/kafka-connect-elasticsearch:${ELASTICSEARCH_CONNECT_VERSION}

ENV CONNECT_PLUGIN_PATH="/usr/share/java,/usr/share/confluent-hub-components"

EXPOSE 8083
