package no.knalum.message;

import no.knalum.ui.rightview.messagetable.SortPane;

public class SortOrderChangedMessage implements AppMessage {

    private final SortPane.SortType sortChoice;

    public SortOrderChangedMessage(SortPane.SortType sortChoice) {
        this.sortChoice = sortChoice;
    }

    public SortPane.SortType getSortChoice() {
        return sortChoice;
    }
}
