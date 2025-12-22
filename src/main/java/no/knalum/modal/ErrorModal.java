package no.knalum.modal;

import no.knalum.KafkaScope;
import no.knalum.menu.dialog.AppDialog;

import javax.swing.*;
import java.awt.*;

import static no.knalum.swingcomponents.Util.getCenterOfFrame;

public class ErrorModal extends AppDialog {
    public ErrorModal(Frame parent, String title, JPanel contentPane) {
        super(parent, title, contentPane);
    }

    public static void showError(String message) {
        // Always show error modal in front
        JOptionPane optionPane = new JOptionPane(message, JOptionPane.ERROR_MESSAGE);
        JDialog dialog = optionPane.createDialog(null, "Error");

        dialog.setLocation(getCenterOfFrame(KafkaScope.getInstance()));
        dialog.setModal(true);
        dialog.setAlwaysOnTop(true);
        dialog.setVisible(true);
    }

    public static void showInfo(String message) {
        // Always show info modal in front
        JOptionPane optionPane = new JOptionPane(message, JOptionPane.INFORMATION_MESSAGE);
        JDialog dialog = optionPane.createDialog(null, "Info");
        dialog.setModal(true);
        dialog.setAlwaysOnTop(true);
        dialog.setVisible(true);
    }
}
