package no.knalum;

import no.knalum.config.BrokerConfig;
import no.knalum.kafka.AppKafkaMessageTableClient;
import no.knalum.ui.rightview.messagetable.SearchFilter;
import no.knalum.ui.rightview.messagetable.SortPane;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
public class AppKafkaMessageTableClientTest {

    private static final String TEST_TOPIC = "test-topic";

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"));

    private AppKafkaMessageTableClient client;

    @BeforeAll
    static void setupKafka() throws ExecutionException, InterruptedException {
        // Configure BrokerConfig to use the embedded Kafka
        BrokerConfig.getInstance().setUrl(kafka.getBootstrapServers());
        BrokerConfig.getInstance().setSchemaRegistryUrl("http://localhost:8081"); // Not used in this test

        System.out.println("Connecting to broker " + kafka.getBootstrapServers());
        // Create test topic
        Properties adminProps = new Properties();
        adminProps.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        try (AdminClient adminClient = AdminClient.create(adminProps)) {
            NewTopic newTopic = new NewTopic(TEST_TOPIC, 3, (short) 1); // 3 partitions
            adminClient.createTopics(Collections.singletonList(newTopic)).all().get();
        }

        // Produce test messages
        produceTestMessages();
    }

    @BeforeEach
    void setUp() {
        client = AppKafkaMessageTableClient.getInstance();
    }

    private static void produceTestMessages() {
        Properties producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(producerProps)) {
            // Produce 250 messages to test pagination
            for (int i = 0; i < 250; i++) {
                String key = "key-" + i;
                String value = "message-" + i;
                producer.send(new ProducerRecord<>(TEST_TOPIC, key, value));
            }
            producer.flush();
        }
    }

    @Test
    void testGetRecords_ReturnsRecords() {
        SearchFilter filter = new SearchFilter();

        List<ConsumerRecord<String, Object>> records = client.getRecords(
                TEST_TOPIC,
                SortPane.SortType.Oldest,
                0,
                filter
        );

        assertNotNull(records);
        assertFalse(records.isEmpty());
        assertTrue(records.size() <= 100); // PAGE_SIZE is 100
    }

    @Test
    void testGetRecords_Pagination_FirstPage() {
        SearchFilter filter = new SearchFilter();

        List<ConsumerRecord<String, Object>> page0 = client.getRecords(
                TEST_TOPIC,
                SortPane.SortType.Oldest,
                0,
                filter
        );

        assertEquals(100, page0.size());
    }

    @Test
    void testGetRecords_Pagination_SecondPage() {
        SearchFilter filter = new SearchFilter();

        List<ConsumerRecord<String, Object>> page1 = client.getRecords(
                TEST_TOPIC,
                SortPane.SortType.Oldest,
                1,
                filter
        );

        assertEquals(100, page1.size());
    }

    @Test
    void testGetRecords_Pagination_ThirdPage() {
        SearchFilter filter = new SearchFilter();

        List<ConsumerRecord<String, Object>> page2 = client.getRecords(
                TEST_TOPIC,
                SortPane.SortType.Oldest,
                2,
                filter
        );

        // Should have 50 records (250 total - 200 from first 2 pages)
        assertEquals(50, page2.size());
    }

    @Test
    void testGetRecords_SortNewest() {
        SearchFilter filter = new SearchFilter();

        List<ConsumerRecord<String, Object>> records = client.getRecords(
                TEST_TOPIC,
                SortPane.SortType.Newest,
                0,
                filter
        );

        assertNotNull(records);
        assertFalse(records.isEmpty());

        // Verify descending order by timestamp
        for (int i = 0; i < records.size() - 1; i++) {
            assertTrue(records.get(i).timestamp() >= records.get(i + 1).timestamp(),
                    "Records should be sorted by timestamp descending");
        }
    }

    @Test
    void testGetRecords_SortOldest() {
        SearchFilter filter = new SearchFilter();

        List<ConsumerRecord<String, Object>> records = client.getRecords(
                TEST_TOPIC,
                SortPane.SortType.Oldest,
                0,
                filter
        );

        assertNotNull(records);
        assertFalse(records.isEmpty());

        // Verify ascending order by timestamp
        for (int i = 0; i < records.size() - 1; i++) {
            assertTrue(records.get(i).timestamp() <= records.get(i + 1).timestamp(),
                    "Records should be sorted by timestamp ascending");
        }
    }

    @Test
    void testGetRecords_WithKeyFilter() {
        SearchFilter filter = new SearchFilter();
        filter.setKey("key-1"); // Should match key-1, key-10, key-11, ..., key-19, key-100, etc.

        List<ConsumerRecord<String, Object>> records = client.getRecords(
                TEST_TOPIC,
                SortPane.SortType.Oldest,
                0,
                filter
        );

        assertNotNull(records);
        assertFalse(records.isEmpty());

        // Verify all records match the key filter
        for (ConsumerRecord<String, Object> record : records) {
            assertTrue(record.key().toLowerCase().contains("key-1"),
                    "All records should have key containing 'key-1'");
        }
    }

    @Test
    void testGetRecords_WithValueFilter() {
        SearchFilter filter = new SearchFilter();
        filter.setValue("message-5"); // Should match message-5, message-50, message-51, etc.

        List<ConsumerRecord<String, Object>> records = client.getRecords(
                TEST_TOPIC,
                SortPane.SortType.Oldest,
                0,
                filter
        );

        assertNotNull(records);
        assertFalse(records.isEmpty());

        // Verify all records match the value filter
        for (ConsumerRecord<String, Object> record : records) {
            assertTrue(record.value().toString().toLowerCase().contains("message-5"),
                    "All records should have value containing 'message-5'");
        }
    }

    @Test
    void testGetRecords_EmptyTopic() {
        SearchFilter filter = new SearchFilter();

        List<ConsumerRecord<String, Object>> records = client.getRecords(
                "non-existent-topic",
                SortPane.SortType.Oldest,
                0,
                filter
        );

        assertTrue(records.isEmpty());
    }

    @Test
    void testGetRecords_PageBeyondData() {
        SearchFilter filter = new SearchFilter();

        List<ConsumerRecord<String, Object>> records = client.getRecords(
                TEST_TOPIC,
                SortPane.SortType.Oldest,
                100, // Way beyond available data
                filter
        );

        assertTrue(records.isEmpty());
    }
}
