package no.knalum.ui.rightview.producepanel;

import javax.swing.*;

public class SetSchemaDialog extends JDialog {
    SetSchemaDialog() {
        setSize(500, 400);

        // AppKafkaClient.getSchemas();
        add(new JTextField("Schema name"));
        //   add(new JList<>(schemas));
    }
}
