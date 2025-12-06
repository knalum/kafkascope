package no.knalum;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.JsonDecoder;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Properties;
import java.util.concurrent.Future;

@Disabled
public class KafkaAvroProducerTest {
    @Test
    void sendJsonRecordAndEncodeItAsAvro() throws Exception {
        // Avro schema
        String schemaString = """
                {"type":"record","name":"User","fields":[{"name":"name","type":"string"},{"name":"age","type":"int"}]}
                """;
        Schema schema = new Schema.Parser().parse(schemaString);

        // JSON record
        String json = new ObjectMapper().writeValueAsString(new Person("Alice", 30));

        JsonDecoder decoder = DecoderFactory.get().jsonDecoder(schema, json);
        SpecificDatumReader<GenericRecord> reader = new SpecificDatumReader<>(schema);
        GenericRecord avroRecord = reader.read(null, decoder);

        // Kafka producer config
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
        props.put("schema.registry.url", "http://localhost:8081");

        KafkaProducer<String, GenericRecord> producer = new KafkaProducer<>(props);
        String topic = "test-avro-topic22";
        ProducerRecord<String, GenericRecord> record = new ProducerRecord<>(topic, "key1", avroRecord);
        Future<RecordMetadata> future = producer.send(record);
        RecordMetadata metadata = future.get();
        System.out.println("Sent record to topic " + metadata.topic() + " partition " + metadata.partition() + " offset " + metadata.offset());
        producer.close();
    }

    record Person(String name,int age){}

    @Test
    void sendExampleAvro1RecordToKafka() throws Exception {
        ExampleAvro1 example = new ExampleAvro1();
        example.setName("Ola Nordmann");
        example.setAge(42);
        example.setIsNorwegian(true);
        java.util.List<String> items = java.util.Arrays.asList("item1", "item2");
        example.setItems(items);
        Address address = new Address();
        address.setStreet("Karl Johans gate");
        address.setCity("Oslo");
        address.setZip("0154");
        example.setAddress(address);

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
        props.put("schema.registry.url", "http://localhost:8081");

        KafkaProducer<String, ExampleAvro1> producer = new KafkaProducer<>(props);
        String topic = "test-avro-example444";
        ProducerRecord<String, ExampleAvro1> record = new ProducerRecord<>(topic, "key1", example);
        Future<RecordMetadata> future = producer.send(record);
        RecordMetadata metadata = future.get();
        System.out.println("Sent ExampleAvro1 record to topic " + metadata.topic() + " partition " + metadata.partition() + " offset " + metadata.offset());
        producer.close();
    }
}
