package laba_3;//63 вариант. 100010001
//1. Обращение к потоку вывода на консоль
//5. Изменение указанной переменной
//9. Обращение к потоку ввода из указанного файла
// Указание пути к фалу «журнала» - 0. С консоли
// Способ реализации событий - 1. Использование класса Observable и интерфейса Observer

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

//ТЗ:
// 1. Ввод данных выполняем либо с консоли, либо с файла
// 2. Весь вывод дублируем в файл журнала. Задание пути к файлу журнала выполняется с консоли.
// 3. Нужно все указанные выше события сгенерировать, перехватить и обработать.

public class Main {
    public static void main(String[] args) {
        try {
            PrintWriter logWriter;
            Scanner scanner = new Scanner(System.in);
            ArrayList<String> Logs = new ArrayList<>();

            var consoleWriter = new ConsoleWriterObservable();
            var consoleWriterObserver = new ConsoleWriterObserver(Logs);
            consoleWriter.addObserver(consoleWriterObserver);

            var variableProcessorObserver = new VariableProcessorObserver(Logs, consoleWriter);
            var variableProcessor = new VariableProcessorObservable();
            variableProcessor.addObserver(variableProcessorObserver);

            var fileReaderObserver = new FileReaderObserver(Logs, consoleWriter);
            var fileReaderObservable = new FileReaderObservable();
            fileReaderObservable.addObserver(fileReaderObserver);

            try{
                //Вводим путь до файла логов
                consoleWriter.write("Введите путь к файлу Журнала");
                String logPath = scanner.nextLine();

                if (logPath.isEmpty()) {
                    logPath = "default_log.txt";
                    variableProcessor.valueChanged("logPath", logPath);
                }

                consoleWriter.write("Введите путь к файлу с данными или 0 для ввода с консоли: ");
                String dataPath = scanner.nextLine();
                int[] numbers;

                if (dataPath.equals("0") || dataPath.isEmpty()) {
                    consoleWriter.write("Введите целые числа через пробел (сначала число b, потом неубывающую последовательность): ");
                    String input = scanner.nextLine();
                    String[] parts = input.split(" ");

                    numbers = new int[parts.length];
                    for (int i = 0; i < parts.length; i++) {
                        variableProcessor.valueChanged("i", Integer.toString(i));
                        numbers[i] = Integer.parseInt(parts[i]);
                    }
                    variableProcessor.valueChanged("numbers", input);
                } else {
                    numbers = fileReaderObservable.readNumbers(dataPath);
                    variableProcessor.valueChanged("numbers", Arrays.toString(numbers));
                }

                processArr(numbers, variableProcessor, consoleWriter);

                logWriter = new PrintWriter(new FileWriter(logPath, true));
                for (String log : Logs) {
                    logWriter.println(log);
                }
                logWriter.flush();
            }
            catch (FileNotFoundException e) {
                consoleWriter.write("Error: " + e.getMessage());
            } catch (NumberFormatException e) {
                consoleWriter.write("Error: Invalid input format");
            }
        }
        catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
        }
    }

    private static void processArr(int[] sequence, VariableProcessorObservable variableProcessor,
                                   ConsoleWriterObservable consoleWriter)
    {
        if (sequence.length < 2) {
            consoleWriter.write("Ошибка: длина последовательности должна быть минимум 2 символа.");
            return;
        }

        try {
            var b = sequence[0];
            sequence = Arrays.stream(sequence).skip(1).toArray();
            variableProcessor.valueChanged("sequence", Arrays.toString(sequence));

            // Проверяем, что последовательность неубывающая
            if (!isSorted(sequence, variableProcessor)) {
                consoleWriter.write("Ошибка: введена убывающая последовательность. Введите неубывающую последовательность");
                return;
            }

            int[] result = insertAndSort(sequence, b, variableProcessor);
            consoleWriter.write("Результирующая последовательность: ");
            // Выводим результат
            consoleWriter.write(Arrays.toString(result));

        } catch (NumberFormatException e) {
            consoleWriter.write("Ошибка: введёно число в неверном формате.");
        }
    }

    // Функция для вставки числа b в отсортированную последовательность
    public static int[] insertAndSort(int[] sequence, int b, VariableProcessorObservable variableProcessor) {
        int[] result = new int[sequence.length + 1];
        int i = 0;
        int j = 0;

        while (i < sequence.length && sequence[i] <= b) {
            result[j] = sequence[i];
            variableProcessor.valueChanged("result", Arrays.toString(result));
            i++;
            j++;
            variableProcessor.valueChanged("i", Integer.toString(i));
            variableProcessor.valueChanged("j", Integer.toString(j));
        }

        result[j] = b;
        variableProcessor.valueChanged("result", Arrays.toString(result));
        j++;
        variableProcessor.valueChanged("j", Integer.toString(j));

        while (i < sequence.length) {
            result[j] = sequence[i];
            variableProcessor.valueChanged("result", Arrays.toString(result));
            i++;
            j++;
            variableProcessor.valueChanged("i", Integer.toString(i));
            variableProcessor.valueChanged("j", Integer.toString(j));
        }

        return result;
    }


    // Функция для проверки, что последовательность неубывающая
    public static boolean isSorted(int[] arr, VariableProcessorObservable variableProcessor) {
        for (int i = 1; i < arr.length; i++) {
            if (arr[i] < arr[i - 1]) {
                return false;
            }
            variableProcessor.valueChanged("i", Integer.toString(i));
        }
        return true;
    }
}