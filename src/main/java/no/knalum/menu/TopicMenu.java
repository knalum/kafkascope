package no.knalum.menu;

import no.knalum.config.BrokerConfig;
import no.knalum.kafka.AppKafkaClient;
import no.knalum.menu.dialog.CreateNewTopicDialog;
import no.knalum.menu.dialog.CreateTopicDialogParams;
import no.knalum.message.*;
import no.knalum.swingcomponents.common.TextAreaDialog;
import no.knalum.ui.rightview.producepanel.SetSchemaDialog;
import no.knalum.ui.treeview.node.TopicNode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Set;
import java.util.function.Consumer;

public class TopicMenu extends JMenu implements MessageListener {
    private final JMenuItem displaySchemaMenuItem;
    private String selectedTopic;

    public TopicMenu() {
        super("Topic");
        add(new JMenuItem("Create new topic...") {{
            addActionListener(e -> new CreateNewTopicDialog(new Consumer<CreateTopicDialogParams>() {
                @Override
                public void accept(CreateTopicDialogParams createTopicDialogParams) {
                    try {
                        AppKafkaClient.createTopic(createTopicDialogParams);
                        Set<String> topics = AppKafkaClient.connect(BrokerConfig.getInstance().getBrokerUrl(), BrokerConfig.getInstance().getSchemaRegistryUrl());
                        MessageBus.getInstance().publish(new ConnectedToBrokerMessage(BrokerConfig.getInstance().getBrokerUrl(), topics));
                        MessageBus.getInstance().publish(new SelectTreeItemMessage(createTopicDialogParams.topicName()));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                }
            }).setVisible(true));
        }});
        JMenuItem refreshItem = new JMenuItem("Refresh topics");
        refreshItem.addActionListener(e -> {
            AppKafkaClient.connectToKafkaAndPopulateTree(BrokerConfig.getInstance().getConfig());
        });
        refreshItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        add(refreshItem);

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
                TextAreaDialog textAreaDialog = new TextAreaDialog(prettySchema, false);
                textAreaDialog.setTitle("Schema for " + selectedTopic);
                textAreaDialog.setVisible(true);
            });
        }});

        add(new JSeparator(JSeparator.HORIZONTAL));

        add(new JMenuItem("Set schema example value") {{
            addActionListener(e -> {
                TopicMenu.this.updateUI();
                String schemaExampleJson = AppKafkaClient.getSchemaExample(selectedTopic);
                MessageBus.getInstance().publish(new SchemaJsonExampleMessage(schemaExampleJson));
            });
        }});

        add(new JMenuItem("Set schema for topic") {{
            addActionListener(e -> {
                new SetSchemaDialog(selectedTopic).withTitle("Set schema for topic").setVisible(true);
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
