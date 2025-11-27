package no.knalum;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class KafkaAdminTest {
    @BeforeEach
    void setUp() {
        ConfigSaver.loadConfig();
    }

    @Test
    void name() {
        AppKafkaClient.describeTopic2("key-value-2");
    }
}
