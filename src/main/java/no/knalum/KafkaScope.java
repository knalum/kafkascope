package no.knalum;

import javax.swing.*;
import java.awt.*;

public class KafkaScope extends JFrame {

    KafkaScope() {
        System.out.println("Init MainApp");
        setMenuBar(new FileMenuBar(this));
        add(new MainSplitPane());
        add(new StatusBar(), BorderLayout.SOUTH);
        ConfigSaver.loadConfig();
        if (!BrokerConfig.getInstance().getBrokerUrl().isEmpty()) {
            AppKafkaClient.connectToKafkaAndPopulateTree();
        }

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                KafkaScope.super.getRootPane().requestFocusInWindow();
            }
        });
    }


    public static void main(String[] args) throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        setDockIcon();
        UIManager.setLookAndFeel("com.formdev.flatlaf.themes.FlatMacLightLaf");
        RepaintManager.currentManager(null).setDoubleBufferingEnabled(true);

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
            } catch (UnsupportedOperationException e) {
                System.err.println("Custom Dock icon not supported");
            }
        }
    }
}
