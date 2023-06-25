package org.formation;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.formation.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class Listener {

	private static final Logger logger = LoggerFactory.getLogger(Listener.class);

	Map<String, Integer> lastIndex = new HashMap<>();
	int nbMessages = 0;
	long start = -1;

	@KafkaListener(topics = "position")
	public void listenPartition0(ConsumerRecord<String, Message> record) throws InterruptedException {
		if (start == -1) {
			start = System.currentTimeMillis();
		}
		if (lastIndex.get(record.key()) != null && lastIndex.get(record.key()) != record.value().getIndex() - 1) {
			logger.info("Listener Thread ID: " + Thread.currentThread().getId() + " partition :" + record.partition());
			logger.info(record.value().getIndex() + " suit " + lastIndex.get(record.key()));
		}
		// Temps du traitement
		Thread.sleep(10);
		lastIndex.put(record.key(), record.value().getIndex());
		nbMessages++;
		long elapsedTime = System.currentTimeMillis() - start;
		if (nbMessages % 1000 == 0) {
			logger.info("Thread : " + Thread.currentThread().getId() + " Process " + nbMessages + " in " + elapsedTime
					+ "ms Débit : " + ((float)nbMessages / (float)elapsedTime)*100 +" msg/s");
		}
		logger.debug(record.key()+"/"+record.value().getIndex());
	}
}
