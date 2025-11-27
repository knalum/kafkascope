package no.knalum;

import org.jdesktop.swingx.prompt.PromptSupport;

import javax.swing.*;
import java.awt.*;

public class ProducerPanel extends JPanel implements MyListener {
    private JTextArea recordValue;
    private String selectedTopic;
    private JTextField keyField;

    public ProducerPanel() {
        setLayout(new BorderLayout());

        add(createKeyValuePanel(), BorderLayout.CENTER);
        add(createSendPanel(), BorderLayout.SOUTH);

        MessageBus.getInstance().subscribe(this);
    }

    private JPanel createKeyValuePanel() {
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.Y_AXIS));
        jPanel.add(this.keyField = new JTextField() {{
            setMaximumSize(new Dimension(2000, 20));
        }});
        PromptSupport.setPrompt("Key", keyField);

        this.recordValue = new JTextArea("");
        PromptSupport.setPrompt("Value", this.recordValue);
        jPanel.add(new JScrollPane(recordValue));


        return jPanel;
    }

    private JPanel createSendPanel() {
        JPanel jPanel = new JPanel(new BorderLayout());
        jPanel.add(new JButton("Send") {{
            addActionListener(e -> {
                AppKafkaClient.sendMessageToBroker(selectedTopic, keyField.getText(), recordValue.getText());
            });
        }}, BorderLayout.EAST);
        return jPanel;
    }

    @Override
    public void handleMessage(AppMessage event) {
        if (event instanceof TreeTopicChanged ev) {
            this.selectedTopic = ev.topic();
        }
    }
}
