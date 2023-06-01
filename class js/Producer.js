import { Kafka } from 'kafkajs';

export class Producer {
  kafkaClient
  client
  constructor(config) {
    this.kafkaClient = new Kafka(config)
    this.client = this.kafkaClient.producer()
  }

  connect() {
    return this.client.connect()
  }

  publish(topic, payload) {
    return this.client.send({
      topic,
      messages: [
        {
          value: JSON.stringify(payload)
        }
      ]
    })
  }

  disconnect() {
    return this.client.disconnect()
  }
}
