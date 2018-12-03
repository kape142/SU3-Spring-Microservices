package service2;

import java.util.Observable;

public class MessagePublisher extends Observable {

    public void sendMessage(String message) {
        setChanged();
        notifyObservers(message);
    }
}
