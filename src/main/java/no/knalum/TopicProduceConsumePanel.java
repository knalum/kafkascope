package no.knalum;

import javax.swing.*;

public class TopicProduceConsumePanel extends JSplitPane implements MyListener {
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

