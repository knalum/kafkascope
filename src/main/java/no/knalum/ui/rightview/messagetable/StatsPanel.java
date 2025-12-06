package no.knalum.ui.rightview.messagetable;

import no.knalum.kafka.KafkaStatsClient;
import no.knalum.message.*;
import no.knalum.ui.treeview.node.TopicNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.util.concurrent.Executors;

import static no.knalum.ui.rightview.TopicConfigsPanel.createRow;

public class StatsPanel extends JPanel implements MessageListener {
    private static final long STATS_UPDATE_INTERVAL_SEC = 10;
    private final JPanel configPanel;
    private final JPanel countField;
    private final JPanel sizeField;
    private final JPanel partitionsField;
    private DefaultMutableTreeNode selectedTopic;

    private static final Logger LOGGER = LoggerFactory.getLogger(StatsPanel.class);

    public StatsPanel() {
        setLayout(new BorderLayout());
        this.configPanel = new JPanel();
        configPanel.setLayout(new BoxLayout(configPanel, BoxLayout.Y_AXIS));
        add(configPanel);
        MessageBus.getInstance().subscribe(this);

        configPanel.add(this.countField = createRow("Count", "", false));
        configPanel.add(this.sizeField = createRow("Size", "", false));
        configPanel.add(this.partitionsField = createRow("Partitions", "", false));

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> {
            if (selectedTopic != null && selectedTopic instanceof TopicNode) {
                getStats(selectedTopic.toString());
            }
        });
        configPanel.add(refreshButton);

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
                () -> {
                    if (selectedTopic != null) {
                        if (selectedTopic instanceof TopicNode) {
                            getStats(selectedTopic.toString());
                        }
                    }
                },
                0, STATS_UPDATE_INTERVAL_SEC, java.util.concurrent.TimeUnit.SECONDS
        );
    }

    @Override
    public void handleMessage(AppMessage message) {
        if (message instanceof TreeTopicChangedMessage treeTopicChangedMessage) {
            this.selectedTopic = treeTopicChangedMessage.selectedNode();
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() {
                    if (selectedTopic instanceof TopicNode) {
                        getStats(selectedTopic.toString());

                    }
                    return null;
                }
            }.execute();
        }
    }


    private void getStats(String selectedTopic) {
        try {
            KafkaStatsClient.TopicStats topicStats = new KafkaStatsClient().getTopicStats(selectedTopic);
            populateStatsPanel(topicStats);
            MessageBus.getInstance().publish(new TopicStatsMessage(topicStats));
        } catch (Exception e) {
            LOGGER.error("Failed to get selectedNode stats for selectedNode " + selectedTopic, e);
        }
    }

    private void populateStatsPanel(KafkaStatsClient.TopicStats topicStats) {
        findTextFieldByName(this.getRootPane(), "Size").setText(String.valueOf(topicStats.size()));
        findTextFieldByName(this.getRootPane(), "Count").setText(String.valueOf(topicStats.count()));
        findTextFieldByName(this.getRootPane(), "Partitions").setText(String.valueOf(topicStats.numPartitions()));

        revalidate();
        repaint();
    }

    public static JTextField findTextFieldByName(Container root, String name) {
        for (Component c : root.getComponents()) {
            if (name.equals(c.getName())) {
                return (JTextField) c;
            }
            if (c instanceof Container) {
                JTextField child = findTextFieldByName((Container) c, name);
                if (child != null) return child;
            }
        }
        return null;
    }

}
