package no.knalum;

import javax.swing.*;

public class FileMenuBar extends JMenuBar {
    public FileMenuBar(MainApp mainApp) {
        JMenu menu = new JMenu("Menu");
        JMenuItem settingsItem = new JMenuItem("Settings...");
        settingsItem.addActionListener(e -> new ConfigDialog(mainApp));
        menu.add(settingsItem);

        add(menu);
    }
}
