import { Kafka } from 'kafkajs';

export class Consumer {
  kafkaClient
  client
  constructor(config) {
    const { groupId, ...kafkaConfig } = config
    this.kafkaClient = new Kafka(kafkaConfig)
    this.client = this.kafkaClient.consumer({ groupId })
  }

  connect() {
    return this.client.connect()
  }

  async subscribe(topics, eachMessage) {
    try {
      await this.client.subscribe({ topics })
      return this.client.run({ eachMessage })
    } catch (err) {
      console.log(err)
    }
  }
  disconnect() {
    return this.client.disconnect()
  }
}
