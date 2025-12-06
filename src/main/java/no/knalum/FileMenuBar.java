package no.knalum;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Set;
import java.util.UUID;

public class FileMenuBar extends JMenuBar implements MyListener {
    private final KafkaScope kafkaScope;
    private DefaultMutableTreeNode selectedNode;
    private JMenuItem exampleJsonMenuItem;

    public FileMenuBar(KafkaScope kafkaScope) {
        this.kafkaScope = kafkaScope;
        JMenu menu = new JMenu("File");
        JMenuItem settingsItem = new JMenuItem("Settings...");
        settingsItem.addActionListener(e -> new ConfigDialog(kafkaScope));

        JMenuItem closeItem = new JMenuItem("Exit");
        closeItem.addActionListener(e -> {
            new SwingWorker<>() {
                @Override
                protected Object doInBackground() throws Exception {
                    System.exit(0);
                    return null;
                }
            }.execute();
        });
        menu.add(closeItem);

        add(menu);
        add(new EditMenu());
        add(new ViewMenu());
        add(new Help());

        MessageBus.getInstance().subscribe(this);
    }

    @Override
    public void handleMessage(AppMessage message) {
        if (message instanceof TreeTopicChanged msg) {
            this.selectedNode = msg.selectedNode();
        }
    }

    private class EditMenu extends JMenu {
        public EditMenu() {
            super("Edit");

            add(new JMenuItem("Create new topic") {{
                addActionListener(e -> {
                    String topicName = UUID.randomUUID().toString().substring(0, 8);
                    AppKafkaClient.createTopic(topicName);
                    AppKafkaClient.connectToKafkaAndPopulateTree();
                    try {
                        Set<String> topics = AppKafkaClient.connect(BrokerConfig.getInstance());
                        MessageBus.getInstance().publish(new ConnectedToBrokerMessage(BrokerConfig.getInstance().getBrokerUrl(), topics));
                        MessageBus.getInstance().publish(new SelectTreeItemMessage(topicName));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
            }});

            JMenuItem refreshItem = new JMenuItem("Refresh topics");
            refreshItem.addActionListener(e -> {
                AppKafkaClient.connectToKafkaAndPopulateTree();
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
