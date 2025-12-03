package no.knalum;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.jdesktop.swingx.prompt.PromptSupport;

import javax.swing.*;
import java.awt.*;

public class ProducerPanel extends JPanel implements MyListener {
    private RSyntaxTextArea recordValue;
    private String selectedTopic;
    private JTextField keyField;

    public ProducerPanel() {
        setLayout(new BorderLayout());

        add(createKeySendPanel(), BorderLayout.NORTH);
        add(createValuePanel(), BorderLayout.CENTER);

        MessageBus.getInstance().subscribe(this);
    }

    private JPanel createKeySendPanel() {
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.X_AXIS));
        jPanel.add(this.keyField = new JTextField() {{
            setMaximumSize(new Dimension(2000, 25));
        }});
        PromptSupport.setPrompt("Key", keyField);


        jPanel.add(new JButton("Send") {{
            addActionListener(e -> {
                AppKafkaClient.sendMessageToBroker(selectedTopic, keyField.getText(), recordValue.getText());
            });
        }});

        return jPanel;
    }

    private JPanel createValuePanel() {
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.Y_AXIS));

        recordValue = new RSyntaxTextArea(4, 60);
        PromptSupport.setPrompt("Value", this.recordValue);
        recordValue.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);
        recordValue.setCodeFoldingEnabled(true);

        RTextScrollPane sp = new RTextScrollPane(recordValue);

        jPanel.add(sp);


        return jPanel;
    }

    @Override
    public void handleMessage(AppMessage event) {
        if (event instanceof TreeTopicChanged ev) {
            this.selectedTopic = ev.selectedNode().toString();
        }
    }
}
