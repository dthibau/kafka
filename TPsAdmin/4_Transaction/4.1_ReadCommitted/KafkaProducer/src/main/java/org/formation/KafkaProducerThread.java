package org.formation;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.formation.model.Coursier;
import org.formation.model.Position;
import org.formation.model.SendMode;

public class KafkaProducerThread implements Runnable {

	public static int NB_BATCH=10;
	
	public static String TOPIC ="position-tx";
	KafkaProducer<String,Coursier> producer;
	private long nbMessages,sleep;
	private SendMode sendMode;
	
	private Coursier coursier;
	
	private ProducerCallback callback = new ProducerCallback();
	
	public KafkaProducerThread(String id, long nbMessages, long sleep, SendMode sendMode) {
		this.nbMessages = nbMessages;
		this.sleep = sleep;
		this.sendMode = sendMode;
		this.coursier = new Coursier(id, new Position(Math.random() + 45, Math.random() + 2));
		
		_initProducer();
		
	}

	@Override
	public void run() {
		int nbBatch=0;
		producer.initTransactions();
		
		for (int i =0; i< nbMessages; i++) {
			
			if (nbBatch == 0 ) {
				producer.beginTransaction();
			}
			coursier.move();
			
			ProducerRecord<String, Coursier> producerRecord = new ProducerRecord<String, Coursier>(TOPIC, coursier.getId(), coursier);
			
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
			nbBatch++;
			if ( nbBatch == NB_BATCH ) {
				producer.commitTransaction();
				nbBatch=0;
			}
			
			try {
				Thread.sleep(sleep);
			} catch (InterruptedException e) {
				System.err.println("INTERRUPTED");
			}
		}
		
	}
	
	public void fireAndForget(ProducerRecord<String,Coursier> record) {
		
		producer.send(record);

	}
	
	public void synchronous(ProducerRecord<String,Coursier> record) throws InterruptedException, ExecutionException {
		RecordMetadata metaData = producer.send(record).get();

	}
	public void asynchronous(ProducerRecord<String,Coursier> record) {
		producer.send(record,callback);
	}
	
	private void _initProducer() {
		Properties kafkaProps = new Properties();
		kafkaProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092,localhost:9192,localhost:9292");
		kafkaProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
				"org.apache.kafka.common.serialization.StringSerializer");
		kafkaProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
				"org.formation.JsonSerializer");
		kafkaProps.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "tx-position"+coursier.getId());
		kafkaProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");

		producer = new KafkaProducer<String, Coursier>(kafkaProps);

		// A compléter
	}
}
