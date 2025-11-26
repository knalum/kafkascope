package no.knalum;

import java.util.Set;

public class TreeUpdatedMessage implements AppMessage {
    public Set<String> newNodes;

    public TreeUpdatedMessage(Set<String> newNodes) {
        this.newNodes = newNodes;
    }

    public Set<String> getNewNodes() {
        return newNodes;
    }

    public void setNewNodes(Set<String> newNodes) {
        this.newNodes = newNodes;
    }
}
