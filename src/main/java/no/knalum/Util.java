package no.knalum;

import java.awt.*;

public class Util {


    public static void setAllChildrenEnabled(boolean enabled, Component[] components) {
        for (Component c : components) {
            c.setEnabled(enabled);
            if (c instanceof Container container) {
                for (Component child : container.getComponents()) {
                    child.setEnabled(enabled);
                }
            }
        }
    }
}
