package no.knalum;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;

public class MessageTable extends JPanel implements MyListener {
    private final JTable table;
    private String selectedTopic;

    public MessageTable() {
        setLayout(new BorderLayout());
        DefaultTableModel model = new DefaultTableModel(new Object[]{"Time", "Key", "Value"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        this.table = new JTable(model);
        table.setCellSelectionEnabled(true);
        table.setRowSelectionAllowed(true);
        table.setColumnSelectionAllowed(true);
        setColumnWidths(table, 20, 20, 80);

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
            ((DefaultTableModel) table.getModel()).insertRow(0, new Object[]{recordMessage.time(), recordMessage.key(), recordMessage.payload()});
        } else if (message instanceof TreeTopicChanged treeTopicChanged) {
            ((DefaultTableModel) table.getModel()).setRowCount(0);
            this.selectedTopic = treeTopicChanged.topic();
        }
    }

}
