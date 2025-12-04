package no.knalum;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.util.UUID;

public class FileMenuBar extends JMenuBar implements MyListener {
    private final KafkaScope kafkaScope;
    private DefaultMutableTreeNode selectedNode;
    private  JMenuItem exampleJsonMenuItem;

    public FileMenuBar(KafkaScope kafkaScope) {
        this.kafkaScope = kafkaScope;
        JMenu menu = new JMenu("File");
        JMenuItem settingsItem = new JMenuItem("Settings...");
        settingsItem.addActionListener(e -> new ConfigDialog(kafkaScope));

        JMenuItem closeItem = new JMenuItem("Close");
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
        add(new ProducerMenu());
        add(new Help());

        MessageBus.getInstance().subscribe(this);
    }

    @Override
    public void handleMessage(AppMessage message) {
        if (message instanceof TreeTopicChanged msg) {
            this.selectedNode = msg.selectedNode();
            exampleJsonMenuItem.setEnabled(true);
        }
    }

    private class EditMenu extends JMenu {
        public EditMenu() {
            super("Edit");

            add(new JMenuItem("Create new topic") {{
                addActionListener(e -> {
                    AppKafkaClient.createTopic(UUID.randomUUID().toString().substring(0, 8));
                    AppKafkaClient.connectToKafkaAndPopulateTree();

                });
            }});

            add(new JMenuItem("Refresh topics") {{
                addActionListener(e -> {
                    AppKafkaClient.connectToKafkaAndPopulateTree();
                });
            }});

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


    private class ProducerMenu extends JMenu {

        public ProducerMenu() {
            super("Producer");
            add(exampleJsonMenuItem = new JMenuItem("Set schema example value") {{
                setEnabled(false);
                addActionListener(e -> {
                    String schemaExampleJson = AppKafkaClient.getSchemaExample(selectedNode);
                    MessageBus.getInstance().publish(new SchemaJsonExampleMessage(schemaExampleJson));
                });
            }});
        }
    }
}
