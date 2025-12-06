package no.knalum.menu.dialog;

import no.knalum.KafkaScope;

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
                "Licensed under the Apache License, Version 2.0 (the \"License\"); you may not use this file except in compliance with the License. You may obtain a copy of the License at<br/>" +
                "<br/>" +
                "http://www.apache.org/licenses/LICENSE-2.0<br/>" +
                "Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License." +
                "<br/><br/>" +
                "Made by Konstantin Nalum 2025" +
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
        scrollPane.setPreferredSize(new java.awt.Dimension(600, 500));

        panel.add(scrollPane);
        return panel;
    }
}
