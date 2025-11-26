package no.knalum;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

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
                deleteTopic.addActionListener(evt->{
                    AppKafkaClient.deleteTopic(selectedNode.getUserObject().toString());
                });
                popup.add(deleteTopic);

                popup.add(new JMenuItem("Add topic"){{
                    addActionListener(e->{
                        AppKafkaClient.createTopic(UUID.randomUUID().toString().substring(0,4));
                        try {
                            Set<String> topics = AppKafkaClient.connect(BrokerConfig.getInstance());
                            MessageBus.getInstance().publish(new ConnectedToBrokerMessage(BrokerConfig.getInstance().getUrl(), topics));
                        } catch (ExecutionException ex) {
                            throw new RuntimeException(ex);
                        } catch (InterruptedException ex) {
                            throw new RuntimeException(ex);
                        }
                    });
                }});


                // Example: enable/disable menu items based on node type
                popup.show(tree, e.getX(), e.getY());
            }
        }
    }
}
