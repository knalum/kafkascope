package no.knalum.swingcomponents;

import no.knalum.KafkaScope;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;

public class Util {


    public static Point getCenterOfFrame(JFrame frame) {
        // Get the location of the frame on screen
        Point location = frame.getLocationOnScreen();
        // Get the size of the frame
        Dimension size = frame.getSize();
        // Calculate the center point
        int x = location.x + size.width / 2;
        int y = location.y + size.height / 2;
        return new Point(x, y);
    }

    public static JTextField findTextFieldByName(Container root, String name) {
        for (Component c : root.getComponents()) {
            if (c instanceof JTextField tf && name.toLowerCase().equals(tf.getName().toLowerCase())) {
                return tf;
            }
            if (c instanceof Container) {
                JTextField child = findTextFieldByName((Container) c, name);
                if (child != null) return child;
            }
        }
        return null;
    }


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


    // Helper method to find a node by name and return its TreePath
    public static TreePath findNodeByName(DefaultMutableTreeNode root, String name) {
        return findNodeByNameRecursive(new TreePath(root), name);
    }

    public static TreePath findNodeByNameRecursive(TreePath parent, String name) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) parent.getLastPathComponent();
        if (node.getUserObject() != null && node.getUserObject().toString().equals(name)) {
            return parent;
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            TreePath path = parent.pathByAddingChild(node.getChildAt(i));
            TreePath result = findNodeByNameRecursive(path, name);
            if (result != null) return result;
        }
        return null;
    }

    public static void silentSleep(int timeoutMs) {
        try {
            Thread.sleep(timeoutMs);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
