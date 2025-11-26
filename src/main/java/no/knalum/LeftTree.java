package no.knalum;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.datatransfer.StringSelection;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;

public class LeftTree extends JPanel implements MyListener {
    private final JTree tree;
    private final DefaultMutableTreeNode root;
    private DefaultMutableTreeNode filteredRoot;
    private final JTextField filter;
    private DefaultTreeModel originalModel;

    public LeftTree() {
        super(new BorderLayout());
        setDoubleBuffered(true);
        this.root = new DefaultMutableTreeNode("Broker");

        add(this.filter = new JTextField(), BorderLayout.NORTH);
        this.filter.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                    System.out.println("Filter: " + filter.getText());
                    tree.setModel(createFilteredTreeModel());
                    tree.updateUI();
                }
            }
        });
        add(new JScrollPane(this.tree = new JTree(root)));
        tree.expandRow(0);

        tree.addMouseListener(new TreeContextMenuAdapter(tree));
        MessageBus.getInstance().subscribe(this);
        tree.getSelectionModel().addTreeSelectionListener(new MySelectionListener());
    }

    private DefaultTreeModel createFilteredTreeModel() {
        DefaultMutableTreeNode root1 = (DefaultMutableTreeNode) ( originalModel).getRoot();
        DefaultMutableTreeNode filteredRoot = new DefaultMutableTreeNode(((DefaultMutableTreeNode) originalModel.getRoot()).getUserObject());
        for (int index = 0; index < root1.getChildCount(); index++) {
                if (((DefaultMutableTreeNode)root1.getChildAt(index)).getUserObject().toString().toLowerCase().contains(filter.getText().toLowerCase())) {
                    Object userObject = ((DefaultMutableTreeNode) root1.getChildAt(index)).getUserObject();
                    filteredRoot.add(new DefaultMutableTreeNode(userObject));
                }
        }
        return new DefaultTreeModel(filteredRoot);
    }

    private JPopupMenu createTreePopupMenu() {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem copyItem = new JMenuItem("Copy");
        copyItem.addActionListener(e -> {
            Object selected = tree.getLastSelectedPathComponent();
            if (selected != null) {
                StringSelection selection = new StringSelection(selected.toString());
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(selection, selection);
            }
        });
        menu.add(copyItem);
        // Add more menu items here if needed
        return menu;
    }

    class MySelectionListener implements TreeSelectionListener {

        private AppKafkaClient client;

        @Override
        public void valueChanged(TreeSelectionEvent e) {
            if (client != null) {
                client.closeSubscribing();
            }
            if(e.getNewLeadSelectionPath()==null){
                return;
            }
            Object lastPathComponent = e.getNewLeadSelectionPath().getLastPathComponent();
            MessageBus.getInstance().publish(new TreeTopicChanged(lastPathComponent.toString()));
            this.client = new AppKafkaClient();
            client.subscribeToKafkaTopic(BrokerConfig.getInstance().getUrl(), lastPathComponent.toString());
        }
    }

    @Override
    public void handleMessage(AppMessage message) {
        if (message instanceof ConnectedToBrokerMessage message1) {
            this.originalModel = new DefaultTreeModel(new DefaultMutableTreeNode(message1.brokerUrl));

            message1.getNewNodes().forEach(n -> {
                ((DefaultMutableTreeNode) originalModel.getRoot()).add(new DefaultMutableTreeNode(n));
            });

            this.tree.setModel(originalModel);
            this.tree.expandRow(0);
            tree.updateUI();
        }
    }
}
