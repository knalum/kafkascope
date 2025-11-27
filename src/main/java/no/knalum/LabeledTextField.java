package no.knalum;

import javax.swing.*;

public class LabeledTextField extends JPanel {
    private final JLabel label;
    private final JTextField textField;

    public LabeledTextField(String fieldName, String fieldValue) {
        add(this.label = new JLabel(fieldName));
        add(this.textField = new JTextField(fieldValue));

    }
}
