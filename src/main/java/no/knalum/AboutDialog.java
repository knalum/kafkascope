package no.knalum;

import javax.swing.*;

public class AboutDialog extends AppDialog {
    public AboutDialog(KafkaScope kafkaScope) {
        super(kafkaScope, "About", createJPanel());
        setSize(800, 800);
    }

    private static JPanel createJPanel() {
        JPanel panel = new JPanel();
        JEditorPane editorPane = new JEditorPane();
        editorPane.setContentType("text/html"); // important
        editorPane.setEditable(false);          // read-only

        String html = "<html>" +
                "<h2>KafkaScope</h2>" +
                "</html>";

        editorPane.setText(html);
        editorPane.addHyperlinkListener(e -> {
            if (e.getEventType() == javax.swing.event.HyperlinkEvent.EventType.ACTIVATED) {
                try {
                    java.awt.Desktop.getDesktop().browse(e.getURL().toURI());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        // Make scrollable
        JScrollPane scrollPane = new JScrollPane(editorPane);
        scrollPane.setPreferredSize(new java.awt.Dimension(400, 200));

        panel.add(scrollPane);
        return panel;
    }
}
