package no.knalum;

import javax.swing.*;
import java.awt.*;

public class ErrorModal extends AppDialog {
    public ErrorModal(Frame parent, String title, JPanel contentPane) {
        super(parent, title, contentPane);
    }

    public static void showError(String message) {
        JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void showInfo(String message) {
        JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.INFORMATION_MESSAGE);
    }
}
