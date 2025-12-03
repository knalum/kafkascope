package no.knalum;

import javax.swing.tree.DefaultMutableTreeNode;

public record TreeTopicChanged(DefaultMutableTreeNode selectedNode) implements AppMessage {


}
