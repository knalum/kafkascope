package no.knalum.menu;

import no.knalum.config.BrokerConfig;
import no.knalum.config.ConfigSaver;
import no.knalum.kafka.AppKafkaClient;

import javax.swing.*;

public class FileMenu extends JMenu {
    public FileMenu() {
        super("File");
        add(new JMenuItem("Connect to broker...") {{
            addActionListener(e -> new ConnectToBrokerModal(cb -> {
                AppKafkaClient.connectToKafkaAndPopulateTree(cb);
                BrokerConfig.getInstance().setUrl(cb.broker());
                BrokerConfig.getInstance().setSchemaRegistryUrl(cb.schemaReg());
                ConfigSaver.saveConfig();
            }).setVisible(true));
        }});
        add(new JMenuItem("Exit") {{
            addActionListener(e -> System.exit(0));

        }});

    }
}
