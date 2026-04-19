package org.formation.model;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.RecordMetadata;

public class ProducerCallback implements Callback {
    @Override
    public void onCompletion(RecordMetadata recordMetadata, Exception e) {
        // System.out.println("Calback " + recordMetadata + " with exception " + e);
        if (e != null) {
            e.printStackTrace();
        }
    }
}
