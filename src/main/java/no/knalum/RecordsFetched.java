package no.knalum;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.List;

public class RecordsFetched implements AppMessage {

    private final List<ConsumerRecord<String, Object>> record;

    public RecordsFetched(List<ConsumerRecord<String, Object>> record) {
        this.record = record;
    }

    public List<ConsumerRecord<String, Object>> getRecord() {
        return record;
    }
}
