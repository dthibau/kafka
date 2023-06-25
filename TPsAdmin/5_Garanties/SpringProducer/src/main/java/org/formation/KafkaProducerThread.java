package org.formation;

import java.util.Date;

import org.formation.model.Courier;
import org.formation.model.Message;
import org.formation.model.Position;
import org.springframework.kafka.core.KafkaTemplate;

public class KafkaProducerThread implements Runnable {

	public static String TOPIC ="position";
	KafkaTemplate<Object, Object> template;
	int nbMessages=1000;
	private Courier courier;
	
	public KafkaProducerThread(String id, KafkaTemplate<Object, Object> template ) {
		this.courier = new Courier(id, new Position(Math.random() + 45, Math.random() + 2));
		this.template = template;
				
	}

	@Override
	public void run() {
		for (int i =0; i< nbMessages; i++) {
			
			this.template.send("position", courier.getId(), new Message(i,new Date(),courier));
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				System.err.println("INTERRUPTED");
			}
			courier.move();

		}
		
	}
	
	
}
