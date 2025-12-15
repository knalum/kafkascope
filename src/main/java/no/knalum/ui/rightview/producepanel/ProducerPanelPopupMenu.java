package no.knalum.ui.rightview.producepanel;

import no.knalum.message.AppMessage;
import no.knalum.message.MessageBus;
import no.knalum.message.MessageListener;
import no.knalum.message.TreeTopicChangedMessage;

import javax.swing.*;

public class ProducerPanelPopupMenu extends JPopupMenu implements MessageListener {
    private JMenuItem exampleJsonMenuItem;

    public ProducerPanelPopupMenu(String selectedTopic) {


        MessageBus.getInstance().subscribe(this);
    }

    @Override
    public void handleMessage(AppMessage message) {
        if (message instanceof TreeTopicChangedMessage) {
        }
    }
}
