package no.knalum;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import javax.swing.*;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class AppKafkaClient {

    private   boolean isSubscribing;

    public static Set<String> connect(BrokerConfig instance) throws ExecutionException, InterruptedException {
        String brokerUrl = instance.getUrl();
        java.util.Properties props = new java.util.Properties();
        props.put("bootstrap.servers", brokerUrl);
        AdminClient client = AdminClient.create(props);
        return client.listTopics().names().get();
    }


    public static void sendMessageToBroker(String topic,String text) {
        String broker = BrokerConfig.getInstance().getUrl();

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, broker);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        KafkaProducer<String, String> producer = new KafkaProducer<>(props);
        producer.send(new ProducerRecord<>(topic, text));
        producer.close();
    }

    public static void deleteTopic(String topic) {
        String brokerUrl = BrokerConfig.getInstance().getUrl();
        Properties props = new Properties();
        props.put("bootstrap.servers", brokerUrl);
        try (AdminClient adminClient = AdminClient.create(props)) {
            adminClient.deleteTopics(Collections.singletonList("output3")).all().get();
            System.out.println("Deleded");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void createTopic(String topic) {
        String brokerUrl = BrokerConfig.getInstance().getUrl();
        Properties props = new Properties();
        props.put("bootstrap.servers", brokerUrl);
        try (AdminClient adminClient = AdminClient.create(props)) {
            adminClient.createTopics(Collections.singletonList(new NewTopic(topic, 1, (short) 1)));
            System.out.println("Created topic");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void closeSubscribing(){
        isSubscribing = false;
    }

    public  void subscribeToKafkaTopic(String broker, String topic) {
        String value_deser="N/A";
        System.out.println("Subscribing to topic " + topic+" using "+value_deser);

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, broker);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, UUID.randomUUID().toString());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,  CustomValueDeserializer.class);
        props.put("schema.registry.url", "http://localhost:8081");
        //    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, SpecificAvroSerde::class.java)
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");


        KafkaConsumer consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(topic));

        new Thread(() -> {
            try {
                isSubscribing = true;
                do {
                    ConsumerRecords<String,Object> records = consumer.poll(Duration.ofMillis(500));
                    for (ConsumerRecord record : records) {
                        String time = new SimpleDateFormat("HH:mm:ss").format(new Date(record.timestamp()));

                        SwingUtilities.invokeLater(() -> {
                            MessageBus.getInstance().publish(new RecordConsumed(time, record.value().toString()));
                        });
                    }

                } while (isSubscribing);
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                consumer.close();
                System.out.println("Closed sub thread " + topic);
            }
        }).start();
    }
}
