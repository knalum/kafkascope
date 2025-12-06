package no.knalum.ui.rightview.messagetable;

import no.knalum.message.*;
import no.knalum.swingcomponents.Util;
import no.knalum.ui.treeview.node.TopicNode;

import javax.swing.*;

public class SortPane extends JPanel implements MessageListener {
    public static JComboBox<SortType> sortChoice = new JComboBox<>(new SortType[]{SortType.Newest, SortType.Oldest, SortType.Tail,});
    private final JButton prevBtn;
    private final JButton nextBtn;

    public SortPane() {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        sortChoice.setMaximumSize(new java.awt.Dimension(100, sortChoice.getPreferredSize().height));
        sortChoice.addActionListener(e -> MessageBus.getInstance().publish(new SortOrderChangedMessage((SortType) sortChoice.getSelectedItem())));

        add(sortChoice);
        add(Box.createHorizontalGlue()); // Add max gap between sortChoice and buttons
        add(this.prevBtn = new JButton("Prev") {{
            setEnabled(false);
            addActionListener(e -> MessageBus.getInstance().publish(new PrevPageMessage()));
        }});
        add(this.nextBtn = new JButton("Next") {{
            setEnabled(false);
            addActionListener(e -> MessageBus.getInstance().publish(new NextPageMessage()));
        }});

        MessageBus.getInstance().subscribe(this);
        Util.setAllChildrenEnabled(false, getComponents());
    }

    @Override
    public void handleMessage(AppMessage message) {
        if (message instanceof TreeTopicChangedMessage msg) {
            Util.setAllChildrenEnabled(msg.selectedNode() instanceof TopicNode, getComponents());
        }
    }

    public static SortType getSortChoice() {
        return (SortType) sortChoice.getSelectedItem();
    }

    public enum SortType {
        Tail, Oldest, Newest
    }


}
