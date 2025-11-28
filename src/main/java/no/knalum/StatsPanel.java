package no.knalum;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
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
    private String selectedTopic;

    private static final Logger LOGGER = LoggerFactory.getLogger(StatsPanel.class);

    public StatsPanel() {
        setLayout(new BorderLayout());
        this.configPanel = new JPanel();
        configPanel.setLayout(new BoxLayout(configPanel, BoxLayout.Y_AXIS));
        configPanel.add(createRow("Count", "B"));

        add(configPanel);
        MessageBus.getInstance().subscribe(this);

        configPanel.add(this.countField = createRow("Count", ""));
        configPanel.add(this.sizeField = createRow("Size", ""));
        configPanel.add(this.partitionsField = createRow("Partitions", ""));
        configPanel.add(this.recordField = createRow("Last record", ""));

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
                () -> {
                    if (selectedTopic != null) {
                        LOGGER.info("Get stats");
                        SwingUtilities.invokeLater(() -> getStats(selectedTopic));
                    }
                },
                0, STATS_UPDATE_INTERVAL_SEC, java.util.concurrent.TimeUnit.SECONDS
        );
    }

    @Override
    public void handleMessage(AppMessage message) {
        if (message instanceof TreeTopicChanged treeTopicChanged) {
            this.selectedTopic = treeTopicChanged.topic();
            SwingUtilities.invokeLater(() -> getStats(selectedTopic));
        }
    }


    private void getStats(String selectedTopic) {
        AppKafkaClient.getTopicStats(selectedTopic);
        try {
            KafkaStatsClient.TopicStats topicStats = new KafkaStatsClient().getTopicStats(selectedTopic);
            System.out.println(topicStats);
            populateStatsPanel(topicStats);
        } catch (Exception e) {
            ErrorModal.showError("Error: " + e.getMessage());
        }
    }

    private void populateStatsPanel(KafkaStatsClient.TopicStats topicStats) {
        findTextFieldByName(this.getRootPane(), "Size").setText(String.valueOf(topicStats.size()));

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
