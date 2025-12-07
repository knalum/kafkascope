package no.knalum.menu;

import no.knalum.kafka.AppKafkaClient;
import no.knalum.message.AppMessage;
import no.knalum.message.MessageBus;
import no.knalum.message.MessageListener;
import no.knalum.message.TreeTopicChangedMessage;
import no.knalum.swingcomponents.common.TextAreaDialog;
import no.knalum.ui.treeview.node.TopicNode;

import javax.swing.*;

public class TopicMenu extends JMenu implements MessageListener {
    private final JMenuItem displaySchemaMenuItem;
    private String selectedTopic;

    public TopicMenu() {
        super("Topic");
        add(this.displaySchemaMenuItem = new JMenuItem("Display schema") {{
            setEnabled(false);
            addActionListener(e -> {
                String schemaForTopic = AppKafkaClient.getSchemaForTopic(selectedTopic);
                String prettySchema;
                try {
                    prettySchema = new com.fasterxml.jackson.databind.ObjectMapper()
                            .writerWithDefaultPrettyPrinter()
                            .writeValueAsString(new com.fasterxml.jackson.databind.ObjectMapper().readTree(schemaForTopic));
                } catch (Exception ex) {
                    prettySchema = schemaForTopic; // fallback to raw if not valid JSON
                }
                TextAreaDialog textAreaDialog = new TextAreaDialog(prettySchema);
                textAreaDialog.setTitle("Schema for " + selectedTopic);
                textAreaDialog.setVisible(true);
            });
        }});
        MessageBus.getInstance().subscribe(this);
    }

    @Override
    public void handleMessage(AppMessage message) {
        if (message instanceof TreeTopicChangedMessage msg) {
            if (msg.selectedNode() instanceof TopicNode) {
                this.selectedTopic = msg.selectedNode().getUserObject().toString();
            }
            displaySchemaMenuItem.setEnabled(msg.selectedNode() instanceof TopicNode);
        }
    }
}
