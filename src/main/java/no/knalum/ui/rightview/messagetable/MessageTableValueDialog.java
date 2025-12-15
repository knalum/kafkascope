package no.knalum.ui.rightview.messagetable;

import no.knalum.swingcomponents.common.TextAreaDialog;

import javax.swing.*;
import java.awt.event.MouseAdapter;

public class MessageTableValueDialog extends MouseAdapter {
    private final JTable table;

    public MessageTableValueDialog(JTable table) {
        this.table = table;
    }

    @Override
    public void mouseClicked(java.awt.event.MouseEvent e) {
        if (e.getClickCount() == 2) {
            int row = table.rowAtPoint(e.getPoint());
            int col = table.columnAtPoint(e.getPoint());
            if (col == table.getColumnCount() - 1 && row != -1) {
                Object value = table.getValueAt(row, col);
                new TextAreaDialog(value.toString(), false).withTitle("Value").setVisible(true);
            }
        }
    }
}
