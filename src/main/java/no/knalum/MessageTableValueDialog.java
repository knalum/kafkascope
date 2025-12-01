package no.knalum;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

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

                RSyntaxTextArea textArea = new RSyntaxTextArea(20, 60);
                textArea.setEditable(false);
                textArea.setText(value != null ? value.toString() : "");
                textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);
                textArea.setCodeFoldingEnabled(true);
                RTextScrollPane sp = new RTextScrollPane(textArea);

                dialog.add(sp);
                dialog.setSize(UserSettingsConfig.getMessageModalDimensions());
                dialog.setLocationRelativeTo(null);
                dialog.addComponentListener(new java.awt.event.ComponentAdapter() {
                    @Override
                    public void componentResized(java.awt.event.ComponentEvent e) {
                        Dimension size = dialog.getSize();
                        UserSettingsConfig.setMessageModalDimensions(size);
                    }
                });
                dialog.addKeyListener(new java.awt.event.KeyAdapter() {
                    @Override
                    public void keyPressed(java.awt.event.KeyEvent e) {
                        if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ESCAPE) {
                            dialog.dispose();
                        }
                    }
                });
                // Register ESC key to close dialog even if not focused
                dialog.getRootPane().registerKeyboardAction(e1 -> dialog.dispose(),
                        KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0),
                        JComponent.WHEN_IN_FOCUSED_WINDOW);
                dialog.setFocusable(true);
                dialog.setFocusTraversalKeysEnabled(false);
                dialog.setVisible(true);
            }
        }
    }
}
