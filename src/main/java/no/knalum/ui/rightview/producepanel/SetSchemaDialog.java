package no.knalum.ui.rightview.producepanel;

import no.knalum.kafka.AppKafkaClient;
import no.knalum.modal.ErrorModal;
import no.knalum.swingcomponents.common.TextAreaDialog;

import javax.swing.*;
import java.awt.*;

public class SetSchemaDialog extends TextAreaDialog {
    private final String selectedTopic;

    public SetSchemaDialog(String selectedTopic) {
        super("", true);
        this.selectedTopic = selectedTopic;
        setSize(500, 400);

        add(createBottomPanel(), BorderLayout.SOUTH);
    }

    private JPanel createBottomPanel() {
        JPanel jPanel = new JPanel();
        jPanel.add(new JButton("Cancel") {{
            addActionListener(e -> dispose());
        }});
        jPanel.add(new JButton("Set schema") {{
            addActionListener(e -> {
                try {
                    AppKafkaClient.setSchemaForTopic(selectedTopic, getTextArea().getText());
                    ErrorModal.showInfo("Schema set for topic " + selectedTopic);
                    dispose();
                } catch (Exception ex) {
                    ErrorModal.showError("Error registering schema: " + ex.getMessage());
                }
            });
        }}, BorderLayout.SOUTH);
        return jPanel;
    }
}
