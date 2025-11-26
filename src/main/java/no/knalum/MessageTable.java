package no.knalum;

import javax.swing.*;
import java.awt.*;
import javax.swing.table.DefaultTableModel;

public class MessageTable extends JPanel implements MyListener{
    private final JTable table;

    public MessageTable() {
        setLayout(new BorderLayout());
        DefaultTableModel model = new DefaultTableModel(new Object[]{"Time", "Payload"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        this.table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        MessageBus.getInstance().subscribe(this);
    }

    @Override
    public void handleMessage(AppMessage message) {

        if(message instanceof RecordConsumed recordMessage){
            ((DefaultTableModel)table.getModel()).insertRow(0,new Object[]{recordMessage.time(), recordMessage.payload()});
        }else if(message instanceof TreeTopicChanged treeTopicChanged){
            ((DefaultTableModel)table.getModel()).setRowCount(0);
        }
    }
}
