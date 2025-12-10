package no.knalum.menu;

import no.knalum.KafkaScope;
import no.knalum.menu.dialog.AboutDialog;
import no.knalum.message.*;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

public class MenuBar extends JMenuBar implements MessageListener {
    private final KafkaScope kafkaScope;
    private DefaultMutableTreeNode selectedNode;
    private JMenuItem exampleJsonMenuItem;

    public MenuBar(KafkaScope kafkaScope) {
        this.kafkaScope = kafkaScope;

        add(new FileMenu());
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
