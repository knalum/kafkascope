package no.knalum;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.util.Set;


@Disabled
public class KafkaJmxTest {
    @Test
    void name() throws Exception {
        String jmxUrl = "service:jmx:rmi:///jndi/rmi://localhost:9999/jmxrmi";
        String topic = "f40b";

        JMXServiceURL url = new JMXServiceURL(jmxUrl);
        JMXConnector jmxc = JMXConnectorFactory.connect(url, null);
        MBeanServerConnection mbeanConn = jmxc.getMBeanServerConnection();

        // Query all log size MBeans for this selectedNode
        String query = String.format("kafka.log:type=Log,name=Size,selectedNode=%s,*", topic);
        Set<ObjectName> mbeans = mbeanConn.queryNames(new ObjectName(query), null);

        long totalSize = 0;
        for (ObjectName mbean : mbeans) {
            Long size = (Long) mbeanConn.getAttribute(mbean, "Value");
            String partition = mbean.getKeyProperty("partition");
            System.out.printf("Partition %s size: %d bytes%n", partition, size);
            totalSize += size;
        }

        System.out.println("Total selectedNode size: " + totalSize + " bytes");

        jmxc.close();
    }


}
