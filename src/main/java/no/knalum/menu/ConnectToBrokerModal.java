package no.knalum.menu;

import no.knalum.KafkaScope;
import no.knalum.swingcomponents.Util;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

import static no.knalum.ui.rightview.TopicConfigsPanel.createRow;

public class ConnectToBrokerModal extends JDialog {

    private final JPanel brokerRow;
    private final JPanel schemaRow;

    public ConnectToBrokerModal(Consumer<BrokerDialogSettings> cb) {
        super(KafkaScope.getInstance(), "Connect to Broker", true);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setSize(400, 120);


        JPanel fieldPanel = new JPanel();
        fieldPanel.setLayout(new BoxLayout(fieldPanel, BoxLayout.Y_AXIS));
        //fieldPanel.add(this.brokerRow = createRow("Broker", BrokerConfig.getInstance().getBrokerUrl(), true));
        //fieldPanel.add(this.schemaRow = createRow("Schema",BrokerConfig.getInstance().getSchemaRegistryUrl(), true));

        fieldPanel.add(this.brokerRow = createRow("Broker", "localhost:29092", true));
        fieldPanel.add(this.schemaRow = createRow("Schema", "http://schema-registry:8081", true));

        add(fieldPanel, BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton connectBtn = new JButton("Connect");
        JButton cancelBtn = new JButton("Cancel");
        buttonPanel.add(cancelBtn);
        buttonPanel.add(connectBtn);
        add(buttonPanel, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(connectBtn);
        connectBtn.addActionListener(e -> {
            JTextField broker = Util.findTextFieldByName(brokerRow, "Broker");
            JTextField schema = Util.findTextFieldByName(schemaRow, "Schema");
            cb.accept(new BrokerDialogSettings(broker.getText(), schema.getText()));
            dispose();
        });
        cancelBtn.addActionListener(e -> dispose());
    }

}
