package no.knalum.kafka;

import no.knalum.config.BrokerConfig;
import no.knalum.ui.rightview.messagetable.SearchFilter;
import no.knalum.ui.rightview.messagetable.SortPane;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;

import java.time.Duration;
import java.util.*;
import java.util.function.Consumer;

public class AppKafkaMessageTableClient {
    static Properties props = new Properties();
    boolean isSubscribing = false;
    private Thread consumerThread;

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

    public List<ConsumerRecord<String, Object>> getRecords(String topic, SortPane.SortType sortChoice, int currentPage, SearchFilter searchFilter) {
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
                // Newest
                consumer.seekToEnd(partitions);
                for (TopicPartition tp : partitions) {
                    long latestOffset = consumer.position(tp);
                    long max = Math.max(0, latestOffset - (currentPage + 1) * 100);
                    consumer.seek(tp, max);
                }
            }

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

            return result;
        } catch (RuntimeException e) {
            return result;
        }
    }

    public static boolean recordMatchesFilter(ConsumerRecord<String, Object> record, SearchFilter searchFilter) {
        Object key = record.key();
        Object value = record.value();
        if (key == null || value == null) {
            return false;
        }

        boolean out = searchFilter.getValue() == null || value.toString().toLowerCase().contains(searchFilter.getValue().toLowerCase());
        if (searchFilter.getKey() != null && !key.toString().toLowerCase().contains(searchFilter.getKey().toLowerCase())) {
            out = false;
        }
        return out;
    }

    public void cancelSubscribe() {
        if (consumerThread != null && consumerThread.isAlive()) {
            consumerThread.interrupt();
        }
    }

    public void subscribe(String selectedTopic, Consumer<ConsumerRecords<String, Object>> o) {
        cancelSubscribe();
        consumerThread = new Thread(() -> {
            Properties threadProps = new Properties();
            threadProps.putAll(props);
            threadProps.put(ConsumerConfig.GROUP_ID_CONFIG, UUID.randomUUID().toString());
            try (KafkaConsumer<String, Object> consumer = new KafkaConsumer<>(threadProps)) {
                isSubscribing = true;
                consumer.subscribe(List.of(selectedTopic));
                boolean firstPoll = true;
                while (!Thread.currentThread().isInterrupted()) {
                    ConsumerRecords<String, Object> records = consumer.poll(Duration.ofMillis(500));
                    if (firstPoll) {
                        consumer.assignment().forEach(tp -> consumer.seekToEnd(List.of(tp)));
                        firstPoll = false;
                        continue;
                    }
                    if (!records.isEmpty()) {
                        o.accept(records);
                    }
                }
            } catch (KafkaException e) {
                //e.printStackTrace();
            }
        }, "KafkaTailConsumerThread-" + selectedTopic);
        consumerThread.start();
    }
}
