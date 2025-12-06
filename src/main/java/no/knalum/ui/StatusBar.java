package no.knalum.ui;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.beans.PropertyAdapter;
import no.knalum.config.BrokerConfig;

import javax.swing.*;
import java.awt.*;

public class StatusBar extends JPanel {

    public StatusBar() {
        setDoubleBuffered(true);
        setLayout(new BorderLayout());
        JLabel statusLabel = BasicComponentFactory.createLabel(new PropertyAdapter<>(BrokerConfig.getInstance(), "url", true));
        statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
        add(statusLabel, BorderLayout.WEST);
    }
}
