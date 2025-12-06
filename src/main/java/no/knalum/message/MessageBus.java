package no.knalum.message;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class MessageBus {
    private static MessageBus instance;

    private List<MessageListener> subscibers;

    private MessageBus() {
        subscibers = new ArrayList<>();
    }

    public static MessageBus getInstance() {
        if (instance == null) {
            instance = new MessageBus();
        }
        return instance;
    }

    public void subscribe(MessageListener component) {
        subscibers.add(component);
    }

    public void unsubscribe(JComponent component) {
        subscibers.remove(component);
    }

    public void publish(AppMessage msg) {
        for (MessageListener component : subscibers) {
            component.handleMessage(msg);
        }
    }
}
