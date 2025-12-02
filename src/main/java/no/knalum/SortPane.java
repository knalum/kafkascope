package no.knalum;

import javax.swing.*;

public class SortPane extends JPanel implements MyListener {
    public static JComboBox<SortType> sortChoice = new JComboBox<>(new SortType[]{SortType.Oldest, SortType.Newest});

    public SortPane() {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        sortChoice.setMaximumSize(new java.awt.Dimension(100, sortChoice.getPreferredSize().height));
        sortChoice.addActionListener(e -> MessageBus.getInstance().publish(new SortOrderChangedMessage((SortType) sortChoice.getSelectedItem())));

        add(sortChoice);
        add(Box.createHorizontalGlue()); // Add max gap between sortChoice and buttons
        add(new JButton("Prev") {{
            addActionListener(e -> MessageBus.getInstance().publish(new PrevPageMessage()));
        }});
        add(new JButton("Next") {{
            addActionListener(e -> MessageBus.getInstance().publish(new NextPageMessage()));
        }});
    }

    @Override
    public void handleMessage(AppMessage message) {
    }

    public enum SortType {
        Oldest, Newest
    }
}
