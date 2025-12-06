package no.knalum.message;

import javax.swing.tree.DefaultMutableTreeNode;

public record TreeTopicChangedMessage(DefaultMutableTreeNode selectedNode) implements AppMessage {


}
