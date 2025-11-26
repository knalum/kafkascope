package no.knalum;

import javax.swing.*;
import java.awt.*;

public class ProducerPanel extends JPanel implements MyListener {
    private final JTextArea msg;
    private String selectedTopic;

    public ProducerPanel() {
        setLayout(new BorderLayout());
        JTextArea msg1 = this.msg = new JTextArea("");
        add(new JScrollPane(msg1),BorderLayout.CENTER);
        msg.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        add(new JButton("Send"){{
            addActionListener(e ->{
                AppKafkaClient.sendMessageToBroker(selectedTopic,msg.getText());
            });
        }},BorderLayout.EAST);

        MessageBus.getInstance().subscribe(this);
    }

    @Override
    public void handleMessage(AppMessage event) {
        if(event instanceof TreeTopicChanged ev){
            this.selectedTopic = ev.topic();
        }
    }
}
