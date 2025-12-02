package no.knalum;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class MessageTable extends JPanel implements MyListener {
    private final JTable table;
    private String selectedTopic;
    private int currentPage = 0;

    public MessageTable() {
        setLayout(new BorderLayout());
        DefaultTableModel model = new DefaultTableModel(new Object[]{"Time", "Key", "Offset", "Partition", "Headers", "Value"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        this.table = new JTable(model);
        table.setCellSelectionEnabled(true);
        table.setRowSelectionAllowed(true);
        table.setColumnSelectionAllowed(true);
        setColumnWidths(table, 30, 10, 10, 10, 10, 80);

        table.addMouseListener(new MessageTableValueDialog(table));
        JScrollPane scrollPane = new JScrollPane(table);
        add(new SortPane(), BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        MessageBus.getInstance().subscribe(this);
    }

    private static void setColumnWidths(JTable table, double... percentages) {
        int tW = table.getPreferredScrollableViewportSize().width;
        TableColumnModel columnModel = table.getColumnModel();

        for (int i = 0; i < percentages.length; i++) {
            columnModel.getColumn(i).setPreferredWidth((int) (tW * percentages[i]));
        }
    }

    @Override
    public void handleMessage(AppMessage message) {
        if (message instanceof TreeTopicChanged treeTopicChanged) {
            ((DefaultTableModel) table.getModel()).setRowCount(0);
            this.selectedTopic = treeTopicChanged.topic();
            List<ConsumerRecord<String, Object>> records = AppKafkaMessageTableClient.getInstance().getRecords(selectedTopic, (SortPane.SortType) SortPane.sortChoice.getSelectedItem(), currentPage);
            setTableRecords(records);
        } else if (message instanceof NextPageMessage) {
            if (selectedTopic == null) {
                return;
            }
            int numPages = Math.toIntExact(AppKafkaClient.getNumRecords(selectedTopic) / 100);
            if (currentPage + 1 > numPages) {
                System.out.println("No more pages");
                return;
            }
            currentPage++;
            System.out.println("Current page " + currentPage);
            List<ConsumerRecord<String, Object>> records = AppKafkaMessageTableClient.getInstance().getRecords(selectedTopic, (SortPane.SortType) SortPane.sortChoice.getSelectedItem(), currentPage);
            setTableRecords(records);
        } else if (message instanceof PrevPageMessage) {
            if (selectedTopic == null) {
                return;
            }
            currentPage = Math.max(0, currentPage - 1);
            System.out.println("Current page " + currentPage);
            List<ConsumerRecord<String, Object>> records = AppKafkaMessageTableClient.getInstance().getRecords(selectedTopic, (SortPane.SortType) SortPane.sortChoice.getSelectedItem(), currentPage);
            setTableRecords(records);
        } else if (message instanceof SortOrderChangedMessage) {
            if (selectedTopic == null) {
                return;
            }
            List<ConsumerRecord<String, Object>> records = AppKafkaMessageTableClient.getInstance().getRecords(selectedTopic, (SortPane.SortType) SortPane.sortChoice.getSelectedItem(), currentPage);
            setTableRecords(records);
        }
    }

    private void setTableRecords(List<ConsumerRecord<String, Object>> records) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);
        records.forEach(record -> {
            String time = new SimpleDateFormat("dd.MM.yyy HH:mm:ss:sss").format(new Date(record.timestamp()));
            String headersString = StreamSupport.stream(record.headers().spliterator(), false)
                    .map(h -> "\"" + h.key() + "\":\"" +
                            new String(h.value(), StandardCharsets.UTF_8) + "\"")
                    .collect(Collectors.joining(", ", "{", "}"));

            Object[] newRow = {
                    time,
                    record.key(),
                    record.offset(),
                    record.partition(),
                    headersString,
                    record.value()};
            if (SortPane.sortChoice.getSelectedItem() == SortPane.SortType.Oldest) {
                model.addRow(newRow);
            } else {
                model.insertRow(0, newRow);
            }
        });

    }

}
