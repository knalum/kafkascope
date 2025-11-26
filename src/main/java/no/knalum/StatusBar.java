package no.knalum;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.beans.PropertyAdapter;

import javax.swing.*;
import java.awt.*;

public class StatusBar extends JPanel {

    public StatusBar() {
        setLayout(new BorderLayout());
        setDoubleBuffered(true);
        JLabel statusLabel = BasicComponentFactory.createLabel(new PropertyAdapter<>(BrokerConfig.getInstance(),"url",true));
        statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
        add(statusLabel, BorderLayout.WEST);
    }
}
