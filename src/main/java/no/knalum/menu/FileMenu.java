package no.knalum.menu;

import no.knalum.config.BrokerConfig;
import no.knalum.config.ConfigSaver;
import no.knalum.kafka.AppKafkaClient;
import no.knalum.message.MessageBus;
import no.knalum.message.StatusMessage;
import no.knalum.modal.ErrorModal;

import javax.swing.*;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public class FileMenu extends JMenu {
    public FileMenu() {
        super("File");
        add(new JMenuItem("Connect to broker...") {{
            addActionListener(e -> new ConnectToBrokerModal(getBrokerDialogSettingsConsumer()).setVisible(true));
        }});
        add(new JMenuItem("Exit") {{
            addActionListener(e -> System.exit(0));
        }});
    }

    public static Consumer<BrokerDialogSettings> getBrokerDialogSettingsConsumer() {
        return cb -> {
            new SwingWorker<>() {
                @Override
                protected Object doInBackground() throws Exception {
                    try {
                        AppKafkaClient.connectToKafkaAndPopulateTree(cb);
                        BrokerConfig.getInstance().setUrl(cb.broker());
                        BrokerConfig.getInstance().setSchemaRegistryUrl(cb.schemaReg());
                        ConfigSaver.saveConfig();
                        cb.connectToBrokerModal().dispose();
                        return null;
                    } catch (Exception e) {
                        if (e.getCause() instanceof TimeoutException) {
                            ErrorModal.showError("Error connecting to Kafka: Timeout exception");
                        } else {
                            MessageBus.getInstance().publish(new StatusMessage("Not connected to broker"));
                            ErrorModal.showError("Error connecting to Kafka: " + e.getCause().getMessage());
                        }
                    }
                    return null;
                }
            }.execute();
        };
    }
}
