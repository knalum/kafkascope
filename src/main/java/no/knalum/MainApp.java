package no.knalum;

import javax.swing.*;
import java.awt.*;
import java.util.Set;

public class MainApp extends JFrame {

    MainApp() {
        setJMenuBar(new FileMenuBar(this));
        add(new MainSplitPane());
        add(new StatusBar(), BorderLayout.SOUTH);
        ConfigSaver.loadConfig();
        if(!BrokerConfig.getInstance().getUrl().isEmpty()){
            connectToKafkaAndPopulateTree();
        }


    }

    private static void connectToKafkaAndPopulateTree() {
        System.out.println("Try to connect at init "+BrokerConfig.getInstance().getUrl());
        try {
            Set<String> topics = AppKafkaClient.connect(BrokerConfig.getInstance());
            MessageBus.getInstance().publish(new ConnectedToBrokerMessage(BrokerConfig.getInstance().getUrl(), topics));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static void main(String[] args) throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException {

        String info = String.format(
                "Branch: %s\nTag: %s\nCommit: %s",
                GitInfo.getBranch(),
                GitInfo.getGitTag(),
                GitInfo.getCommit()
        );
        System.out.println(info);
        /*
        UIManager.setLookAndFeel("com.formdev.flatlaf.themes.FlatMacLightLaf");
        RepaintManager.currentManager(null).setDoubleBufferingEnabled(true);

        MainApp mainApp = new MainApp();
        mainApp.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        mainApp.setSize(700, 500);
        mainApp.setLocationRelativeTo(null);
        mainApp.setTitle("KafkaScope");
        mainApp.setVisible(true);

         */
    }
}
