package org.formation;

import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.formation.model.Courier;
import org.formation.model.Position;
import org.formation.model.ProducerCallback;
import org.formation.model.SendMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

import static org.formation.KafkaProducerApplication.TOPIC;

public class KafkaProducerThread implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(KafkaProducerThread.class);

	private long nbMessages,sleep;
	private SendMode sendMode;
	private Courier courier;

	private KafkaProducer<String, Courier> producer;
	ProducerCallback callback = new ProducerCallback();

	public KafkaProducerThread(String id, long nbMessages, long sleep, SendMode sendMode) {
		this.nbMessages = nbMessages;
		this.sleep = sleep;
		this.sendMode = sendMode;
		this.courier = new Courier(id, "David", 1, new Position(Math.random() + 45, Math.random() + 2));
		
		_initProducer();
		
	}

	@Override
	public void run() {
		
		for (int i =0; i< nbMessages; i++) {
			// courier.move();
			
			ProducerRecord<String, Courier> producerRecord = new ProducerRecord(TOPIC, courier.getId(), courier);
			
			switch (sendMode) {
			case FIRE_AND_FORGET:
				fireAndForget(producerRecord);
				break;
			case SYNCHRONOUS:
				try {
					synchronous(producerRecord);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (ExecutionException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				break;
			case ASYNCHRONOUS:
				asynchronous(producerRecord);
				break;
			default:
				break;
			}
			
			
			try {
				Thread.sleep(sleep);
			} catch (InterruptedException e) {
				System.err.println("INTERRUPTED");
			}
		}
		
	}
	
	public void fireAndForget(ProducerRecord<String,Courier> record) {

		// System.out.println("fireAndForget " + record.key());
		// A compléter
		producer.send(record);

		
	}
	
	public void synchronous(ProducerRecord<String,Courier> record) throws InterruptedException, ExecutionException {
		// A compléter
		RecordMetadata metadata = producer.send(record).get();
	//	System.out.println("Envoi synchrone " + metadata.offset() + " " + metadata.partition() + " " + metadata.timestamp());
		
	}
	public void asynchronous(ProducerRecord<String,Courier> record) {
		// A compléter
		producer.send(record,callback);
	}
	
	private void _initProducer() {

		Properties kafkaProps = new Properties();
		kafkaProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
				"localhost:19092,localhost:19093");
		kafkaProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
				"org.apache.kafka.common.serialization.StringSerializer");
		kafkaProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "io.confluent.kafka.serializers.KafkaAvroSerializer");
		kafkaProps.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, KafkaProducerApplication.REGISTRY_URL);


		producer = new KafkaProducer<String, Courier>(kafkaProps);

	}
}
