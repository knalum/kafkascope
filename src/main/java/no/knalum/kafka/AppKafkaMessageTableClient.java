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
import java.util.stream.Collectors;

public class AppKafkaMessageTableClient {
    boolean isSubscribing = false;
    private Thread consumerThread;

    private static final int PAGE_SIZE = 100;

    private static AppKafkaMessageTableClient instance;

    private Properties getConsumerProps() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BrokerConfig.getInstance().getBrokerUrl());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, CustomValueDeserializer.class);
        props.put("schema.registry.url", BrokerConfig.getInstance().getSchemaRegistryUrl());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100);
        return props;
    }

    public static AppKafkaMessageTableClient getInstance() {
        if (instance == null) {
            instance = new AppKafkaMessageTableClient();
        }
        return instance;
    }

    public List<ConsumerRecord<String, Object>> getRecords(String topic, SortPane.SortType sortType, int currentPage, SearchFilter searchFilter) {
        Properties consumerProps = getConsumerProps();

        try (KafkaConsumer<String, Object> consumer = new KafkaConsumer<>(consumerProps)) {
            // Get all partitions for the topic
            List<PartitionInfo> partitionInfos = consumer.partitionsFor(topic);
            if (partitionInfos == null || partitionInfos.isEmpty()) {
                return Collections.emptyList();
            }

            List<TopicPartition> topicPartitions = partitionInfos.stream()
                    .filter(partition->{
                        if(searchFilter.getPartition()!=null && !searchFilter.getPartition().isBlank() && !searchFilter.getPartition().equals("All")){
                            return partition.partition() == Integer.parseInt(searchFilter.getPartition());
                        }
                        return true;
                    })
                    .map(pi -> new TopicPartition(topic, pi.partition()))
                    .collect(Collectors.toList());

            // Assign all partitions to the consumer
            consumer.assign(topicPartitions);

            // Get beginning and end offsets for all partitions
            Map<TopicPartition, Long> beginningOffsets = consumer.beginningOffsets(topicPartitions);
            Map<TopicPartition, Long> endOffsets = consumer.endOffsets(topicPartitions);

            // Calculate total records to fetch based on pagination
            int skipCount = currentPage * PAGE_SIZE;
            int maxRecordsToFetch = skipCount + PAGE_SIZE;

            List<ConsumerRecord<String, Object>> allRecords;
            if (sortType == SortPane.SortType.Newest || sortType == SortPane.SortType.Tail) {
                // For newest/tail, start from end and go backwards
                allRecords = fetchRecordsFromEnd(consumer, topicPartitions, beginningOffsets, endOffsets, maxRecordsToFetch, searchFilter);
                allRecords.sort((r1, r2) -> Long.compare(r2.timestamp(), r1.timestamp()));
            } else {
                // For oldest, start from beginning
                allRecords = fetchRecordsFromBeginning(consumer, topicPartitions, beginningOffsets, endOffsets, maxRecordsToFetch, searchFilter);
                allRecords.sort(Comparator.comparingLong(ConsumerRecord::timestamp));
            }

            // Apply pagination: skip records and take PAGE_SIZE
            if (skipCount >= allRecords.size()) {
                return Collections.emptyList();
            }

            int endIndex = Math.min(skipCount + PAGE_SIZE, allRecords.size());
            return new ArrayList<>(allRecords.subList(skipCount, endIndex));

        } catch (KafkaException e) {
            System.err.println("Error fetching records from Kafka: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    private List<ConsumerRecord<String, Object>> fetchRecordsFromEnd(
            KafkaConsumer<String, Object> consumer,
            List<TopicPartition> topicPartitions,
            Map<TopicPartition, Long> beginningOffsets,
            Map<TopicPartition, Long> endOffsets,
            int maxRecords,
            SearchFilter searchFilter) {

        List<ConsumerRecord<String, Object>> records = new ArrayList<>();

        for (TopicPartition tp : topicPartitions) {
            long endOffset = endOffsets.get(tp);
            long beginningOffset = beginningOffsets.get(tp);

            if (endOffset <= beginningOffset) {
                continue; // No records in this partition
            }

            // Start from maxRecords before the end, or from beginning if fewer records exist
            long startOffset = Math.max(beginningOffset, endOffset - maxRecords);
            consumer.seek(tp, startOffset);

            // Poll records from this partition
            while (records.size() < maxRecords) {
                ConsumerRecords<String, Object> polledRecords = consumer.poll(Duration.ofMillis(1000));
                if (polledRecords.isEmpty()) {
                    break;
                }

                for (ConsumerRecord<String, Object> record : polledRecords.records(tp)) {
                    if (record.offset() >= endOffset) {
                        break;
                    }
                    if (matchesFilter(record, searchFilter)) {
                        records.add(record);
                    }
                }

                if (consumer.position(tp) >= endOffset) {
                    break;
                }
            }
        }

        return records;
    }

    private List<ConsumerRecord<String, Object>> fetchRecordsFromBeginning(
            KafkaConsumer<String, Object> consumer,
            List<TopicPartition> topicPartitions,
            Map<TopicPartition, Long> beginningOffsets,
            Map<TopicPartition, Long> endOffsets,
            int maxRecords,
            SearchFilter searchFilter) {

        List<ConsumerRecord<String, Object>> records = new ArrayList<>();

        // Seek to beginning of all partitions
        for (TopicPartition tp : topicPartitions) {
            consumer.seek(tp, beginningOffsets.get(tp));
        }

        // Poll records until we have enough or there are no more
        int emptyPollCount = 0;
        while (records.size() < maxRecords && emptyPollCount < 3) {
            ConsumerRecords<String, Object> polledRecords = consumer.poll(Duration.ofMillis(1000));

            if (polledRecords.isEmpty()) {
                emptyPollCount++;
                continue;
            }

            emptyPollCount = 0;

            for (ConsumerRecord<String, Object> record : polledRecords) {
                TopicPartition tp = new TopicPartition(record.topic(), record.partition());
                if (record.offset() >= endOffsets.get(tp)) {
                    continue;
                }
                if (matchesFilter(record, searchFilter)) {
                    records.add(record);
                    if (records.size() >= maxRecords) {
                        break;
                    }
                }
            }

            // Check if all partitions have reached their end
            boolean allDone = topicPartitions.stream()
                    .allMatch(tp -> consumer.position(tp) >= endOffsets.get(tp));
            if (allDone) {
                break;
            }
        }

        return records;
    }

    private boolean matchesFilter(ConsumerRecord<String, Object> record, SearchFilter searchFilter) {
        if (searchFilter == null) {
            return true;
        }

        String key = record.key();
        Object value = record.value();

        // Check key filter
        if (searchFilter.getKey() != null && !searchFilter.getKey().isEmpty()) {
            if (key == null || !key.toLowerCase().contains(searchFilter.getKey().toLowerCase())) {
                return false;
            }
        }

        // Check value filter
        if (searchFilter.getValue() != null && !searchFilter.getValue().isEmpty()) {
            if (value == null || !value.toString().toLowerCase().contains(searchFilter.getValue().toLowerCase())) {
                return false;
            }
        }

        return true;
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
            Properties consumerProps = getConsumerProps();
            consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, UUID.randomUUID().toString());
            try (KafkaConsumer<String, Object> consumer = new KafkaConsumer<>(consumerProps)) {
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

    public List<Integer> getNumberOfPartitions(String string) {
        Properties consumerProps = getConsumerProps();
        try (KafkaConsumer<String, Object> consumer = new KafkaConsumer<>(consumerProps)) {
            List<PartitionInfo> partitionInfos = consumer.partitionsFor(string);
            return  partitionInfos.stream().map(p->p.partition()).collect(Collectors.toList());

        } catch (KafkaException e) {
            System.err.println("Error fetching partitions: " + e.getMessage());
        }
        return null;
    }
}
