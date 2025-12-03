package no.knalum;

public record TopicStatsMessage(KafkaStatsClient.TopicStats topicStats) implements AppMessage {
}
