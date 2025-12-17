package no.knalum.ui.rightview.messagetable;

import no.knalum.KafkaScope;
import no.knalum.menu.dialog.AppDialog;
import no.knalum.message.MessageBus;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class SearchDialog extends AppDialog {
    private static JTextField keyField;
    private static JTextField valueField;

    private static String value;
    private static String key;

    public SearchDialog() {
        super(KafkaScope.getInstance(), "Search", contentPane(), "Search", okCallback());

    }


    private static ActionListener okCallback() {
        return e -> {
            MessageBus.getInstance().publish(new SearchFilterAppliedMessage(keyField, valueField));
            value = valueField.getText();
            key = keyField.getText();
            Window window = SwingUtilities.getWindowAncestor(valueField);
            if (window != null) {
                window.dispose();
            }
        };
    }

    private static JComponent contentPane() {
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Key label
        gbc.gridx = 0;
        gbc.gridy = 0;
        jPanel.add(new JLabel("Key"), gbc);

        // Key text field
        gbc.gridx = 1;
        keyField = new JTextField(20);
        keyField.setText(key == null ? "" : key);
        // Add key listener for Enter
        keyField.addActionListener(okCallback());
        jPanel.add(keyField, gbc);

        // Value label
        gbc.gridx = 0;
        gbc.gridy = 1;
        jPanel.add(new JLabel("Value"), gbc);

        // Value text field
        gbc.gridx = 1;
        valueField = new JTextField(20);
        valueField.setText(value == null ? "" : value);
        // Add key listener for Enter
        valueField.addActionListener(okCallback());
        jPanel.add(valueField, gbc);

        // Ok button
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        return jPanel;
    }
}
