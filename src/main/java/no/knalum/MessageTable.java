package no.knalum;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class MessageTable extends JPanel implements MyListener {
    private final JTable table;
    private String selectedTopic;

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
        setColumnWidths(table, 20, 10, 10, 10, 10, 80);

        JScrollPane scrollPane = new JScrollPane(table);
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
        if (message instanceof RecordConsumed recordMessage) {
            String time = new SimpleDateFormat("HH:mm:ss").format(new Date(recordMessage.record().timestamp()));
            String headersString = StreamSupport.stream(recordMessage.record().headers().spliterator(), false)
                    .map(h -> "\"" + h.key() + "\":\"" +
                            new String(h.value(), StandardCharsets.UTF_8) + "\"")
                    .collect(Collectors.joining(", ", "{", "}"));

            ((DefaultTableModel) table.getModel()).insertRow(0, new Object[]{
                    time,
                    recordMessage.record().key(),
                    recordMessage.record().offset(),
                    recordMessage.record().partition(),
                    headersString,
                    recordMessage.record().value()});
        } else if (message instanceof TreeTopicChanged treeTopicChanged) {
            ((DefaultTableModel) table.getModel()).setRowCount(0);
            this.selectedTopic = treeTopicChanged.topic();
        }
    }

}
