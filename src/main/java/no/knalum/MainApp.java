package no.knalum;

import javax.swing.*;
import java.awt.*;

public class MainApp extends JFrame {

    MainApp() {
        System.out.println("Init MainApp");
        setMenuBar(new FileMenuBar(this));
        add(new MainSplitPane());
        add(new StatusBar(), BorderLayout.SOUTH);
        ConfigSaver.loadConfig();
        if (!BrokerConfig.getInstance().getUrl().isEmpty()) {
            AppKafkaClient.connectToKafkaAndPopulateTree();
        }

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                MainApp.super.getRootPane().requestFocusInWindow();
            }
        });
    }


    public static void main(String[] args) throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        setDockIcon();
        UIManager.setLookAndFeel("com.formdev.flatlaf.themes.FlatMacLightLaf");
        RepaintManager.currentManager(null).setDoubleBufferingEnabled(true);

        MainApp mainApp = new MainApp();
        mainApp.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        mainApp.setSize(1000, 700);
        mainApp.setLocationRelativeTo(null);
        mainApp.setTitle("KafkaScope \uD83D\uDD0D");
        mainApp.setVisible(true);

    }

    private static void setDockIcon() {
        if (Taskbar.isTaskbarSupported()) {
            Taskbar taskbar = Taskbar.getTaskbar();
            try {
                Image icon = Toolkit.getDefaultToolkit().getImage(MainApp.class.getResource("/logo.png"));
                taskbar.setIconImage(icon);
            } catch (UnsupportedOperationException e) {
                System.err.println("Custom Dock icon not supported");
            }
        }
    }
}
