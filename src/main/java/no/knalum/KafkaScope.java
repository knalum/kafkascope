package no.knalum;

import com.formdev.flatlaf.themes.FlatMacLightLaf;
import no.knalum.config.BrokerConfig;
import no.knalum.config.ConfigSaver;
import no.knalum.kafka.AppKafkaClient;
import no.knalum.menu.MenuBar;
import no.knalum.swingcomponents.GlobalTextPopupInstaller;
import no.knalum.ui.MainSplitPane;
import no.knalum.ui.StatusBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

import static no.knalum.swingcomponents.Util.centerMainFrame;

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
        ConfigSaver.loadConfig();
        if (BrokerConfig.getInstance().getBrokerUrl() != null) {
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    AppKafkaClient.connectToKafkaAndPopulateTree(BrokerConfig.getInstance().getConfig());
                    return null;
                }
            }.execute();
        }
        ts2 = System.currentTimeMillis();
        LOGGER.info("Kafka Scope started at " + (ts2 - ts1) + " ms");
    }


    public static void main(String[] args) throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        FlatMacLightLaf.setup();
        ts1 = System.currentTimeMillis();
        setDockIcon();
        GlobalTextPopupInstaller.install();

        kafkaScope = new KafkaScope();
        centerMainFrame(kafkaScope);
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
