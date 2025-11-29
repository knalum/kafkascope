package no.knalum;

import org.apache.kafka.clients.admin.ConfigEntry;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class TopicConfigsPanel extends JPanel implements MyListener {
    private final JPanel configPanel;
    private String selectedTopic;

    TopicConfigsPanel() {
        setLayout(new BorderLayout());
        this.configPanel = new JPanel();
        configPanel.setLayout(new BoxLayout(configPanel, BoxLayout.Y_AXIS));
        add(new JScrollPane(configPanel), BorderLayout.CENTER);
        add(createUpdateButtonRow(), BorderLayout.SOUTH);

        MessageBus.getInstance().subscribe(this);
    }

    private JPanel createUpdateButtonRow() {
        JPanel jPanel = new JPanel(new BorderLayout());
        JButton update = new JButton("Update");
        update.addActionListener(e -> {
            Component[] children = configPanel.getComponents();

            List<ConfigEntry> updatedEntries = new ArrayList<>();
            for (Component c : children) {
                if (c instanceof JPanel panel) {
                    JLabel name = (JLabel) Arrays.stream(panel.getComponents()).filter(c2 -> c2 instanceof JLabel).findAny().get();
                    JTextField txt = (JTextField) Arrays.stream(panel.getComponents()).filter(c2 -> c2 instanceof JTextField).findAny().get();
                    System.out.println(name.getText() + " " + txt.getText());
                    updatedEntries.add(new ConfigEntry(name.getText(), txt.getText()));
                }
            }

            AppKafkaClient.updateTopicConfig(selectedTopic, updatedEntries);
        });
        jPanel.add(update, BorderLayout.EAST);
        return jPanel;
    }

    public static JPanel createRow(String labelText, String value) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

        JLabel label = new JLabel(labelText);
        JTextField textField = new JTextField(value);
        textField.setName(labelText);

        // Optional: align label and text field nicely
        label.setPreferredSize(new Dimension(200, label.getPreferredSize().height));
        textField.setMaximumSize(new Dimension(Integer.MAX_VALUE, textField.getPreferredSize().height));

        panel.add(label);
        panel.add(Box.createRigidArea(new Dimension(5, 0))); // small spacing
        panel.add(textField);

        // Align sub-panel left in main panel
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        return panel;
    }

    @Override
    public void handleMessage(AppMessage message) {
        if (message instanceof TreeTopicChanged topicSelected) {
            this.selectedTopic = topicSelected.topic();
            getTopicConfigParams(selectedTopic);
        }
    }

    private void getTopicConfigParams(String topic) {
        Collection<ConfigEntry> configEntries = AppKafkaClient.describeTopic2(topic).stream().sorted(Comparator.comparing(ConfigEntry::name)).toList();
        configPanel.removeAll();
        for (ConfigEntry configEntry : configEntries) {
            configPanel.add(createRow(configEntry.name(), configEntry.value()));
        }
        configPanel.revalidate();
        configPanel.repaint();
    }
}
