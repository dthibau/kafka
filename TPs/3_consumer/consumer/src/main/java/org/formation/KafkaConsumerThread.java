package org.formation;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.formation.model.Courier;

public class KafkaConsumerThread implements Runnable {

	public static String TOPIC = "position";
	KafkaConsumer<String, Courier> consumer;
	private String id;
	
	

	public KafkaConsumerThread(String id) {
		this.id = id;

		_initConsumer();

	}

	@Override
	public void run() {
		Map<String, Integer> updateMap = new HashMap<>();
		try {
			while (true) {

				// A compléter
				
			}
		} finally {
			consumer.close();
		}

	}

	private void _initConsumer() {

	}
}
