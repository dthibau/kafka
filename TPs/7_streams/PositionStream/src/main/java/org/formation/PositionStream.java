package org.formation;

import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;
import org.formation.model.Coursier;
import org.formation.model.Position;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class PositionStream {

    public static String TOPIC = "avro-position";
    public static String REGISTRY_URL = "http://localhost:8081";

    public static void main(String[] args) {
        // Propriétés : ID, BOOTSTRAP, Serialiseur/Désérialiseur
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "position-stream");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:19092,localhost:19093");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, SpecificAvroSerde.class);
        props.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, REGISTRY_URL);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        Map<String, Object> config = new HashMap<>();
        config.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, REGISTRY_URL); // URL
        SpecificAvroSerde<Position> positionSerde = new SpecificAvroSerde<>();
        positionSerde.configure(config, true);

        // Création d’une topolgie de processeurs
        final StreamsBuilder builder = new StreamsBuilder();

        Map<String, KStream<Position,String>> branches = builder.<String, Coursier>stream(TOPIC).mapValues(coursier -> {
            Position position = (Position)coursier.getPosition();
            // Transformation : calcul de la distance
            position.setLatitude((double)Math.round(position.getLatitude()));
            position.setLongitude((double)Math.round(position.getLongitude()));
            return coursier;
        }).selectKey((s, coursier) -> (Position)coursier.getPosition())
                .mapValues(coursier -> coursier.getId().toString())
                .split(Named.as("Geo-"))
                .branch((key, value) -> key.getLatitude() > 45, Branched.as("North"))
                .defaultBranch(Branched.as("South"));

      KTable<Position,Long> countTable = branches.get("Geo-North")
              .groupByKey(Grouped.with(positionSerde, Serdes.String())).count(Materialized.with(positionSerde, Serdes.Long()));
      countTable.toStream().mapValues(l -> l.toString()).to("count-north",Produced.with(positionSerde, Serdes.String()));

      branches.get("Geo-South").to("avro-position-south", Produced.with(positionSerde, Serdes.String()));


        final Topology topology = builder.build();
        final KafkaStreams streams = new KafkaStreams(topology, props);

        final CountDownLatch latch = new CountDownLatch(1);

        // attach shutdown handler to catch control-c
        Runtime.getRuntime().addShutdownHook(new Thread("streams-shutdown-hook") {
            @Override
            public void run() {
                streams.close();
                latch.countDown();
            }
        });

        // Démarrage du stream
        try {
            streams.start();
            latch.await();
        } catch (Throwable e) {
            System.exit(1);
        }
        System.exit(0);
    }

}
