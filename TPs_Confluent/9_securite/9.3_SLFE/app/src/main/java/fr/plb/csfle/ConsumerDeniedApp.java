package fr.plb.csfle;

import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.List;
import java.util.Properties;

/**
 * Consumer NON AUTORISÉ : token Vault invalide.
 *
 * Deux scénarios au choix selon onFailure dans la rule :
 *   - ERROR,NONE (par défaut dans cet atelier) : la désérialisation échoue et le consumer voit une exception.
 *   - NONE,NONE : les champs chiffrés restent sous forme de bytes opaques et la désérialisation réussit.
 *
 * On utilise onFailure=ERROR,NONE donc on attrape l'exception pour la rendre parlante.
 */
public class ConsumerDeniedApp {

    public static void main(String[] args) {
        Properties p = new Properties();

        p.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:19092,localhost:19093,localhost:19094");
        p.put(ConsumerConfig.GROUP_ID_CONFIG, "consumer-denied");
        p.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        p.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        p.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);

        p.put("security.protocol", "SASL_SSL");
        p.put("sasl.mechanism", "PLAIN");
        p.put("sasl.jaas.config",
                "org.apache.kafka.common.security.plain.PlainLoginModule required "
                        + "username=\"bob\" password=\"bob-secret\";");
        p.put("ssl.truststore.location", System.getenv().getOrDefault(
                "TRUSTSTORE", "../ssl/mount/kafka.truststore.jks"));
        p.put("ssl.truststore.password", "secret");
        p.put("ssl.endpoint.identification.algorithm", "");

        p.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081");
        p.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);
        p.put("rule.service.loader.enable", true);

        // Token Vault INVALIDE : Vault refusera l'appel de décryption sur la KEK
        p.put("rule.executors._default_.param.token.id", "mauvais-token");

        try (KafkaConsumer<String, Customer> consumer = new KafkaConsumer<>(p)) {
            consumer.subscribe(List.of("customers"));
            System.out.println("[DENIED] En attente de messages (un accès Vault invalide est configuré)…");
            long deadline = System.currentTimeMillis() + 20_000;
            while (System.currentTimeMillis() < deadline) {
                try {
                    ConsumerRecords<String, Customer> records = consumer.poll(Duration.ofSeconds(2));
                    for (ConsumerRecord<String, Customer> r : records) {
                        // On n'y arrivera pas si onFailure=ERROR
                        Customer c = r.value();
                        System.out.printf("[DENIED] contenu déchiffré inattendu: %s%n", c);
                    }
                } catch (Exception e) {
                    System.err.printf("[DENIED] Échec attendu au déchiffrement : %s%n", e.getMessage());
                    // On sort pour ne pas spammer : l'offset ne sera pas commité
                    break;
                }
            }
        }
    }
}
