package laba_3;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Scanner;

public class FileReaderObservable extends Observable {
    public int[] readNumbers(String path) throws FileNotFoundException {
        setChanged();
        notifyObservers(new ResourceEvent("Чтение из файла: " + path));

        List<Integer> numbers = new ArrayList<>();
        try (Scanner scanner = new Scanner(new File(path))) {
            while (scanner.hasNextInt()) {
                numbers.add(scanner.nextInt());
            }
        }
        //преобразуем список в массив
        return numbers.stream().mapToInt(x -> x).toArray();
    }
}