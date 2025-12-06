package no.knalum.ui;

import no.knalum.ui.rightview.RightView;
import no.knalum.ui.treeview.TopicTreeView;

import javax.swing.*;

public class MainSplitPane extends JSplitPane {
    public MainSplitPane() {
        super(HORIZONTAL_SPLIT, true, new TopicTreeView(), new RightView());
        setDividerLocation(200);
    }
}
