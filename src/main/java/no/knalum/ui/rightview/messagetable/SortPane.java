package no.knalum.ui.rightview.messagetable;

import no.knalum.kafka.AppKafkaMessageTableClient;
import no.knalum.message.*;
import no.knalum.swingcomponents.Util;
import no.knalum.ui.treeview.node.TopicNode;
import raven.datetime.DatePicker;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class SortPane extends JPanel implements MessageListener {
    public static JComboBox<SortType> sortChoice = new JComboBox<>(new SortType[]{SortType.Newest, SortType.Oldest, SortType.Tail,});
    private final JButton prevBtn;
    private final JButton nextBtn;
    private List<Integer> numPartitions;
    private JComboBox<String> partitionDropdown;

    public SortPane() {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        sortChoice.setMaximumSize(new java.awt.Dimension(100, sortChoice.getPreferredSize().height));
        sortChoice.addActionListener(e -> MessageBus.getInstance().publish(new SortOrderChangedMessage((SortType) sortChoice.getSelectedItem())));

        add(sortChoice);
        add(createSearchButton());
        add(createPartitionDropdown());
        add(createDatePicker());
        add(Box.createHorizontalGlue()); // Add max gap between sortChoice and buttons
        add(this.prevBtn = new JButton("Prev") {{
            setEnabled(false);
            addActionListener(e -> MessageBus.getInstance().publish(new PrevPageMessage()));
        }});
        add(this.nextBtn = new JButton("Next") {{
            setEnabled(false);
            addActionListener(e -> MessageBus.getInstance().publish(new NextPageMessage()));
        }});

        KeyStroke searchKeyStroke = KeyStroke.getKeyStroke('F', Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(searchKeyStroke, "openSearchDialog");
        getActionMap().put("openSearchDialog", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                new SearchDialog();
            }
        });

        MessageBus.getInstance().subscribe(this);
        Util.setAllChildrenEnabled(false, getComponents());
    }

    private Component createDatePicker() {
        DatePicker datePicker = new DatePicker();

        // Optionally set initial value
        datePicker.setSelectedDate(LocalDate.now());

        JFormattedTextField editor = new JFormattedTextField();
        datePicker.setEditor(editor);

        datePicker.addDateSelectionListener(d -> {
            System.out.println("Date: " + d);
            // TODO: Implement
        });

        editor.setMaximumSize(new Dimension(100, 25));
        return editor;
    }

    private Component createPartitionDropdown() {

        this.partitionDropdown = new JComboBox<>(new String[]{"All"});
        partitionDropdown.setMaximumSize(new Dimension(150, partitionDropdown.getPreferredSize().height));

        partitionDropdown.addActionListener(e -> {
            MessageBus.getInstance().publish(new PartitionFilterAppliedMessage(partitionDropdown.getSelectedItem().toString()));
        });
        return partitionDropdown;
    }

    private JButton createSearchButton() {
        JButton searchButton = new JButton("Search...");
        searchButton.addActionListener(e -> {
            new SearchDialog();
        });
        return searchButton;
    }

    @Override
    public void handleMessage(AppMessage message) {
        if (message instanceof TreeTopicChangedMessage msg) {
            Util.setAllChildrenEnabled(msg.selectedNode() instanceof TopicNode, getComponents());
            this.numPartitions = new AppKafkaMessageTableClient().getNumberOfPartitions(msg.selectedNode().toString());
            String partitionsJoined = numPartitions == null ? "" : "All," + String.join(",", numPartitions.stream().map(String::valueOf).toList());

            String[] split = partitionsJoined.split(",");

            partitionDropdown.setModel(new DefaultComboBoxModel<>(partitionsJoined.isEmpty() ? new String[]{"All"} : split));
        }
    }

    public static SortType getSortChoice() {
        return (SortType) sortChoice.getSelectedItem();
    }

    public enum SortType {
        Tail, Oldest, Newest
    }


}
