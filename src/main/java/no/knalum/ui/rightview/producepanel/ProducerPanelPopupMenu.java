package no.knalum.ui.rightview.producepanel;

import no.knalum.kafka.AppKafkaClient;
import no.knalum.message.*;

import javax.swing.*;

public class ProducerPanelPopupMenu extends JPopupMenu implements MessageListener {
    private JMenuItem exampleJsonMenuItem;

    public ProducerPanelPopupMenu(String selectedTopic) {
        add(new JMenuItem("Set schema example value") {{
            addActionListener(e -> {
                ProducerPanelPopupMenu.this.updateUI();
                String schemaExampleJson = AppKafkaClient.getSchemaExample(selectedTopic);
                MessageBus.getInstance().publish(new SchemaJsonExampleMessage(schemaExampleJson));
            });
        }});

        add(new JMenuItem("Set value schema") {{
            addActionListener(e -> {
                new SetSchemaDialog().setVisible(true);
            });
        }});

        MessageBus.getInstance().subscribe(this);
    }

    @Override
    public void handleMessage(AppMessage message) {
        if (message instanceof TreeTopicChangedMessage) {
        }
    }
}
