package no.knalum;

public class SortOrderChangedMessage implements AppMessage {

    private final SortPane.SortType sortChoice;

    public SortOrderChangedMessage(SortPane.SortType sortChoice) {
        this.sortChoice = sortChoice;
    }

    public SortPane.SortType getSortChoice() {
        return sortChoice;
    }
}
