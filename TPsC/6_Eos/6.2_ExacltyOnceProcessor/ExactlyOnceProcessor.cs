using System;
using System.Text.Json;
using System.Threading;
using Confluent.Kafka;

internal class ExactlyOnceProcessor
{
    private readonly string _bootstrapServers;
    private readonly string _groupId;
    private readonly string _inputTopic;
    private readonly string _outputTopic;

    public ExactlyOnceProcessor(string bootstrapServers, string groupId, string inputTopic, string outputTopic)
    {
        _bootstrapServers = bootstrapServers;
        _groupId = groupId;
        _inputTopic = inputTopic;
        _outputTopic = outputTopic;
    }

    public void Process(int nbMessages, CancellationToken cancellationToken)
    {
        // Configuration du Producer transactionnel
        // A compléter :
        //   - BootstrapServers
        //   - EnableIdempotence = true
        //   - TransactionalId = "exactly-once-processor"
        var producerConfig = new ProducerConfig
        {
            // A compléter
        };

        // Configuration du Consumer
        // A compléter :
        //   - BootstrapServers
        //   - GroupId
        //   - EnableAutoCommit = false (les offsets sont committés via la transaction)
        //   - AutoOffsetReset = AutoOffsetReset.Earliest
        //   - IsolationLevel = IsolationLevel.ReadCommitted
        var consumerConfig = new ConsumerConfig
        {
            // A compléter
        };

        using var producer = new ProducerBuilder<string, string>(producerConfig).Build();
        using var consumer = new ConsumerBuilder<string, string>(consumerConfig).Build();

        // Initialisation des transactions
        // A compléter

        consumer.Subscribe(_inputTopic);

        int messagesTraites = 0;

        try
        {
            while (messagesTraites < nbMessages && !cancellationToken.IsCancellationRequested)
            {
                var consumeResult = consumer.Consume(cancellationToken);
                if (consumeResult.IsPartitionEOF)
                    continue;

                // Transformation : calcul de la distance au point d'origine (0,0)
                string transformedValue = Transform(consumeResult.Message.Value);

                // A compléter : Boucle transactionnelle
                //   1. BeginTransaction
                //   2. Produce vers le topic de sortie (clé = même clé, valeur = message transformé)
                //   3. SendOffsetsToTransaction (consumer offsets + group metadata)
                //   4. CommitTransaction

                messagesTraites++;
                Console.WriteLine($"Message {messagesTraites}/{nbMessages} traité : partition={consumeResult.Partition}, offset={consumeResult.Offset}");
            }
        }
        catch (OperationCanceledException)
        {
            Console.WriteLine("Traitement interrompu.");
        }
        finally
        {
            consumer.Close();
        }

        Console.WriteLine($"Traitement terminé. {messagesTraites} messages traités.");
    }

    /// <summary>
    /// Transforme un message JSON position en ajoutant la distance au point d'origine (0,0).
    /// </summary>
    private string Transform(string jsonValue)
    {
        using var doc = JsonDocument.Parse(jsonValue);
        var root = doc.RootElement;

        double latitude = 0, longitude = 0;

        // Le message peut contenir un objet Position directement ou un Coursier avec une Position
        if (root.TryGetProperty("Position", out var posElement))
        {
            latitude = posElement.GetProperty("Latitude").GetDouble();
            longitude = posElement.GetProperty("Longitude").GetDouble();
        }
        else if (root.TryGetProperty("Latitude", out _))
        {
            latitude = root.GetProperty("Latitude").GetDouble();
            longitude = root.GetProperty("Longitude").GetDouble();
        }

        double distance = Math.Sqrt(latitude * latitude + longitude * longitude);

        return JsonSerializer.Serialize(new
        {
            latitude,
            longitude,
            distanceFromOrigin = Math.Round(distance, 2)
        });
    }
}
