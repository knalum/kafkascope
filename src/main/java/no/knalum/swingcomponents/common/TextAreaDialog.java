package no.knalum.swingcomponents.common;

import no.knalum.KafkaScope;
import no.knalum.swingcomponents.UserSettingsConfig;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import java.awt.*;

public class TextAreaDialog extends JDialog {
    private final RSyntaxTextArea textArea;

    public TextAreaDialog(String value, boolean editable) {
        super(KafkaScope.getInstance(), "Value", true);

        this.textArea = new RSyntaxTextArea(20, 60);
        textArea.setEditable(editable);
        textArea.setText(value != null ? value : "");
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);
        textArea.setCodeFoldingEnabled(true);
        RTextScrollPane sp = new RTextScrollPane(textArea);

        add(sp);
        setSize(UserSettingsConfig.getMessageModalDimensions());
        setLocationRelativeTo(null);
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                Dimension size = getSize();
                UserSettingsConfig.setMessageModalDimensions(size);
            }
        });
        addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ESCAPE) {
                    dispose();
                }
            }
        });

        getRootPane().registerKeyboardAction(e1 -> dispose(),
                KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
    }

    public TextAreaDialog withTitle(String title) {
        setTitle(title);
        return this;
    }

    public RSyntaxTextArea getTextArea() {
        return textArea;
    }
}
