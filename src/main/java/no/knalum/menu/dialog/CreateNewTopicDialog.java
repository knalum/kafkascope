package no.knalum.menu.dialog;

import no.knalum.KafkaScope;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class CreateNewTopicDialog extends JDialog {
    public CreateNewTopicDialog(Consumer<CreateTopicDialogParams> cb) {
        super(KafkaScope.getInstance(), "Create New Topic", false);
        setTitle("Create New Topic");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(400, 320);
        setLocationRelativeTo(null);
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new java.awt.Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Topic Name:"), gbc);
        gbc.gridx = 1;
        JTextField topicNameField = new JTextField(20);
        panel.add(topicNameField, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("Partitions:"), gbc);
        gbc.gridx = 1;
        JSpinner partitionsSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
        panel.add(partitionsSpinner, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("Replication Factor:"), gbc);
        gbc.gridx = 1;
        JSpinner replicationSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
        panel.add(replicationSpinner, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("Cleanup Policy:"), gbc);
        gbc.gridx = 1;
        JPanel cleanupPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        JRadioButton deleteRadio = new JRadioButton("delete", true);
        JRadioButton compactRadio = new JRadioButton("compact");
        JRadioButton deleteCompactRadio = new JRadioButton("delete+compact");
        ButtonGroup cleanupGroup = new ButtonGroup();
        cleanupGroup.add(deleteRadio);
        cleanupGroup.add(compactRadio);
        cleanupGroup.add(deleteCompactRadio);
        cleanupPanel.add(deleteRadio);
        cleanupPanel.add(compactRadio);
        cleanupPanel.add(deleteCompactRadio);
        panel.add(cleanupPanel, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("Retention Time (ms):"), gbc);
        gbc.gridx = 1;
        JSpinner retentionSpinner = new JSpinner(new SpinnerNumberModel(604800000, 0, Integer.MAX_VALUE, 1000));
        panel.add(retentionSpinner, gbc);
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton createButton = new JButton("Create");
        JButton cancelButton = new JButton("Cancel");
        buttonPanel.add(cancelButton);
        buttonPanel.add(createButton);
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(buttonPanel, gbc);
        add(panel);
        // Set the default button to 'Create' for blue highlight and Enter key
        getRootPane().setDefaultButton(createButton);
        // ESC key closes dialog
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke("ESCAPE"),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        // Button actions
        cancelButton.addActionListener(e -> dispose());
        createButton.addActionListener(e -> {
            String topicName = topicNameField.getText().trim();
            int partitions = (Integer) partitionsSpinner.getValue();
            int replication = (Integer) replicationSpinner.getValue();
            String cleanupPolicy = deleteRadio.isSelected() ? "delete" : compactRadio.isSelected() ? "compact" : "delete+compact";
            int retention = (Integer) retentionSpinner.getValue();
            if (topicName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Topic name cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            } else if (partitions < 0 || partitions > 100) {
                JOptionPane.showMessageDialog(this, "Partitions should be between 0 and 100.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            } else if (replication < 0 || replication > 100) {
                JOptionPane.showMessageDialog(this, "Replication should be between 0 and 100.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            } else if (retention < 0 || retention > 100_000_000_0) {
                JOptionPane.showMessageDialog(this, "Retention should be between 0 and 1000000000.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            cb.accept(new CreateTopicDialogParams(topicName, partitions, replication, cleanupPolicy, retention));
            dispose();
        });
    }

}

