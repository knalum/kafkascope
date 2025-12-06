package no.knalum.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.util.HashSet;
import java.util.Set;

public class KafkaStatsClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaStatsClient.class);

    public record TopicStats(Long size, Long count, Integer numPartitions) {
    }


    public TopicStats getTopicStats(String topicName) throws Exception {
        long topicSize = getTopicSize(topicName);
        long topicCount = getTopicCount(topicName);
        int numberOfPartitions = getNumberOfPartitions(topicName);

        return new TopicStats(topicSize, topicCount, numberOfPartitions);
    }

    private int getNumberOfPartitions(String topicName) {
        String jmxUrl = "service:jmx:rmi:///jndi/rmi://localhost:9999/jmxrmi";
        String pattern = "kafka.log:type=Log,name=LogEndOffset,topic=" + topicName + ",partition=*";
        Set<Integer> partitions = new HashSet<>();
        try {
            JMXServiceURL url = new JMXServiceURL(jmxUrl);
            JMXConnector jmxc = JMXConnectorFactory.connect(url, null);
            MBeanServerConnection mbeanConn = jmxc.getMBeanServerConnection();
            Set<ObjectName> mbeans = mbeanConn.queryNames(new ObjectName(pattern), null);
            for (ObjectName mbean : mbeans) {
                String key = mbean.getKeyProperty("partition");
                if (key != null) {
                    try {
                        partitions.add(Integer.parseInt(key));
                    } catch (NumberFormatException ignore) {
                    }
                }
            }
            jmxc.close();
        } catch (Exception ex) {
            LOGGER.error("Failed to get number of partitions for topic {}: {}", topicName, ex.getMessage());
            return -1;
        }
        return partitions.size();
    }

    private long getTopicCount(String topicName) {
        long sum = 0L;
        String jmxUrl = "service:jmx:rmi:///jndi/rmi://localhost:9999/jmxrmi";
        String pattern = "kafka.log:type=Log,name=LogEndOffset,topic=" + topicName + ",partition=*";
        try {
            JMXServiceURL url = new JMXServiceURL(jmxUrl);
            JMXConnector jmxc = JMXConnectorFactory.connect(url, null);
            MBeanServerConnection mbeanConn = jmxc.getMBeanServerConnection();
            Set<ObjectName> mbeans = mbeanConn.queryNames(new ObjectName(pattern), null);
            for (ObjectName mbean : mbeans) {
                Object valueObj = mbeanConn.getAttribute(mbean, "Value");
                if (valueObj instanceof Long) {
                    sum += (Long) valueObj;
                } else if (valueObj instanceof Integer) {
                    sum += ((Integer) valueObj).longValue();
                }
            }
            jmxc.close();
        } catch (Exception ex) {
            LOGGER.error("Failed to get topic count for topic {}: {}", topicName, ex.getMessage());
            return -1L;
        }
        return sum;
    }

    private long getTopicSize(String topicName) {
        long sum = 0L;
        String jmxUrl = "service:jmx:rmi:///jndi/rmi://localhost:9999/jmxrmi";
        String pattern = "kafka.log:type=Log,name=Size,topic=" + topicName + ",partition=*";
        try {
            JMXServiceURL url = new JMXServiceURL(jmxUrl);
            JMXConnector jmxc = JMXConnectorFactory.connect(url, null);
            MBeanServerConnection mbeanConn = jmxc.getMBeanServerConnection();
            Set<ObjectName> mbeans = mbeanConn.queryNames(new ObjectName(pattern), null);
            for (ObjectName mbean : mbeans) {
                Object valueObj = mbeanConn.getAttribute(mbean, "Value");
                if (valueObj instanceof Long) {
                    sum += (Long) valueObj;
                } else if (valueObj instanceof Integer) {
                    sum += ((Integer) valueObj).longValue();
                }
            }
            jmxc.close();
        } catch (Exception ex) {
            LOGGER.error("Failed to get topic size for topic {}: {}", topicName, ex.getMessage());
            return -1L;
        }
        return sum;
    }

}
