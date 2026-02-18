package no.knalum.message;

public record PartitionFilterAppliedMessage(String partition) implements AppMessage{
}
