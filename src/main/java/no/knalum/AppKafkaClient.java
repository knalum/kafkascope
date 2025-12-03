package no.knalum;

import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.TopicPartitionInfo;
import org.apache.kafka.common.config.ConfigResource;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutionException;

public class AppKafkaClient {

    private boolean isSubscribing;
    private static final Logger LOGGER = LoggerFactory.getLogger(AppKafkaClient.class);

    public static Set<String> connect(BrokerConfig instance) throws ExecutionException, InterruptedException {
        String brokerUrl = instance.getBrokerUrl();
        java.util.Properties props = new java.util.Properties();
        props.put("bootstrap.servers", brokerUrl);
        props.put("bootstrap.servers", brokerUrl);
        AdminClient client = AdminClient.create(props);
        return client.listTopics().names().get();
    }

    public static void connectToKafkaAndPopulateTree() {
        try {
            Set<String> topics = AppKafkaClient.connect(BrokerConfig.getInstance());
            MessageBus.getInstance().publish(new ConnectedToBrokerMessage(BrokerConfig.getInstance().getBrokerUrl(), topics));
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("Error connect to kafka and populate tree: {}", e.getMessage());
            ErrorModal.showError("Error connecting to Kafka: " + e.getMessage());
        }
    }


    public static void sendMessageToBroker(String topic, String key, String value) {
        String broker = BrokerConfig.getInstance().getBrokerUrl();

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, broker);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        KafkaProducer<String, String> producer = new KafkaProducer<>(props);
        producer.send(new ProducerRecord<>(topic, key, value));
        producer.close();
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

    public static void describeTopic(String topic) {
        String brokerUrl = BrokerConfig.getInstance().getBrokerUrl();
        Properties props = new Properties();
        props.put("bootstrap.servers", brokerUrl);
        try (AdminClient adminClient = AdminClient.create(props)) {
            var descriptions = adminClient.describeTopics(Collections.singleton(topic)).all().get();
            var desc = descriptions.get(topic);
            StringBuilder sb = new StringBuilder();
            sb.append("Topic: ").append(topic).append("\n");
            sb.append("Partitions: ").append(desc.partitions().size()).append("\n");
            desc.partitions().forEach(p -> {
                sb.append("Partition ").append(p.partition()).append(": leader=").append(p.leader()).append(", replicas=").append(p.replicas()).append(", isr=").append(p.isr()).append("\n");
            });
            ErrorModal.showInfo(sb.toString()); // Reuse modal for info
        } catch (Exception e) {
            ErrorModal.showError("Error describing selectedNode: " + e.getMessage());
        }
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
}
