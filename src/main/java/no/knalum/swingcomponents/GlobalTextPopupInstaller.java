package no.knalum.swingcomponents;

import no.knalum.swingcomponents.common.TextComponentPopup;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.MouseEvent;

public class GlobalTextPopupInstaller {

    public static void install() {

        Toolkit.getDefaultToolkit().addAWTEventListener(event -> {
            if (!(event instanceof MouseEvent)) return;

            MouseEvent e = (MouseEvent) event;
            if (!e.isPopupTrigger()) return;

            Component src = e.getComponent();
            if (src == null) return;

            Component deepest = SwingUtilities.getDeepestComponentAt(
                    src, e.getX(), e.getY()
            );

            // Only apply to JTextField (or change to JTextComponent)
            if (deepest instanceof JTextField) {

                JTextComponent tc = (JTextComponent) deepest;

                // Make sure popup installed once per component
                if (tc.getClientProperty("text-popup-installed") == null) {
                    TextComponentPopup.addDefaultPopup(tc);
                    tc.putClientProperty("text-popup-installed", true);
                }

                // Retrieve the menu you stored earlier
                JPopupMenu menu = (JPopupMenu) tc.getClientProperty("text-popup");
                if (menu != null) {
                    menu.show(tc, e.getX(), e.getY());
                }
            }

        }, AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
    }
}
