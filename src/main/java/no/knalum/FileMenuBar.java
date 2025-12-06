package no.knalum;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Set;
import java.util.function.Consumer;

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
                addActionListener(e -> new CreateNewTopicDialog(new Consumer<Params>() {
                    @Override
                    public void accept(Params params) {
                        try {
                            AppKafkaClient.createTopic(params);
                            Set<String> topics = AppKafkaClient.connect(BrokerConfig.getInstance());
                            MessageBus.getInstance().publish(new ConnectedToBrokerMessage(BrokerConfig.getInstance().getBrokerUrl(), topics));
                            MessageBus.getInstance().publish(new SelectTreeItemMessage(params.topicName()));
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }

                    }
                }).setVisible(true));
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
