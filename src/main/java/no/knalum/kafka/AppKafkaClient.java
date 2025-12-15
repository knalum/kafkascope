package no.knalum.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaMetadata;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import no.knalum.config.BrokerConfig;
import no.knalum.menu.BrokerDialogSettings;
import no.knalum.menu.dialog.CreateTopicDialogParams;
import no.knalum.message.ConnectedToBrokerMessage;
import no.knalum.message.MessageBus;
import no.knalum.modal.ErrorModal;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.JsonDecoder;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.TopicPartitionInfo;
import org.apache.kafka.common.config.ConfigResource;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class AppKafkaClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppKafkaClient.class);
    static ObjectMapper mapper = new ObjectMapper();

    public static Set<String> connect(String broker, String schema) throws ExecutionException, InterruptedException {
        java.util.Properties props = new java.util.Properties();
        props.put("bootstrap.servers", broker);
        AdminClient client = AdminClient.create(props);
        return client.listTopics().names().get();
    }

    public static void connectToKafkaAndPopulateTree(String broker, String schema) {
        try {
            Set<String> topics = AppKafkaClient.connect(broker, schema);
            MessageBus.getInstance().publish(new ConnectedToBrokerMessage(BrokerConfig.getInstance().getBrokerUrl(), topics));
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("Error connect to kafka and populate tree: {}", e.getMessage());
            ErrorModal.showError("Error connecting to Kafka: " + e.getMessage());
        }
    }

    public static void sendMessageToBroker(String topic, String key, String value, Integer partition) {
        String schemaForTopic = getSchemaForTopic(topic);
        KafkaProducer<String, Object> producer = null;
        String broker = BrokerConfig.getInstance().getBrokerUrl();
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, broker);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put("schema.registry.url", "http://localhost:8081");

        Object recordValue;
        if (schemaForTopic == null) {
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            recordValue = value;
        } else {
            LOGGER.info("Using schema for topic {}", schemaForTopic);
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());


            Schema schema = new Schema.Parser().parse(schemaForTopic);
            JsonDecoder decoder = null;
            try {
                decoder = DecoderFactory.get().jsonDecoder(schema, value);
                SpecificDatumReader<GenericRecord> reader = new SpecificDatumReader<>(schema);
                recordValue = reader.read(null, decoder);
            } catch (Exception e) {
                e.printStackTrace();
                ErrorModal.showError("Error " + e.getMessage());
                return;
            }
        }


        try {
            producer = new KafkaProducer<>(props);
            producer.send(new ProducerRecord<>(topic, partition, key, recordValue));
        } catch (Exception e) {
            e.printStackTrace();
            ErrorModal.showError("Error sending message to broker: " + e.getMessage());
            LOGGER.error("Error sending message to broker: {}", e.getMessage());
        } finally {
            if (producer != null) {
                try {
                    producer.close();
                } catch (Exception e) {
                    LOGGER.warn("Error closing KafkaProducer: {}", e.getMessage());
                }
            }
        }
    }

    public static void deleteTopic(String topic) {
        AppKafkaMessageTableClient.getInstance().cancelSubscribe();
        String brokerUrl = BrokerConfig.getInstance().getBrokerUrl();
        Properties props = new Properties();
        props.put("bootstrap.servers", brokerUrl);
        try (AdminClient adminClient = AdminClient.create(props)) {
            adminClient.deleteTopics(Collections.singletonList(topic)).all().get();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void createTopic(String topic) {
        String brokerUrl = BrokerConfig.getInstance().getBrokerUrl();
        Properties props = new Properties();
        props.put("bootstrap.servers", brokerUrl);
        try (AdminClient adminClient = AdminClient.create(props)) {
            adminClient.createTopics(Collections.singletonList(new NewTopic(topic, 1, (short) 1)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void createTopic(CreateTopicDialogParams params) {
        String brokerUrl = BrokerConfig.getInstance().getBrokerUrl();
        Properties props = new Properties();
        props.put("bootstrap.servers", brokerUrl);
        try (AdminClient adminClient = AdminClient.create(props)) {
            Map<String, String> topicConfig = Map.of(
                    TopicConfig.CLEANUP_POLICY_CONFIG, getCleanupPolicy(params.cleanupPolicy()),
                    TopicConfig.RETENTION_MS_CONFIG, String.valueOf(params.retention())
            );

            NewTopic newTopic = new NewTopic(
                    params.topicName(),
                    params.partitions(),
                    (short) params.replication()
            );
            newTopic.configs(topicConfig);
            adminClient.createTopics(Collections.singletonList(newTopic)).all().get();
        } catch (Exception e) {
            if (e.getMessage().contains("Invalid replication factor")) {
                ErrorModal.showError(e.getMessage());
            } else {
                ErrorModal.showError(e.getMessage());
            }
        }
    }

    private static String getCleanupPolicy(String s) {
        if (Objects.equals(s, "delete")) return TopicConfig.CLEANUP_POLICY_DELETE;
        else if (Objects.equals(s, "compact")) return TopicConfig.CLEANUP_POLICY_COMPACT;
        else if (Objects.equals(s, "delete+compact")) return "compact+delete";
        return "delete";
    }

    public static Map<String, TopicDescription> describeTopic(String topic) {
        String brokerUrl = BrokerConfig.getInstance().getBrokerUrl();
        Properties props = new Properties();
        props.put("bootstrap.servers", brokerUrl);
        try (AdminClient adminClient = AdminClient.create(props)) {
            return adminClient.describeTopics(Collections.singleton(topic)).all().get();
        } catch (Exception e) {
            ErrorModal.showError("Error describing selectedNode: " + e.getMessage());
        }
        return null;
    }

    public static Collection<ConfigEntry> describeTopic2(String topic) {
        String brokerUrl = BrokerConfig.getInstance().getBrokerUrl();
        Properties props = new Properties();
        props.put("bootstrap.servers", brokerUrl);
        try (AdminClient admin = AdminClient.create(props)) {
            ConfigResource resource = new ConfigResource(ConfigResource.Type.TOPIC, topic);
            DescribeConfigsResult result = admin.describeConfigs(Collections.singleton(resource));
            Config config = result.all().get().get(resource);
            return config.entries();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void updateTopicConfig(String topic, List<ConfigEntry> configEntry) {
        Properties props = new Properties();
        props.put("bootstrap.servers", BrokerConfig.getInstance().getBrokerUrl());
        try (AdminClient adminClient = AdminClient.create(props)) {
            ConfigResource resource = new ConfigResource(ConfigResource.Type.TOPIC, topic);

            Map<ConfigResource, Collection<AlterConfigOp>> changes = new HashMap<>();
            List<AlterConfigOp> ops = new ArrayList<>();
            for (ConfigEntry entry : configEntry) {
                AlterConfigOp op = new AlterConfigOp(
                        new ConfigEntry(entry.name(), entry.value()),
                        AlterConfigOp.OpType.SET
                );
                ops.add(op);

            }
            changes.put(resource, ops);

            adminClient.incrementalAlterConfigs(changes).all().get();

        } catch (Exception e) {
            ErrorModal.showError("Error: " + e.getMessage());
        }
    }

    public static long getNumRecords(String selectedTopic) {
        Properties props = new Properties();
        props.put("bootstrap.servers", BrokerConfig.getInstance().getBrokerUrl());
        try (AdminClient admin = AdminClient.create(props)) {

            String topic = selectedTopic;
            var topicDesc = admin.describeTopics(List.of(topic))
                    .all().get().get(topic);

            List<TopicPartition> partitions = new ArrayList<>();
            for (TopicPartitionInfo p : topicDesc.partitions()) {
                partitions.add(new TopicPartition(topic, p.partition()));
            }

            long total = 0;
            for (TopicPartition tp : partitions) {

                ListOffsetsResult begin = admin.listOffsets(Map.of(tp, OffsetSpec.earliest()));
                ListOffsetsResult listOffsetsResult = admin.listOffsets(Map.of(tp, OffsetSpec.latest()));
                long earliest = begin.all().get().get(tp).offset();
                long latest = listOffsetsResult.all().get().get(tp).offset();

                total += (latest - earliest);
            }

            return total;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static String getSchemaExample(String selectedNode) {
        String schemaForTopic = getSchemaForTopic(selectedNode);
        return transformAvroSchemaToExampleJson(schemaForTopic);
    }

    public static String getSchemaForTopic(String topic) {
        SchemaRegistryClient cli = new CachedSchemaRegistryClient("http://localhost:8081", 100);
        try {
            SchemaMetadata data = cli.getLatestSchemaMetadata(topic + "-value");

            String schema = data.getSchema();
            return schema;
        } catch (RestClientException e) {
            // NOOP: Topic does not have schema
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static String transformAvroSchemaToExampleJson(String avroSchema) {

        Schema schema = new Schema.Parser().parse(avroSchema);
        try {

            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(generateExampleJson(schema));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "N/A";
    }


    public static JsonNode generateExampleJson(Schema schema) {
        switch (schema.getType()) {

            case RECORD:
                ObjectNode recordNode = mapper.createObjectNode();
                for (Schema.Field field : schema.getFields()) {
                    recordNode.set(field.name(), generateExampleJson(field.schema()));
                }
                return recordNode;

            case STRING:
                return mapper.getNodeFactory().textNode("exampleString");

            case INT:
                return mapper.getNodeFactory().numberNode(42);

            case LONG:
                return mapper.getNodeFactory().numberNode(1234567890123L);

            case FLOAT:
                return mapper.getNodeFactory().numberNode(3.14f);

            case DOUBLE:
                return mapper.getNodeFactory().numberNode(3.14159);

            case BOOLEAN:
                return mapper.getNodeFactory().booleanNode(true);

            case ENUM:
                return mapper.getNodeFactory().textNode(schema.getEnumSymbols().get(0));

            case ARRAY:
                return mapper.createArrayNode().add(generateExampleJson(schema.getElementType()));

            case MAP:
                ObjectNode mapNode = mapper.createObjectNode();
                mapNode.set("key1", generateExampleJson(schema.getValueType()));
                return mapNode;

            case UNION:
                // pick first non-null type
                for (Schema s : schema.getTypes()) {
                    if (s.getType() != Schema.Type.NULL) {
                        return generateExampleJson(s);
                    }
                }
                return mapper.nullNode();

            case NULL:
                return mapper.nullNode();

            default:
                return mapper.getNodeFactory().textNode("unsupportedType");
        }
    }

    public static void connectToKafkaAndPopulateTree(BrokerDialogSettings cb) {
        connectToKafkaAndPopulateTree(cb.broker(), cb.schemaReg());
    }

    public static String getAvroSchemaForTopic(String topic) {
        SchemaRegistryClient client =
                new io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient(BrokerConfig.getInstance().getSchemaRegistryUrl(), 10);
        SchemaMetadata metadata = null;
        try {
            metadata = client.getLatestSchemaMetadata(topic + "-value");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return metadata.getSchema();
    }


    /**
     * Sets the Avro schema for a topic in the Schema Registry.
     *
     * @param topic      The topic name (without -value suffix)
     * @param avroSchema The Avro schema as a String
     */
    public static void setSchemaForTopic(String topic, String avroSchema) {
        String schemaRegistryUrl = BrokerConfig.getInstance().getSchemaRegistryUrl();
        SchemaRegistryClient client = new CachedSchemaRegistryClient(schemaRegistryUrl, 10);
        try {
            // Register the schema for topic-value subject
            int id = client.register(topic + "-value", new Schema.Parser().parse(avroSchema));
            LOGGER.info("Registered schema for {}-value with id {}", topic, id);
        } catch (Exception e) {
            LOGGER.error("Error registering schema for topic {}: {}", topic, e.getMessage());
            ErrorModal.showError("Error registering schema: " + e.getMessage());
        }
    }
}
