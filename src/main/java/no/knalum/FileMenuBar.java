package no.knalum;

import javax.swing.*;
import java.util.UUID;

public class FileMenuBar extends JMenuBar {
    private final KafkaScope kafkaScope;

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
        add(new Help());
    }

    private class EditMenu extends JMenu {
        public EditMenu() {
            super("Edit");

            add(new JMenuItem("Create selectedNode") {{
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
}
