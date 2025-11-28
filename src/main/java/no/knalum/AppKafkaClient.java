package no.knalum;

import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.TopicPartitionInfo;
import org.apache.kafka.common.config.ConfigResource;
import org.apache.kafka.common.serialization.StringSerializer;

import javax.swing.*;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class AppKafkaClient {

    private boolean isSubscribing;

    public static Set<String> connect(BrokerConfig instance) throws ExecutionException, InterruptedException {
        String brokerUrl = instance.getUrl();
        java.util.Properties props = new java.util.Properties();
        props.put("bootstrap.servers", brokerUrl);
        AdminClient client = AdminClient.create(props);
        return client.listTopics().names().get();
    }

    public static void connectToKafkaAndPopulateTree() {
        try {
            Set<String> topics = AppKafkaClient.connect(BrokerConfig.getInstance());
            MessageBus.getInstance().publish(new ConnectedToBrokerMessage(BrokerConfig.getInstance().getUrl(), topics));
        } catch (Exception e) {
            ErrorModal.showError("Error connecting to Kafka: " + e.getMessage());
        }
    }


    public static void sendMessageToBroker(String topic, String key, String value) {
        String broker = BrokerConfig.getInstance().getUrl();

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, broker);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        KafkaProducer<String, String> producer = new KafkaProducer<>(props);
        producer.send(new ProducerRecord<>(topic, key, value));
        producer.close();
    }

    public static void deleteTopic(String topic) {
        String brokerUrl = BrokerConfig.getInstance().getUrl();
        Properties props = new Properties();
        props.put("bootstrap.servers", brokerUrl);
        try (AdminClient adminClient = AdminClient.create(props)) {
            adminClient.deleteTopics(Collections.singletonList("output3")).all().get();
            System.out.println("Deleded");


            Map<String, TopicDescription> descriptions = adminClient.describeTopics(Collections.singleton(topic)).all().get();
            TopicDescription desc = descriptions.get(topic);

            if (desc.isInternal()) {
                // Cannot delete internal topics
            }
        } catch (Exception e) {
            e.printStackTrace();
            ErrorModal.showError("Error connecting to Kafka: " + e.getMessage());
        }
    }

    public static void createTopic(String topic) {
        String brokerUrl = BrokerConfig.getInstance().getUrl();
        Properties props = new Properties();
        props.put("bootstrap.servers", brokerUrl);
        try (AdminClient adminClient = AdminClient.create(props)) {
            adminClient.createTopics(Collections.singletonList(new NewTopic(topic, 1, (short) 1)));
            System.out.println("Created topic");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void describeTopic(String topic) {
        String brokerUrl = BrokerConfig.getInstance().getUrl();
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
            ErrorModal.showError("Error describing topic: " + e.getMessage());
        }
    }

    public static Collection<ConfigEntry> describeTopic2(String topic) {
        String brokerUrl = BrokerConfig.getInstance().getUrl();
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
        props.put("bootstrap.servers", BrokerConfig.getInstance().getUrl());
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

    public static long getTopicStats(String selectedTopic) {
        Properties props = new Properties();
        props.put("bootstrap.servers", BrokerConfig.getInstance().getUrl());
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

            System.out.println("Messages in topic: " + total);
            return total;
        } catch (Exception e) {
            ErrorModal.showError("Error getting topic stats: " + e.getMessage());
        }
        return -1;
    }

    public void closeSubscribing() {
        isSubscribing = false;
    }

    public void subscribeToKafkaTopic(String broker, String topic) {
        String value_deser = "N/A";
        System.out.println("Subscribing to topic " + topic + " using " + value_deser);

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, broker);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, UUID.randomUUID().toString());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, CustomValueDeserializer.class);
        props.put("schema.registry.url", "http://localhost:8081");
        //    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, SpecificAvroSerde::class.java)
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");


        KafkaConsumer consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(topic));

        new Thread(() -> {
            try {
                isSubscribing = true;
                do {
                    ConsumerRecords<String, Object> records = consumer.poll(Duration.ofMillis(500));
                    for (ConsumerRecord record : records) {
                        String time = new SimpleDateFormat("HH:mm:ss").format(new Date(record.timestamp()));

                        SwingUtilities.invokeLater(() -> {
                            if (record.value() != null) {
                                MessageBus.getInstance().publish(new RecordConsumed(time, record.key().toString(), record.value().toString()));
                            }
                        });
                    }

                } while (isSubscribing);
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                consumer.close();
                System.out.println("Closed sub thread " + topic);
            }
        }).start();
    }
}
