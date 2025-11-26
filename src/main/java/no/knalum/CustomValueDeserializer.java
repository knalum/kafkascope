package no.knalum;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import org.apache.kafka.common.serialization.Deserializer;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class CustomValueDeserializer implements Deserializer<Object> {

    private String schemaRegistryUrl;
    private KafkaAvroDeserializer avroDeserializer;
    private SchemaRegistryClient schemaRegistryClient;

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        this.schemaRegistryUrl = (String) configs.get("schema.registry.url");

        if (this.schemaRegistryUrl == null) {
            throw new IllegalArgumentException(
                    "schema.registry.url must be provided to CustomValueDeserializer"
            );
        }

        // Initialize Schema Registry client
        this.schemaRegistryClient =
                new CachedSchemaRegistryClient(schemaRegistryUrl, 100);

        // Initialize Avro deserializer with the same config
        this.avroDeserializer = new KafkaAvroDeserializer(schemaRegistryClient);
        this.avroDeserializer.configure((Map) configs, isKey);
    }

    @Override
    public Object deserialize(String topic, byte[] data) {
        if (data == null || data.length == 0) return null;

        // Check if this is Avro data with Confluent magic byte
        if (isConfluentAvro(data)) {
            int schemaId = extractSchemaId(data);

            if (schemaExists(schemaId)) {
                return avroDeserializer.deserialize(topic, data);
            }
        }

        // Not Avro: treat as String
        return new String(data, StandardCharsets.UTF_8);
    }

    @Override
    public void close() {
        avroDeserializer.close();
    }

    // --- Utility methods ----------------------------------------------------

    private boolean isConfluentAvro(byte[] data) {
        // Magic byte must be 0 and we need at least 5 bytes total
        return data.length > 5 && data[0] == 0x00;
    }

    private int extractSchemaId(byte[] data) {
        return ((data[1] & 0xFF) << 24) |
                ((data[2] & 0xFF) << 16) |
                ((data[3] & 0xFF) << 8) |
                (data[4] & 0xFF);
    }

    private boolean schemaExists(int schemaId) {
        try {
            return schemaRegistryClient.getSchemaById(schemaId) != null;
        } catch (Exception e) {
            return false;
        }
    }
}
