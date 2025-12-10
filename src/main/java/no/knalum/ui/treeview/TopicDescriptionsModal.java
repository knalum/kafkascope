package no.knalum.ui.treeview;

import no.knalum.KafkaScope;
import no.knalum.menu.dialog.AppDialog;
import org.apache.kafka.clients.admin.TopicDescription;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class TopicDescriptionsModal extends AppDialog {
    public TopicDescriptionsModal(Map<String, TopicDescription> descriptions, String topic) {
        super(KafkaScope.getInstance(), "Topic description", createContent(descriptions, topic));
        setTitle("Topic Description: " + topic);
        setModal(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(500, 400));

        setLocationRelativeTo(null);
    }

    private static JScrollPane createContent(Map<String, TopicDescription> descriptions, String topic) {
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
        JPanel jPanel = new JPanel();
        jPanel.add(editorPane);
        return new JScrollPane(jPanel);
    }
}
