package laba_3;

import java.util.Observable;

public class ConsoleWriterObservable extends Observable {
    public void write(String message) {
        setChanged();
        notifyObservers(new ResourceEvent("Вывод в консоль" + System.lineSeparator() + message));
    }
}