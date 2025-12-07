package no.knalum.menu;

import no.knalum.KafkaScope;
import no.knalum.config.BrokerConfig;
import no.knalum.kafka.AppKafkaClient;
import no.knalum.menu.dialog.AboutDialog;
import no.knalum.menu.dialog.CreateNewTopicDialog;
import no.knalum.menu.dialog.CreateTopicDialogParams;
import no.knalum.message.*;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Set;
import java.util.function.Consumer;

public class MenuBar extends JMenuBar implements MessageListener {
    private final KafkaScope kafkaScope;
    private DefaultMutableTreeNode selectedNode;
    private JMenuItem exampleJsonMenuItem;

    public MenuBar(KafkaScope kafkaScope) {
        this.kafkaScope = kafkaScope;

        add(new FileMenu());
        add(new EditMenu());
        add(new TopicMenu());
        add(new ViewMenu());
        add(new Help());

        MessageBus.getInstance().subscribe(this);
    }

    @Override
    public void handleMessage(AppMessage message) {
        if (message instanceof TreeTopicChangedMessage msg) {
            this.selectedNode = msg.selectedNode();
        }
    }

    private class EditMenu extends JMenu {
        public EditMenu() {
            super("Edit");

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
            refreshItem.setAccelerator(KeyStroke.getKeyStroke("meta R"));
            add(refreshItem);

        }
    }

    private class Help extends JMenu {
        public Help() {
            super("Help");
            add(new JMenuItem("About...") {{
                addActionListener(e -> {
                    new AboutDialog(kafkaScope);
                });
            }});
        }
    }


    private class ViewMenu extends JMenu {
        public ViewMenu() {
            super("View");
            add(new JMenuItem("Toggle producer panel layout") {{
                addActionListener(e -> {
                    MessageBus.getInstance().publish(new ToggleProducerPanelMessage());
                });
            }});
        }
    }
}
