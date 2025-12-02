package no.knalum;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

public class AppKafkaMessageTableClient {
    static Properties props = new Properties();

    static {
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BrokerConfig.getInstance().getBrokerUrl());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, UUID.randomUUID().toString());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, CustomValueDeserializer.class);
        props.put("schema.registry.url", BrokerConfig.getInstance().getSchemaRegistryUrl());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100);
    }

    private static AppKafkaMessageTableClient instance;


    public static AppKafkaMessageTableClient getInstance() {
        if (instance == null) {
            instance = new AppKafkaMessageTableClient();
        }
        return instance;
    }

    public List<ConsumerRecord<String, Object>> getRecords(String topic, SortPane.SortType sortChoice, int currentPage) {
        List<ConsumerRecord<String, Object>> result = new ArrayList<>();

        try (KafkaConsumer<String, Object> consumer = new KafkaConsumer<>(props)) {
            List<PartitionInfo> partitionInfos = consumer.partitionsFor(topic);
            if (partitionInfos == null || partitionInfos.isEmpty()) return result;
            List<TopicPartition> partitions = new ArrayList<>();
            for (PartitionInfo p : partitionInfos) {
                partitions.add(new TopicPartition(topic, p.partition()));
            }
            consumer.assign(partitions);
            if (sortChoice == SortPane.SortType.Oldest) {

                for (TopicPartition tp : partitions) {
                    consumer.seek(tp, currentPage * 100L);
                }

            } else {
                consumer.seekToEnd(partitions);
                for (TopicPartition tp : partitions) {
                    long latestOffset = consumer.position(tp);
                    consumer.seek(tp, Math.max(0, latestOffset - (currentPage + 1) * 100));
                }
            }

            ConsumerRecords<String, Object> records = consumer.poll(Duration.ofMillis(100));
            for (ConsumerRecord<String, Object> record : records) {
                result.add(record);
                if (result.size() >= 100) break;
            }

            return result;
        }
    }
}
