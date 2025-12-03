package no.knalum;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.util.concurrent.Executors;

import static no.knalum.TopicConfigsPanel.createRow;

public class StatsPanel extends JPanel implements MyListener {
    private static final long STATS_UPDATE_INTERVAL_SEC = 10;
    private final JPanel configPanel;
    private final JPanel countField;
    private final JPanel sizeField;
    private final JPanel partitionsField;
    private final JPanel recordField;
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
        configPanel.add(this.recordField = createRow("Last record", "", false));

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
        if (message instanceof TreeTopicChanged treeTopicChanged) {
            this.selectedTopic = treeTopicChanged.selectedNode();
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
        AppKafkaClient.getNumRecords(selectedTopic);
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
        findTextFieldByName(this.getRootPane(), "Last record").setText(String.valueOf(topicStats.lastTs()));

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
