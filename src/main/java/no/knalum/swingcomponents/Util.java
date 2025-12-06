package no.knalum.swingcomponents;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;

public class Util {


    public static JTextField findTextFieldByName(Container root, String name) {
        for (Component c : root.getComponents()) {
            if (c instanceof JTextField tf && name.equals(tf.getName())) {
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
}
