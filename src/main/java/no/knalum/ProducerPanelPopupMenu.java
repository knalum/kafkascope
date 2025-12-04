package no.knalum;

import javax.swing.*;

public class ProducerPanelPopupMenu extends JPopupMenu implements MyListener {
    private JMenuItem exampleJsonMenuItem;

    public ProducerPanelPopupMenu(String selectedTopic) {
        JMenuItem exampleJsonMenuItem = new JMenuItem("Set schema example value");
        add(exampleJsonMenuItem);

        exampleJsonMenuItem.setEnabled(false);
        exampleJsonMenuItem.addActionListener(e -> {
            ProducerPanelPopupMenu.this.updateUI();
            String schemaExampleJson = AppKafkaClient.getSchemaExample(selectedTopic);
            MessageBus.getInstance().publish(new SchemaJsonExampleMessage(schemaExampleJson));
        });

        MessageBus.getInstance().subscribe(this);
    }

    @Override
    public void handleMessage(AppMessage message) {
        if(message instanceof TreeTopicChanged){
            exampleJsonMenuItem.setEnabled(true);
        }
    }
}
