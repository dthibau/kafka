# Utilise l'image officielle Confluent Kafka Connect comme base
FROM confluentinc/cp-kafka-connect:7.8.0

# Définir une variable pour la version du plugin Elasticsearch
ENV ELASTICSEARCH_CONNECT_VERSION=14.0.0

# Télécharger et installer le plugin Elasticsearch Connect
RUN confluent-hub install --no-prompt confluentinc/kafka-connect-elasticsearch:$ELASTICSEARCH_CONNECT_VERSION

# Assurez-vous que le plugin est bien ajouté au chemin des plugins
ENV CONNECT_PLUGIN_PATH="/usr/share/java,/usr/share/confluent-hub-components"

# Exposer le port pour Kafka Connect REST (optionnel)
EXPOSE 8083
