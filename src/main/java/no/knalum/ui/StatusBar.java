package no.knalum.ui;

import no.knalum.message.AppMessage;
import no.knalum.message.MessageBus;
import no.knalum.message.MessageListener;
import no.knalum.message.StatusMessage;

import javax.swing.*;
import java.awt.*;

public class StatusBar extends JPanel implements MessageListener {

    private final JLabel statusLabel;

    public StatusBar() {
        setDoubleBuffered(true);
        setLayout(new BorderLayout());
        this.statusLabel = new JLabel("N/A");
        statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
        add(statusLabel, BorderLayout.WEST);

        MessageBus.getInstance().subscribe(this);
    }

    @Override
    public void handleMessage(AppMessage message) {
        if (message instanceof StatusMessage msg) {
            statusLabel.setText(msg.msg());
            updateUI();
        }
    }
}
