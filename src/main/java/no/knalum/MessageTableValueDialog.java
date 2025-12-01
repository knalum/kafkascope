package no.knalum;

import javax.swing.*;
import java.awt.*;
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
                JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(table), "Value", true);
                JTextArea textArea = new JTextArea(value != null ? value.toString() : "");
                textArea.setEditable(false);
                dialog.add(new JScrollPane(textArea));
                dialog.setSize(500, 300);
                dialog.setLocationRelativeTo(null);
                dialog.setVisible(true);
            }
        }
    }
}
