package no.knalum;

import javax.swing.*;

public class RightView extends JSplitPane {
    public RightView() {
        super(VERTICAL_SPLIT,true,new MessageTable(),new ProducerPanel());
        setDoubleBuffered(true);
        setDividerLocation(400);
    }
}
