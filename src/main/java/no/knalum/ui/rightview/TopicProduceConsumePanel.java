package no.knalum.ui.rightview;

import no.knalum.message.AppMessage;
import no.knalum.message.MessageBus;
import no.knalum.message.MessageListener;
import no.knalum.message.ToggleProducerPanelMessage;
import no.knalum.ui.rightview.messagetable.MessageTable;
import no.knalum.ui.rightview.producepanel.ProducerPanel;

import javax.swing.*;

public class TopicProduceConsumePanel extends JSplitPane implements MessageListener {
    public TopicProduceConsumePanel() {
        super(VERTICAL_SPLIT, true, new MessageTable(), new ProducerPanel());
        setDoubleBuffered(true);
        setDividerLocation(0.2);

        MessageBus.getInstance().subscribe(this);
    }

    @Override
    public void handleMessage(AppMessage message) {
        if (message instanceof ToggleProducerPanelMessage) {
            if (this.orientation == VERTICAL_SPLIT) {
                this.setOrientation(HORIZONTAL_SPLIT);
            } else {
                this.setOrientation(VERTICAL_SPLIT);
            }
        }
    }
}

