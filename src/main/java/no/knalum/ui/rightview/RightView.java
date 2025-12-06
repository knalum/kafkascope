package no.knalum.ui.rightview;

import no.knalum.ui.rightview.messagetable.StatsPanel;

import javax.swing.*;

public class RightView extends JTabbedPane {
    public RightView() {
        addTab("Data", new TopicProduceConsumePanel());
        addTab("Configs", new TopicConfigsPanel());
        addTab("Stats", new StatsPanel());
    }
}
