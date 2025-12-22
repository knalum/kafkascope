package no.knalum.menu;

import no.knalum.KafkaScope;
import no.knalum.config.BrokerConfig;
import no.knalum.message.AppMessage;
import no.knalum.message.MessageBus;
import no.knalum.message.MessageListener;
import no.knalum.message.StatusMessage;
import no.knalum.swingcomponents.Util;
import org.jdesktop.swingx.prompt.PromptSupport;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

import static no.knalum.ui.rightview.TopicConfigsPanel.createRow;

public class ConnectToBrokerModal extends JDialog implements MessageListener {

    private final JPanel brokerRow;
    private final JPanel schemaRow;
    private final JButton connectBtn;

    public ConnectToBrokerModal(Consumer<BrokerDialogSettings> cb) {
        super(KafkaScope.getInstance(), "Connect to Broker", false);

        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        Dimension minimumSize = new Dimension(500, 180);
        setSize(minimumSize);
        setResizable(false);

        // Set 10px padding for the entire dialog content
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel fieldPanel = new JPanel();
        fieldPanel.setLayout(new BoxLayout(fieldPanel, BoxLayout.Y_AXIS));

        fieldPanel.add(this.brokerRow = createRow("Bootstrap servers URL", BrokerConfig.getInstance().getBrokerUrl(), true, "broker"));
        fieldPanel.add(this.schemaRow = createRow("Schema registry URL", BrokerConfig.getInstance().getSchemaRegistryUrl(), true, "schema"));

        add(fieldPanel, BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        this.connectBtn = new JButton("Connect");
        JButton cancelBtn = new JButton("Cancel");
        buttonPanel.add(cancelBtn);
        buttonPanel.add(connectBtn);
        add(buttonPanel, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(connectBtn);
        connectBtn.addActionListener(e -> {
            JTextField broker = Util.findTextFieldByName(brokerRow, "Broker");
            JTextField schema = Util.findTextFieldByName(schemaRow, "Schema");
            cb.accept(new BrokerDialogSettings(broker.getText(), schema.getText(), this));
        });
        cancelBtn.addActionListener(e -> dispose());

        MessageBus.getInstance().subscribe(this);
    }

    @Override
    public void handleMessage(AppMessage message) {
        if (message instanceof StatusMessage msg) {
            if (msg.msg().contains("Connecting")) {
                connectBtn.setEnabled(false);
                // Update UI immediately
            } else {
                connectBtn.setEnabled(true);
            }
        }
    }
}
