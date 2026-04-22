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
 * Consumer AUTORISÉ : a le token Vault, donc peut unwrap la DEK et déchiffrer.
 * Affiche email et creditCard en clair.
 */
public class ConsumerAllowedApp {

    public static void main(String[] args) {
        Properties p = new Properties();

        p.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:19092,localhost:19093,localhost:19094");
        p.put(ConsumerConfig.GROUP_ID_CONFIG, "consumer-allowed");
        p.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        p.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        p.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);

        p.put("security.protocol", "SASL_SSL");
        p.put("sasl.mechanism", "PLAIN");
        p.put("sasl.jaas.config",
                "org.apache.kafka.common.security.plain.PlainLoginModule required "
                        + "username=\"alice\" password=\"alice-secret\";");
        p.put("ssl.truststore.location", System.getenv().getOrDefault(
                "TRUSTSTORE", "../ssl/mount/kafka.truststore.jks"));
        p.put("ssl.truststore.password", "secret");
        p.put("ssl.endpoint.identification.algorithm", "");

        p.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081");
        p.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);
        p.put("rule.service.loader.enable", true);

        // Token Vault valide → déchiffrement OK
        p.put("rule.executors._default_.param.token.id", "root-token-atelier");

        try (KafkaConsumer<String, Customer> consumer = new KafkaConsumer<>(p)) {
            consumer.subscribe(List.of("customers"));
            System.out.println("[ALLOWED] En attente de messages…");
            long deadline = System.currentTimeMillis() + 20_000;
            while (System.currentTimeMillis() < deadline) {
                ConsumerRecords<String, Customer> records = consumer.poll(Duration.ofSeconds(2));
                for (ConsumerRecord<String, Customer> r : records) {
                    Customer c = r.value();
                    System.out.printf(
                            "[ALLOWED] %s %s %s  email=%s  carte=%s  pays=%s%n",
                            c.getId(), c.getFirstName(), c.getLastName(),
                            c.getEmail(), c.getCreditCard(), c.getCountry());
                }
            }
        }
    }
}
