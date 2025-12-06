package no.knalum.menu.dialog;

public record CreateTopicDialogParams(String topicName, int partitions, int replication, String cleanupPolicy,
                                      int retention) {

}
