package no.knalum;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class AppDialog extends JDialog {
    public AppDialog(java.awt.Frame parent, String title, JPanel contentPane) {
        this(parent, title, contentPane, null, null);
    }

    public AppDialog(java.awt.Frame parent, String title, JPanel contentPane, String okButtonLabel, ActionListener okButtonCallback) {
        super(parent, title, true);
        setLayout(new BorderLayout());

        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke("ESCAPE"),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        JPanel buttonPanel = new JPanel();
        if (okButtonLabel != null) {
            JButton okButton = new JButton(okButtonLabel);
            okButton.addActionListener(okButtonCallback);
            buttonPanel.add(okButton);
        }

        JButton cancelButton = new JButton("Cancel");
        buttonPanel.add(cancelButton);

        add(contentPane, BorderLayout.CENTER); // Use CENTER for resizing
        add(buttonPanel, BorderLayout.SOUTH);
        cancelButton.addActionListener(e -> dispose());
        pack(); // Set size according to components
        setLocationRelativeTo(this);
        setVisible(true);

    }
}
