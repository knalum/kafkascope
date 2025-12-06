package no.knalum;

import javax.swing.*;

public class MainSplitPane extends JSplitPane {
    public MainSplitPane() {
        super(HORIZONTAL_SPLIT, true, new LeftTree(), new RightView());
        setDividerLocation(200);
    }
}
