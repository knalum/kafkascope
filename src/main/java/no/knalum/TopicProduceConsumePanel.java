package no.knalum;

import javax.swing.*;

public class TopicProduceConsumePanel extends JSplitPane {
    public TopicProduceConsumePanel() {
        super(VERTICAL_SPLIT, true, new MessageTable(), new ProducerPanel());
        setDoubleBuffered(true);
        setDividerLocation(0.2);
    }
}

