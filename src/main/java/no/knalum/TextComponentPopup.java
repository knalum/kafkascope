package no.knalum;

import javax.swing.*;
import javax.swing.text.JTextComponent;

public class TextComponentPopup {

    public static void addDefaultPopup(JTextComponent textComponent) {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem cut = new JMenuItem("Cut");
        cut.addActionListener(e -> textComponent.cut());

        JMenuItem copy = new JMenuItem("Copy");
        copy.addActionListener(e -> textComponent.copy());

        JMenuItem paste = new JMenuItem("Paste");
        paste.addActionListener(e -> textComponent.paste());

        JMenuItem selectAll = new JMenuItem("Select All");
        selectAll.addActionListener(e -> textComponent.selectAll());

        menu.add(cut);
        menu.add(copy);
        menu.add(paste);
        menu.addSeparator();
        menu.add(selectAll);

        textComponent.putClientProperty("text-popup", menu);
    }
}
