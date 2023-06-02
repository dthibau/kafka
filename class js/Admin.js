import { Kafka } from 'kafkajs';

export class Admin {
  kafkaClient
  client
  constructor(config) {
    this.kafkaClient = new Kafka(config)
    this.client = this.kafkaClient.admin()
  }

  connect() {
    return this.client.connect()
  }

  async deleteTopics(topics) {
    await this.client.deleteTopics({ topics });
  }

  async createTopics(topics) {
    await this.client.createTopics({ topics: topics.map(topic => ({ topic })) });
  }
  disconnect() {
    return this.client.disconnect()
  }
}
