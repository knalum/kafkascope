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
        String topic = "key-topic-2";

        JMXServiceURL url = new JMXServiceURL(jmxUrl);
        JMXConnector jmxc = JMXConnectorFactory.connect(url, null);
        MBeanServerConnection mbeanConn = jmxc.getMBeanServerConnection();
        // Query all MBeans
        Set<ObjectName> mbeans = mbeanConn.queryNames(null, null);
        for (ObjectName name : mbeans) {
            if (name.toString().contains("LogEndOffset")) {

                System.out.println(name);
            }
        }
        jmxc.close();
    }

    @Test
    void getJmxAttributeForMbeanName() throws Exception {
        String jmxUrl = "service:jmx:rmi:///jndi/rmi://localhost:9999/jmxrmi";
        String mBeanName = "kafka.log:type=Log,name=LogEndOffset,topic=key-topic-2,partition=0";
        JMXServiceURL url = new JMXServiceURL(jmxUrl);
        JMXConnector jmxc = JMXConnectorFactory.connect(url, null);
        MBeanServerConnection mbeanConn = jmxc.getMBeanServerConnection();
        ObjectName objectName = new ObjectName(mBeanName);
        Object value = mbeanConn.getAttribute(objectName, "Value");
        System.out.println("Value for " + mBeanName + ": " + value);
        jmxc.close();
    }
}
