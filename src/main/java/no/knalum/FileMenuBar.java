package no.knalum;

import javax.swing.*;
import java.awt.*;
import java.util.UUID;

public class FileMenuBar extends MenuBar {
    private final KafkaScope kafkaScope;

    public FileMenuBar(KafkaScope kafkaScope) {
        this.kafkaScope = kafkaScope;
        Menu menu = new Menu("File");
        JMenuItem settingsItem = new JMenuItem("Settings...");
        settingsItem.addActionListener(e -> new ConfigDialog(kafkaScope));

        MenuItem closeItem = new MenuItem("Close");
        closeItem.addActionListener(e -> System.exit(0));
        menu.add(closeItem);

        add(menu);
        add(new EditMenu());
        add(new Help());
    }

    private class EditMenu extends Menu {
        public EditMenu() {
            super("Edit");

            add(new MenuItem("Create topic") {{
                addActionListener(e -> {
                    AppKafkaClient.createTopic(UUID.randomUUID().toString().substring(0, 8));
                    AppKafkaClient.connectToKafkaAndPopulateTree();

                });
            }});

            add(new MenuItem("Refresh topics") {{
                addActionListener(e -> {
                    AppKafkaClient.connectToKafkaAndPopulateTree();
                });
            }});

        }
    }

    private class Help extends Menu {
        public Help() {
            super("Help");
            add(new MenuItem("About...") {{
                addActionListener(e -> {
                    new AboutDialog(kafkaScope);
                });
            }});
        }
    }
}
