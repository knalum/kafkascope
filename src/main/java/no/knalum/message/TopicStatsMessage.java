package no.knalum.message;

import no.knalum.kafka.KafkaStatsClient;

public record TopicStatsMessage(KafkaStatsClient.TopicStats topicStats) implements AppMessage {
}
