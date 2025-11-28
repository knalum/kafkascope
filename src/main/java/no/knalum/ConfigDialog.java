package no.knalum;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.beans.PropertyAdapter;

import javax.swing.*;
import java.awt.*;
import java.util.Set;

public class ConfigDialog extends JDialog {

    public ConfigDialog(KafkaScope kafkaScope) {
        super(kafkaScope, "Modal", true);
        setLayout(new BorderLayout());

        JTextField nameField = BasicComponentFactory.createTextField(new PropertyAdapter<>(BrokerConfig.getInstance(), "url", true));

        JPanel buttonPanel = new JPanel();
        JButton okButton = new JButton("Connect");
        okButton.addActionListener(e -> {
            try {
                Set<String> topics = AppKafkaClient.connect(BrokerConfig.getInstance());
                MessageBus.getInstance().publish(new ConnectedToBrokerMessage(BrokerConfig.getInstance().getBrokerUrl(), topics));
                ConfigSaver.saveConfig();
                dispose();


            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to connect to Kafka Broker: " + BrokerConfig.getInstance().getBrokerUrl() + "\n" + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton cancelButton = new JButton("Cancel");
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        add(nameField, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.SOUTH);
        cancelButton.addActionListener(e -> dispose());
        setSize(300, 120);
        setLocationRelativeTo(this);
        setVisible(true);
    }

}
