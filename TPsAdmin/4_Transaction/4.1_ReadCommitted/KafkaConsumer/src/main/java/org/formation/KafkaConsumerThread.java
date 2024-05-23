package org.formation;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.formation.model.Coursier;

public class KafkaConsumerThread implements Runnable, ConsumerRebalanceListener {

	public static String TOPIC = "position-tx";
	KafkaConsumer<String, Coursier> consumer;
	private int id;
	private boolean readCommited;
	

	public KafkaConsumerThread(int id, boolean readCommitted) {
		this.id = id;
		this.readCommited = readCommitted;

		_initConsumer();

	}

	@Override
	public void run() {
		int nbMessages = 0;
		try {
			while (true) {
				ConsumerRecords<String, Coursier> records = consumer.poll(Duration.ofMillis(100));
				System.out.println("Consumer " + id + " vient de recevoir " + records.count() + " records");
				for (var record : records) {
					nbMessages++;
					// Temps du traitement d'un message
					Thread.sleep(10);
				}
				System.out.println("Consumer " + id + " a reçu " + nbMessages + " messages au total");
			}
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		} catch (WakeupException e) {
			System.out.println("CLOSING : Consumer " + id + " a reçu " + nbMessages + " messages au total");
		}  finally {
			consumer.close();
		}
	}

	private void _initConsumer() {
		Properties kafkaProps = new Properties();
		kafkaProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092,localhost:9192,localhost:9292");
		kafkaProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
		kafkaProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.formation.JsonDeserializer");
		kafkaProps.put(ConsumerConfig.GROUP_ID_CONFIG, "position-tx-consumer"+readCommited);
		kafkaProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
//		kafkaProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,"false");
		if ( readCommited )
			kafkaProps.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");

		consumer = new KafkaConsumer<String, Coursier>(kafkaProps);
		consumer.subscribe(Collections.singletonList(TOPIC),this);
	}

	@Override
	public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
		System.out.println("Process " +  ProcessHandle.current().pid() + " Thread " +id + " Revocation of " + partitions);
		
	}

	@Override
	public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
		System.out.println("Process " +  ProcessHandle.current().pid() + " Thread " +id + " Assignement of " + partitions);		
	}
}
