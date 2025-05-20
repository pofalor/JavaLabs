package laba_3;

import java.util.Observable;

public class VariableProcessorObservable extends Observable {

    public void valueChanged(String variableName, String newValue) {
        setChanged();
        notifyObservers(new ResourceEvent(String.format("Изменение переменной: %s. Новое значение: %s",
                variableName, newValue)));
    }
}
