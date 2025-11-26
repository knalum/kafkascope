package no.knalum;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class MessageBus {
    private static MessageBus instance;

    private List<MyListener> subscibers;

    private MessageBus() {
        subscibers = new ArrayList<>();
    }

    public static MessageBus getInstance() {
        if (instance == null) {
            instance = new MessageBus();
        }
        return instance;
    }

    public void subscribe(MyListener component) {
        subscibers.add(component);
    }

    public void unsubscribe(JComponent component) {
        subscibers.remove(component);
    }

    public void publish(AppMessage msg) {
        for (MyListener component : subscibers) {
            component.handleMessage(msg);
        }
    }
}
