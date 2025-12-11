package no.knalum.ui.rightview.producepanel;

import no.knalum.kafka.AppKafkaClient;
import no.knalum.message.*;
import no.knalum.swingcomponents.Util;
import no.knalum.ui.treeview.node.TopicNode;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.jdesktop.swingx.prompt.PromptSupport;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public class ProducerPanel extends JPanel implements MessageListener {
    private RSyntaxTextArea recordValue;
    private String selectedTopic;
    private JTextField keyField;
    private JComboBox partitionField;
    private JButton moreMenu;
    private JButton sendButton;

    public ProducerPanel() {
        setDoubleBuffered(true);
        setLayout(new BorderLayout());

        add(createKeySendPanel(), BorderLayout.NORTH);
        add(createValuePanel(), BorderLayout.CENTER);

        MessageBus.getInstance().subscribe(this);
        Util.setAllChildrenEnabled(false, getComponents());
    }


    private JPanel createKeySendPanel() {
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.X_AXIS));

        jPanel.add(this.keyField = new JTextField() {{
            setMaximumSize(new Dimension(2000, 25));
        }});
        PromptSupport.setPrompt("Key", keyField);

        jPanel.add(this.partitionField = new JComboBox<>(new Object[]{"0"}) {{
            setMaximumSize(new Dimension(20, 25));
            setToolTipText("Partition");
        }});

        jPanel.add(this.moreMenu = new JButton("...") {{
            setEnabled(false);
            addActionListener(e -> new ProducerPanelPopupMenu(selectedTopic).show(this, 0, this.getHeight()));
        }});

        jPanel.add(this.sendButton = new JButton("Send") {{
            setEnabled(false);
            addActionListener(e -> {
                AppKafkaClient.sendMessageToBroker(selectedTopic, keyField.getText(), recordValue.getText(), Integer.valueOf(partitionField.getSelectedItem().toString()));
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

        recordValue.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()), "sendMessage");
        recordValue.getActionMap().put("sendMessage", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                AppKafkaClient.sendMessageToBroker(selectedTopic, keyField.getText(), recordValue.getText(), Integer.valueOf(partitionField.getSelectedItem().toString()));
            }
        });
        RTextScrollPane sp = new RTextScrollPane(recordValue);

        jPanel.add(sp);


        return jPanel;
    }

    @Override
    public void handleMessage(AppMessage event) {
        if (event instanceof TreeTopicChangedMessage ev) {
            this.selectedTopic = ev.selectedNode().toString();
            Util.setAllChildrenEnabled(ev.selectedNode() instanceof TopicNode, getComponents());
        } else if (event instanceof TopicStatsMessage msg) {
            setNumPartitions(msg.topicStats().numPartitions());
        } else if (event instanceof SchemaJsonExampleMessage msg) {
            recordValue.setText(msg.schemaExampleJson());
        }
    }

    private void setNumPartitions(Integer integer) {
        partitionField.removeAllItems();
        for (int i = 0; i < integer.intValue(); i++) {
            partitionField.addItem(i);
        }
        if (partitionField.getItemCount() == 0) {
            partitionField.addItem(0);
        }
        updateUI();
    }
}
