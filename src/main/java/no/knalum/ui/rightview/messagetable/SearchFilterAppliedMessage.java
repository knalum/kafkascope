package no.knalum.ui.rightview.messagetable;

import no.knalum.message.AppMessage;

import javax.swing.*;

public record SearchFilterAppliedMessage(JTextField keyField, JTextField valueField) implements AppMessage {

}
