package laba_3;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

public class VariableProcessorObserver implements Observer {
    private final ArrayList<String> logWriter;
    private final ConsoleWriterObservable consoleWriter;

    public VariableProcessorObserver(ArrayList<String> logs, ConsoleWriterObservable cons) throws IOException {
        logWriter = logs;
        consoleWriter = cons;
    }

    @Override
    public void update(Observable o, Object arg) {
        var resourceEvent = (ResourceEvent)arg;
        String message = "[EVENT] " + resourceEvent.getMessage();
        consoleWriter.write(message);
    }
}
