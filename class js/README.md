Les différentes classes se basent sur la lib kafkajs et prennent les configurations à l'instantiation.

Ces différentes classes doivent se connecter et cette methode retourne une promesse


```javascript
const consumer = new Consumer({
  clientId: 'consumer',
  brokers: ['localhost:19092'],
  groupId: 'test-group',
})
    
await consumer.connect()
await consumer.subscribe('topicName', (event) => {
  const {message} = event;
  if (message.value) {
    console.log(JSON.parse(message.value.toString()))
    return
  }
  
  console.log('message empty');
})
```

```javascript
const producer = new Producer({
  clientId: 'producer',
  brokers: ['localhost:19092'],
})

await producer.connect()
await producer.publish('topic', { foo: 'bar' })
```

```javascript
const topics = ['topic1', 'topic2']

const admin = new Admin( {
  clientId: 'admin',
  brokers: ['host.docker.internal:9092']
});

await admin.connect()
await admin.createTopics(topics)
```
