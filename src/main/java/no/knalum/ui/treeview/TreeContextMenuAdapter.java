package no.knalum.ui.treeview;

import no.knalum.kafka.AppKafkaClient;
import org.apache.kafka.clients.admin.TopicDescription;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;

public class TreeContextMenuAdapter extends MouseAdapter {

    private final JTree tree;

    public TreeContextMenuAdapter(JTree tree) {
        this.tree = tree;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        showPopup(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        showPopup(e);
    }

    private void showPopup(MouseEvent e) {


        if (e.isPopupTrigger()) {
            int row = tree.getClosestRowForLocation(e.getX(), e.getY());
            tree.setSelectionRow(row); // select node on right-click
            TreePath path = tree.getPathForRow(row);

            if (path != null) {
                DefaultMutableTreeNode selectedNode =
                        (DefaultMutableTreeNode) path.getLastPathComponent();

                JPopupMenu popup = new JPopupMenu();
                JMenuItem deleteTopic = new JMenuItem("Delete topic");
                deleteTopic.addActionListener(evt -> {
                    AppKafkaClient.deleteTopic(selectedNode.getUserObject().toString());
                    AppKafkaClient.connectToKafkaAndPopulateTree();
                });
                popup.add(deleteTopic);

                JMenuItem describeItem = new JMenuItem("Describe");
                describeItem.addActionListener(e2 -> {
                    String topic = selectedNode.getUserObject().toString();
                    Map<String, TopicDescription> descriptions = AppKafkaClient.describeTopic(topic);
                    new TopicDescriptionsModal(descriptions, topic);
                });
                popup.add(describeItem);


                // Example: enable/disable menu items based on node type
                popup.show(tree, e.getX(), e.getY());
            }
        }
    }
}
