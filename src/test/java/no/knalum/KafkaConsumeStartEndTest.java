package no.knalum;

import com.fasterxml.jackson.databind.deser.std.StringDeserializer;
import no.knalum.config.BrokerConfig;
import no.knalum.kafka.CustomValueDeserializer;
import no.knalum.ui.rightview.messagetable.SearchFilter;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.*;

import static no.knalum.kafka.AppKafkaMessageTableClient.recordMatchesFilter;

@Disabled
public class KafkaConsumeStartEndTest {
    @Test
    void name() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, CustomValueDeserializer.class);
        props.put("schema.registry.url", "http://schema-registry:8081");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

        KafkaConsumer consumer = new KafkaConsumer<>(props);
        TopicPartition tp = new TopicPartition("f40b", 0);
        consumer.assign(Collections.singletonList(tp));

        //consumer.seek(tp,0); // Seek to beginning

        consumer.seekToEnd(Collections.singletonList(tp));
        long latestOffset = consumer.position(tp);
        consumer.seek(tp, latestOffset - 20);


        while (true) {

            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            records.forEach(record -> {
                System.out.println(record.offset());
            });
        }
    }

    @Test
    void consumeFromBack() {
        BrokerConfig.getInstance().setUrl("localhost:29092");
        BrokerConfig.getInstance().setSchemaRegistryUrl("http://schema-registry:8081");
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, CustomValueDeserializer.class);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "paging-consumer");
        props.put("schema.registry.url", "http://schema-registry:8081");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 500);

        String topic = "3159";

        List<ConsumerRecord<String, Object>> result = new ArrayList<>();

        try (KafkaConsumer<String, Object> consumer = new KafkaConsumer<>(props)) {
            List<PartitionInfo> partitionInfos = consumer.partitionsFor(topic);
            List<TopicPartition> partitions = new ArrayList<>();
            for (PartitionInfo p : partitionInfos) {
                partitions.add(new TopicPartition(topic, p.partition()));
            }
            consumer.assign(partitions);
            long currentPage = 0;
            for (TopicPartition tp : partitions) {
                consumer.seek(tp, currentPage * 100L);
            }
            SearchFilter searchFilter = new SearchFilter();
            searchFilter.setKey("asd");

            boolean done = false;
            Map<TopicPartition, Long> endOffsets = consumer.endOffsets(partitions);
            while (!done) {
                ConsumerRecords<String, Object> records = consumer.poll(0);
                for (ConsumerRecord<String, Object> record : records) {
                    if (recordMatchesFilter(record, searchFilter)) {
                        result.add(record);
                    }
                }
                consumer.commitSync();

                done = true;
                for (TopicPartition tp : partitions) {
                    long position = consumer.position(tp);
                    long end = endOffsets.get(tp);
                    if (position < end) {
                        done = false;
                        break;
                    }
                }
            }

            System.out.println(result.size());
        }
    }

    @Test
    void name2() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);

        String topic = "my-selectedNode";
        int partition = 0;
        long startOffset = 12345L;   // <- YOUR START OFFSET

        TopicPartition tp = new TopicPartition(topic, partition);

        // 1️⃣ Assign the partition manually
        consumer.assign(Collections.singletonList(tp));

        // 2️⃣ Seek to the offset you want
        consumer.seek(tp, startOffset);

        // 3️⃣ Start polling (reads from startOffset → forward)
        while (true) {
            ConsumerRecords<String, String> records =
                    consumer.poll(Duration.ofMillis(200));

            for (ConsumerRecord<String, String> rec : records) {
                System.out.println(
                        "offset=" + rec.offset() +
                                ", value=" + rec.value());
            }
        }
    }
}
