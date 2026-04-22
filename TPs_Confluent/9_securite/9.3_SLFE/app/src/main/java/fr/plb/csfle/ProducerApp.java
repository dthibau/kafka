package fr.plb.csfle;

import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.List;
import java.util.Properties;

/**
 * Producer qui envoie des clients sur le topic "customers".
 *
 * Les champs email et creditCard sont taggés "PII" dans le schéma Avro.
 * La rule ENCRYPT attachée au subject customers-value déclenche le chiffrement
 * côté serializer via la KEK stockée dans HashiCorp Vault.
 */
public class ProducerApp {

    public static void main(String[] args) throws Exception {
        Properties p = new Properties();

        // Connexion Kafka (listener EXTERNAL SASL_SSL, user rest-proxy ou alice peu importe)
        p.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:19092,localhost:19093,localhost:19094");
        p.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        p.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);

        p.put("security.protocol", "SASL_SSL");
        p.put("sasl.mechanism", "PLAIN");
        p.put("sasl.jaas.config",
                "org.apache.kafka.common.security.plain.PlainLoginModule required "
                        + "username=\"alice\" password=\"alice-secret\";");
        p.put("ssl.truststore.location", System.getenv().getOrDefault(
                "TRUSTSTORE", "../ssl/mount/kafka.truststore.jks"));
        p.put("ssl.truststore.password", "secret");
        p.put("ssl.endpoint.identification.algorithm", "");

        // Schema Registry
        p.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081");

        // CSFLE — active le chargement des rule executors (encryption + hcvault)
        p.put(KafkaAvroSerializerConfig.AUTO_REGISTER_SCHEMAS, false);
        p.put(KafkaAvroSerializerConfig.USE_LATEST_VERSION, true);
        p.put("rule.service.loader.enable", true);

        // Credentials Vault — token root en mode dev, sinon approle/token scoped en prod
        p.put("rule.executors._default_.param.token.id", "root-token-atelier");

        try (KafkaProducer<String, Customer> producer = new KafkaProducer<>(p)) {
            List<Customer> batch = List.of(
                    Customer.newBuilder()
                            .setId("C-001").setFirstName("Alice").setLastName("Martin")
                            .setEmail("alice.martin@example.com")
                            .setCreditCard("4539 1488 0343 6467")
                            .setCountry("FR").build(),
                    Customer.newBuilder()
                            .setId("C-002").setFirstName("Bob").setLastName("Dupont")
                            .setEmail("bob.dupont@example.com")
                            .setCreditCard("5500 0000 0000 0004")
                            .setCountry("FR").build(),
                    Customer.newBuilder()
                            .setId("C-003").setFirstName("Claire").setLastName("Leroy")
                            .setEmail("claire.leroy@example.com")
                            .setCreditCard("3400 0000 0000 009")
                            .setCountry("BE").build()
            );

            for (Customer c : batch) {
                ProducerRecord<String, Customer> rec = new ProducerRecord<>("customers", c.getId().toString(), c);
                producer.send(rec, (md, ex) -> {
                    if (ex != null) ex.printStackTrace();
                    else System.out.printf("Envoyé: %s → partition=%d offset=%d%n",
                            c.getId(), md.partition(), md.offset());
                });
            }
            producer.flush();
        }
        System.out.println("Producer terminé. Les champs email et creditCard sont chiffrés dans le topic.");
    }
}
