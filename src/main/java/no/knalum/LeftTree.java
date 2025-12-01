package no.knalum;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.jdesktop.swingx.prompt.PromptSupport;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

public class LeftTree extends JPanel implements MyListener {
    private final JTree tree;
    private final DefaultMutableTreeNode root;
    private JTextField filter;
    private DefaultTreeModel originalModel;
    private String selectedTopic;
    private SortPane.SortType sortChoice = SortPane.SortType.Oldest;
    public static int page = 0;

    public LeftTree() {
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
                filteredRoot.add(new DefaultMutableTreeNode(userObject));
            }
        }
        return new DefaultTreeModel(filteredRoot);
    }

    private AppKafkaClient client;

    class MySelectionListener implements TreeSelectionListener {

        //private AppKafkaClient client;

        @Override
        public void valueChanged(TreeSelectionEvent e) {
            if (client != null) {
                client.closeSubscribing();
            }
            if (e.getNewLeadSelectionPath() == null) {
                return;
            }
            Object lastPathComponent = e.getNewLeadSelectionPath().getLastPathComponent();
            selectedTopic = (String) lastPathComponent.toString();
            MessageBus.getInstance().publish(new TreeTopicChanged(lastPathComponent.toString()));
            client = new AppKafkaClient();
            client.subscribeToKafkaTopic(BrokerConfig.getInstance().getBrokerUrl(), lastPathComponent.toString(), SortPane.SortType.Oldest, page);
        }
    }

    @Override
    public void handleMessage(AppMessage message) {
        if (message instanceof ConnectedToBrokerMessage message1) {
            this.originalModel = new DefaultTreeModel(new DefaultMutableTreeNode(message1.brokerUrl));

            message1.getNewNodes().stream().sorted().forEach(n -> {
                ((DefaultMutableTreeNode) originalModel.getRoot()).add(new DefaultMutableTreeNode(n));
            });

            this.tree.setModel(originalModel);
            this.tree.expandRow(0);
            tree.updateUI();
        } else if (message instanceof SortOrderChangedMessage sortOrderChangedMessage) {
            MessageBus.getInstance().publish(new TreeTopicChanged(selectedTopic));
            if (client != null) {
                client.closeSubscribing();
            }
            client = new AppKafkaClient();
            this.sortChoice = sortOrderChangedMessage.getSortChoice();
            List<ConsumerRecord<String, Object>> records = client.getRecords(BrokerConfig.getInstance().getBrokerUrl(), selectedTopic, sortChoice, page);
            records.stream().forEach(record -> {
                MessageBus.getInstance().publish(new RecordConsumed(record));
            });

        } else if (message instanceof NextPageMessage) {
            if (client != null) {
                client.closeSubscribing();
            }
            client = new AppKafkaClient();
            page += 1;
            List<ConsumerRecord<String, Object>> records = client.getRecords(BrokerConfig.getInstance().getBrokerUrl(), selectedTopic, sortChoice, page);
            MessageBus.getInstance().publish(new RecordsFetched(records));
        } else if (message instanceof PrevPageMessage) {
            if (client != null) {
                client.closeSubscribing();
            }
            client = new AppKafkaClient();
            page = Math.max(0, page - 1);
            List<ConsumerRecord<String, Object>> records = client.getRecords(BrokerConfig.getInstance().getBrokerUrl(), selectedTopic, sortChoice, page);
            MessageBus.getInstance().publish(new RecordsFetched(records));
        }
    }
}
