package no.knalum;

public record RecordConsumed(String time, String key, String payload) implements AppMessage {

}
