package no.knalum;

import javax.swing.*;

public class SortPane extends JPanel {
    public static JComboBox<SortType> sortChoice = new JComboBox<>(new SortType[]{SortType.Oldest, SortType.Newest});

    public SortPane() {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        sortChoice.addActionListener(e -> {
            if (sortChoice.getSelectedItem() == SortType.Oldest) {
            } else {
            }
            MessageBus.getInstance().publish(new SortOrderChangedMessage((SortType) sortChoice.getSelectedItem()));
        });
        add(sortChoice);
    }

    public enum SortType {
        Oldest, Newest
    }
}
