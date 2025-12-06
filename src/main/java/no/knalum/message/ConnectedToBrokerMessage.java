package no.knalum.message;

import java.util.Set;

public class ConnectedToBrokerMessage implements AppMessage {
    public Set<String> newNodes;
    public String brokerUrl;

    public ConnectedToBrokerMessage(String brokerUrl, Set<String> topics) {
        newNodes = topics;
        this.brokerUrl = brokerUrl;
    }

    public Set<String> getNewNodes() {
        return newNodes;
    }

    public void setNewNodes(Set<String> newNodes) {
        this.newNodes = newNodes;
    }
}
