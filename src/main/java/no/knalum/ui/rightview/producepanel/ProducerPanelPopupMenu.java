package no.knalum.ui.rightview.producepanel;

import no.knalum.kafka.AppKafkaClient;
import no.knalum.message.*;

import javax.swing.*;

public class ProducerPanelPopupMenu extends JPopupMenu implements MessageListener {
    private JMenuItem exampleJsonMenuItem;

    public ProducerPanelPopupMenu(String selectedTopic) {
        JMenuItem exampleJsonMenuItem = new JMenuItem("Set schema example value");
        add(exampleJsonMenuItem);

        exampleJsonMenuItem.addActionListener(e -> {
            ProducerPanelPopupMenu.this.updateUI();
            String schemaExampleJson = AppKafkaClient.getSchemaExample(selectedTopic);
            MessageBus.getInstance().publish(new SchemaJsonExampleMessage(schemaExampleJson));
        });

        MessageBus.getInstance().subscribe(this);
    }

    @Override
    public void handleMessage(AppMessage message) {
        if (message instanceof TreeTopicChangedMessage) {
        }
    }
}
