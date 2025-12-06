package no.knalum.ui.treeview;

import org.apache.kafka.clients.admin.TopicDescription;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class TopicDescriptionsModal extends JDialog {
    public TopicDescriptionsModal(Map<String, TopicDescription> descriptions, String topic) {
        setTitle("Topic Description: " + topic);
        setModal(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(800, 600));

        var desc = descriptions.get(topic);
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body style='font-family:sans-serif;'>");
        sb.append("<h2>Topic: ").append(topic).append("</h2>");
        sb.append("<b>Partitions:</b> ").append(desc.partitions().size()).append("<br><br>");
        sb.append("<ul>");
        desc.partitions().forEach(p -> {
            sb.append("<li><b>Partition ").append(p.partition()).append(":</b> ")
                    .append("leader=").append(p.leader())
                    .append(", replicas=").append(p.replicas())
                    .append(", isr=").append(p.isr())
                    .append("</li>");
        });
        sb.append("</ul>");
        sb.append("</body></html>");

        JEditorPane editorPane = new JEditorPane("text/html", sb.toString());
        editorPane.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(editorPane);
        add(scrollPane, BorderLayout.CENTER);

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
