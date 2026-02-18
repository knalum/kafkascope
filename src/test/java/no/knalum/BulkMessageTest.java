package no.knalum;

import com.github.javafaker.Faker;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

@Disabled
public class BulkMessageTest {

    @Test
    void sendBulk() throws ExecutionException, InterruptedException {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        KafkaProducer<String, String> prod = new KafkaProducer<>(props);
        Faker faker = new Faker();
        for (int i = 0; i < 100; i++) {
            String randomWord = faker.lorem().word();
            prod.send(new ProducerRecord<>("qwe", "key_"+randomWord, randomWord)).get();
            Thread.sleep((long) (Math.random() * 200));
            System.out.println(randomWord);
        }
    }

}
