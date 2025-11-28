package no.knalum;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class KafkaStatsClient {

    record TopicStats(Long size, Long count, String lastTs, Integer numPartitions) {
    }


    public TopicStats getTopicStats(String topicName) throws Exception {
        Map<String, String> stats = getStats(Map.of(
                "size", "kafka.log:type=Log,name=Size,topic=" + topicName + ",*",
                "count", "kafka.log:type=Log,name=LogEndOffset,topic=" + topicName + ",*"
        ));

        ConsumerStats consumerStats = findTsAndNumPartitions();

        System.out.println(stats);
        System.out.println("Last: " + Instant.ofEpochMilli(consumerStats.lastTs));
        System.out.println("Num partitions: " + consumerStats.numPartitions);

        return new TopicStats(
                Long.parseLong(stats.get("size")),
                Long.parseLong(stats.get("count")),
                Instant.ofEpochMilli(consumerStats.lastTs).toString(),
                consumerStats.numPartitions
        );
    }


    record ConsumerStats(Long lastTs, Integer numPartitions) {
    }

    ConsumerStats findTsAndNumPartitions() {
        ConsumerStats stats;
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "temp");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, CustomValueDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.put("schema.registry.url", "http://localhost:8081");

        int numberOfPartitions;
        Long ts = 0L;

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            String topic = "f40b";
            List<TopicPartition> partitions = new ArrayList<>();
            for (int p = 0; p < consumer.partitionsFor(topic).size(); p++) {
                partitions.add(new TopicPartition(topic, p));
            }
            numberOfPartitions = consumer.partitionsFor(topic).size();

            consumer.assign(partitions);
            consumer.seekToEnd(partitions);
            for (TopicPartition tp : partitions) {
                long lastOffset = consumer.position(tp) - 1;
                if (lastOffset >= 0) {
                    consumer.seek(tp, lastOffset);
                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));
                    ts = records.iterator().next().timestamp();
                } else {
                    System.out.println("Partition " + tp.partition() + " is empty");
                }
            }
        }

        return new ConsumerStats(ts, numberOfPartitions);
    }

    Map<String, String> getStats(Map<String, String> metricNames) throws Exception {
        String jmxUrl = "service:jmx:rmi:///jndi/rmi://localhost:9999/jmxrmi";

        JMXServiceURL url = new JMXServiceURL(jmxUrl);
        JMXConnector jmxc = JMXConnectorFactory.connect(url, null);
        MBeanServerConnection mbeanConn = jmxc.getMBeanServerConnection();

        Map<String, String> stats = new HashMap<>();
        for (Map.Entry<String, String> metricName : metricNames.entrySet()) {
            Set<ObjectName> mbeans = mbeanConn.queryNames(new ObjectName(metricName.getValue()), null);
            for (ObjectName mbean : mbeans) {
                Long value = (Long) mbeanConn.getAttribute(mbean, "Value");
                stats.put(metricName.getKey(), value.toString());
            }
        }
        return stats;
    }
}
