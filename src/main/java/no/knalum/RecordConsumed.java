package no.knalum;

import org.apache.kafka.clients.consumer.ConsumerRecord;

public record RecordConsumed(ConsumerRecord record) implements AppMessage {

}
