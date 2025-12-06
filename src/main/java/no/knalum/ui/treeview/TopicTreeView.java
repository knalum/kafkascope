package no.knalum.ui.treeview;

import no.knalum.message.*;
import no.knalum.swingcomponents.Util;
import no.knalum.ui.rightview.messagetable.SortPane;
import no.knalum.ui.treeview.node.TopicNode;
import org.jdesktop.swingx.prompt.PromptSupport;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class TopicTreeView extends JPanel implements MessageListener {
    private final JTree tree;
    private final DefaultMutableTreeNode root;
    private JTextField filter;
    private DefaultTreeModel originalModel;
    private String selectedTopic;
    private SortPane.SortType sortChoice = SortPane.SortType.Oldest;
    public static int page = 0;

    public TopicTreeView() {
        super(new BorderLayout());
        setDoubleBuffered(true);
        this.root = new DefaultMutableTreeNode("Broker");

        add(createFilterPanel(), BorderLayout.NORTH);

        this.filter.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                    tree.setModel(createFilteredTreeModel());
                    tree.updateUI();
                }
            }
        });
        add(new JScrollPane(this.tree = new JTree(root)));
        tree.expandRow(0);
        tree.setCellRenderer(new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(
                    JTree tree, Object value, boolean sel,
                    boolean expanded, boolean leaf, int row, boolean hasFocus) {

                JLabel label = (JLabel) super.getTreeCellRendererComponent(
                        tree, value, sel, expanded, leaf, row, hasFocus);

                String text = value.toString();

                if (text.contains("localhost")) {
                    setIcon(new ImageIcon(getClass().getResource("/icons/database.png")));
                }

                return label;
            }
        });


        tree.addMouseListener(new TreeContextMenuAdapter(tree));
        MessageBus.getInstance().subscribe(this);
        tree.getSelectionModel().addTreeSelectionListener(new MySelectionListener());
    }

    private JPanel createFilterPanel() {
        JPanel jPanel = new JPanel(new BorderLayout());
        this.filter = new JTextField();
        PromptSupport.setPrompt("Search", filter);
        jPanel.add(this.filter, BorderLayout.CENTER);
        return jPanel;
    }

    private DefaultTreeModel createFilteredTreeModel() {
        DefaultMutableTreeNode root1 = (DefaultMutableTreeNode) (originalModel).getRoot();
        DefaultMutableTreeNode filteredRoot = new DefaultMutableTreeNode(((DefaultMutableTreeNode) originalModel.getRoot()).getUserObject());
        for (int index = 0; index < root1.getChildCount(); index++) {
            if (((DefaultMutableTreeNode) root1.getChildAt(index)).getUserObject().toString().toLowerCase().contains(filter.getText().toLowerCase())) {
                Object userObject = ((DefaultMutableTreeNode) root1.getChildAt(index)).getUserObject();
                filteredRoot.add(new TopicNode(userObject.toString()));
            }
        }
        return new DefaultTreeModel(filteredRoot);
    }

    class MySelectionListener implements TreeSelectionListener {
        @Override
        public void valueChanged(TreeSelectionEvent e) {
            if (e.getNewLeadSelectionPath() != null) {
                Object lastPathComponent = e.getNewLeadSelectionPath().getLastPathComponent();
                selectedTopic = lastPathComponent.toString();
                DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) e.getNewLeadSelectionPath().getLastPathComponent();
                MessageBus.getInstance().publish(new TreeTopicChangedMessage(selectedNode));
            }
        }
    }

    @Override
    public void handleMessage(AppMessage message) {
        if (message instanceof ConnectedToBrokerMessage message1) {
            this.originalModel = new DefaultTreeModel(new DefaultMutableTreeNode(message1.brokerUrl));

            message1.getNewNodes().stream().sorted().forEach(n -> {
                ((DefaultMutableTreeNode) originalModel.getRoot()).add(new TopicNode(n));
            });

            this.tree.setModel(originalModel);
            this.tree.expandRow(0);
            tree.updateUI();
        } else if (message instanceof SelectTreeItemMessage msg) {
            // Find and select the new topic node
            DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
            TreePath foundPath = Util.findNodeByName(root, msg.topicName());
            if (foundPath != null) {
                tree.setSelectionPath(foundPath);
                tree.scrollPathToVisible(foundPath);
                tree.requestFocus();
            }
        }
    }
}
