package no.knalum;

import com.formdev.flatlaf.themes.FlatMacLightLaf;
import no.knalum.config.BrokerConfig;
import no.knalum.config.ConfigSaver;
import no.knalum.kafka.AppKafkaClient;
import no.knalum.menu.ConnectToBrokerModal;
import no.knalum.menu.MenuBar;
import no.knalum.swingcomponents.GlobalTextPopupInstaller;
import no.knalum.ui.MainSplitPane;
import no.knalum.ui.StatusBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import static no.knalum.menu.FileMenu.getBrokerDialogSettingsConsumer;

public class KafkaScope extends JFrame {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaScope.class);
    private static KafkaScope kafkaScope;
    private static long ts1;
    private static long ts2;

    public static KafkaScope getInstance() {
        return kafkaScope;
    }

    KafkaScope() {
        setJMenuBar(new MenuBar(this));
        add(new MainSplitPane());
        add(new StatusBar(), BorderLayout.SOUTH);

        initBrokerConnectionOnStartup();
        ts2 = System.currentTimeMillis();
        LOGGER.info("Kafka Scope started at " + (ts2 - ts1) + " ms");
    }

    public static void main(String[] args) throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        FlatMacLightLaf.setup();
        ts1 = System.currentTimeMillis();
        setDockIcon();
        UIManager.setLookAndFeel("com.formdev.flatlaf.themes.FlatMacLightLaf");
        GlobalTextPopupInstaller.install();

        kafkaScope = new KafkaScope();
        kafkaScope.setLocation(700, 700);
        kafkaScope.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        kafkaScope.setSize(1000, 700);

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

    private void initBrokerConnectionOnStartup() {
        ConfigSaver.loadConfig();
        if (BrokerConfig.getInstance().getBrokerUrl() == null) {
            // Show connect modal if no config found
            new SwingWorker<>() {
                @Override
                protected Object doInBackground() throws Exception {
                    new ConnectToBrokerModal(getBrokerDialogSettingsConsumer()).setVisible(true);
                    return null;
                }
            }.execute();
        } else {
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    AppKafkaClient.connectToKafkaAndPopulateTree(BrokerConfig.getInstance().getConfig());
                    return null;
                }
            }.execute();
        }
    }
}
