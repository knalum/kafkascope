package no.knalum.ui.rightview.producepanel;

import no.knalum.kafka.AppKafkaClient;
import no.knalum.swingcomponents.common.TextAreaDialog;

import javax.swing.*;
import java.awt.*;

public class SetSchemaDialog extends TextAreaDialog {
    public SetSchemaDialog(String selectedTopic) {
        super("", true);
        setSize(500, 400);

        add(new JButton("Set schema") {{
            addActionListener(e -> {
                AppKafkaClient.setSchemaForTopic(selectedTopic, getTextArea().getText());
            });
        }}, BorderLayout.SOUTH);
    }
}
