package no.knalum;

import no.knalum.config.BrokerConfig;
import no.knalum.config.ConfigSaver;
import no.knalum.kafka.AppKafkaClient;
import no.knalum.menu.FileMenuBar;
import no.knalum.swingcomponents.GlobalTextPopupInstaller;
import no.knalum.ui.MainSplitPane;
import no.knalum.ui.StatusBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.Date;

public class KafkaScope extends JFrame {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaScope.class);

    KafkaScope() {
        setJMenuBar(new FileMenuBar(this));
        add(new MainSplitPane());
        add(new StatusBar(), BorderLayout.SOUTH);
        ConfigSaver.loadConfig();
        if (!BrokerConfig.getInstance().getBrokerUrl().isEmpty()) {
            AppKafkaClient.connectToKafkaAndPopulateTree();
        }
        LOGGER.info("Kafka Scope started at " + new Date());
    }


    public static void main(String[] args) throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        setDockIcon();
        UIManager.setLookAndFeel("com.formdev.flatlaf.themes.FlatMacLightLaf");
        GlobalTextPopupInstaller.install();

        KafkaScope kafkaScope = new KafkaScope();
        kafkaScope.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        kafkaScope.setSize(1000, 700);
        kafkaScope.setLocationRelativeTo(null);
        kafkaScope.setTitle("KafkaScope \uD83D\uDD0D");
        kafkaScope.setVisible(true);

    }

    private static void setDockIcon() {
        if (Taskbar.isTaskbarSupported()) {
            Taskbar taskbar = Taskbar.getTaskbar();
            try {
                Image icon = Toolkit.getDefaultToolkit().getImage(KafkaScope.class.getResource("/logo.png"));
                taskbar.setIconImage(icon);
            } catch (UnsupportedOperationException ignored) {

            }
        }
    }
}
