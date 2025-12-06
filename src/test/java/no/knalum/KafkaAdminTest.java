package no.knalum;

import no.knalum.config.ConfigSaver;
import no.knalum.kafka.AppKafkaClient;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.DeleteTopicsResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

@Disabled
public class KafkaAdminTest {
    @BeforeEach
    void setUp() {
        ConfigSaver.loadConfig();
    }

    @Test
    void name() {
        AppKafkaClient.deleteTopic("d8717580");
    }

    @Test
    void testDel() throws ExecutionException, InterruptedException {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");

        try (AdminClient admin = AdminClient.create(props)) {
            DeleteTopicsResult result =
                    admin.deleteTopics(Collections.singletonList("3650"));

            // Wait for operation to complete
            result.all().get();
        }
    }
}
