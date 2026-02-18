package no.knalum.ui.rightview.messagetable;

import no.knalum.kafka.AppKafkaClient;
import no.knalum.kafka.AppKafkaMessageTableClient;
import no.knalum.message.*;
import no.knalum.swingcomponents.common.TextAreaDialog;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static no.knalum.kafka.AppKafkaMessageTableClient.recordMatchesFilter;

public class MessageTable extends JPanel implements MessageListener {
    private final JTable table;
    private String selectedTopic;
    private int currentPage = 0;
    private SwingWorker<Void, Void> activeWorker;
    private SearchFilter searchFilter = new SearchFilter();

    public MessageTable() {
        setDoubleBuffered(true);
        setLayout(new BorderLayout());
        DefaultTableModel model = new DefaultTableModel(new Object[]{"Time", "Key", "Offset", "Partition", "Headers", "Value"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        this.table = new JTable(model);
        table.getTableHeader().setReorderingAllowed(false);
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        table.setCellSelectionEnabled(true);
        table.setRowSelectionAllowed(true);
        table.setColumnSelectionAllowed(true);
        setColumnWidths(table, 30, 10, 10, 10, 10, 80);

        table.addMouseListener(new MessageTableValueDialog(table));
        JScrollPane scrollPane = new JScrollPane(table);
        add(new SortPane(), BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Add key binding for Enter on Value column
        KeyStroke enterKey = KeyStroke.getKeyStroke("ENTER");
        table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(enterKey, "openValueDialog");
        table.getActionMap().put("openValueDialog", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int row = table.getSelectedRow();
                int col = table.getSelectedColumn();
                // Value column is index 5
                if (row != -1 && col == 5) {
                    Object value = table.getValueAt(row, col);
                    new TextAreaDialog(value.toString(), false).setVisible(true);
                }
            }
        });

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
        if (message instanceof TreeTopicChangedMessage treeTopicChangedMessage) {
            this.selectedTopic = treeTopicChangedMessage.selectedNode().toString();
            subscribeOrGetFromKafka(selectedTopic);
        } else if (message instanceof NextPageMessage) {
            if (selectedTopic == null) {
                return;
            }
            int numPages = Math.toIntExact(AppKafkaClient.getNumRecords(selectedTopic) / 100);
            if (currentPage + 1 > numPages) {
                return;
            }
            currentPage++;

            subscribeOrGetFromKafka(selectedTopic);
        } else if (message instanceof PrevPageMessage) {
            if (selectedTopic == null) {
                return;
            }
            currentPage = Math.max(0, currentPage - 1);

            subscribeOrGetFromKafka(selectedTopic);
        } else if (message instanceof SortOrderChangedMessage) {
            if (selectedTopic == null) {
                return;
            }
            subscribeOrGetFromKafka(selectedTopic);
        } else if (message instanceof SearchFilterAppliedMessage) {
            if (selectedTopic == null) {
                return;
            }
            this.searchFilter.setValue(((SearchFilterAppliedMessage) message).valueField().getText());
            this.searchFilter.setKey(((SearchFilterAppliedMessage) message).keyField().getText());
            subscribeOrGetFromKafka(selectedTopic);
        }else if(message instanceof PartitionFilterAppliedMessage){
            this.searchFilter.setPartition(((PartitionFilterAppliedMessage) message).partition());
            subscribeOrGetFromKafka(selectedTopic);
        }
    }

    private void subscribeOrGetFromKafka(String selectedTopic) {
        ((DefaultTableModel) table.getModel()).setRowCount(0);
        // Cancel any previous worker
        if (activeWorker != null && !activeWorker.isDone()) {
            activeWorker.cancel(true);
        }
        activeWorker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                if (SortPane.getSortChoice() == SortPane.SortType.Tail) {
                    DefaultTableModel model = (DefaultTableModel) table.getModel();
                    AppKafkaMessageTableClient.getInstance().subscribe(selectedTopic, (ConsumerRecords<String, Object> o) -> {
                        for (ConsumerRecord<String, Object> record : o) {
                            if (isCancelled()) break;
                            if (recordMatchesFilter(record, searchFilter)) {
                                SwingUtilities.invokeLater(() -> model.insertRow(0, recordToObjectRow(record)));
                            }
                        }
                    });
                } else {
                    List<ConsumerRecord<String, Object>> records = AppKafkaMessageTableClient.getInstance().getRecords(selectedTopic, (SortPane.SortType) SortPane.sortChoice.getSelectedItem(), currentPage, searchFilter);
                    SwingUtilities.invokeLater(() -> setTableRecords(records));

                    if (SortPane.getSortChoice() == SortPane.SortType.Newest) {
                        DefaultTableModel model = (DefaultTableModel) table.getModel();
                        AppKafkaMessageTableClient.getInstance().subscribe(selectedTopic, (ConsumerRecords<String, Object> o) -> {
                            for (ConsumerRecord<String, Object> record : o) {
                                if (isCancelled()) break;
                                if (recordMatchesFilter(record, searchFilter)) {
                                    SwingUtilities.invokeLater(() -> model.insertRow(0, recordToObjectRow(record)));
                                }
                            }
                        });
                    }
                }
                return null;
            }
        };
        activeWorker.execute();
    }

    private void setTableRecords(List<ConsumerRecord<String, Object>> records) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        records.forEach(record -> {
            Object[] newRow = recordToObjectRow(record);
            if (SortPane.sortChoice.getSelectedItem() == SortPane.SortType.Oldest) {
                model.addRow(newRow);
            } else {
                model.insertRow(0, newRow);
            }
        });

    }

    private static Object[] recordToObjectRow(ConsumerRecord<String, Object> record) {
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
        return newRow;
    }

}
