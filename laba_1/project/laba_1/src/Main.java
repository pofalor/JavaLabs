//63 mod 22 = 19 (вариант)
//cd C:\Users\danii\OneDrive\Документы\КАИ_ДЗ\JAVA\laba_1\project\laba_1\out\production\laba_1
//C:\Users\danii\.jdks\openjdk-23.0.2\bin\java Main
public class Main {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java Main <b> <number1> <number2> ...");
            System.out.println("Example: java Main 5 1 3 6 8 10");
            return;
        }

        try {
            int b = Integer.parseInt(args[0]);
            int[] sequence = new int[args.length - 1];

            for (int i = 1; i < args.length; i++) {
                sequence[i - 1] = Integer.parseInt(args[i]);
            }

            // Проверяем, что последовательность неубывающая
            if (!isSorted(sequence)) {
                System.out.println("Error: Input sequence is not non-decreasing.");
                return;
            }


            int[] result = insertAndSort(sequence, b);
            System.out.print("Result sequence: ");
            // Выводим результат
            for (int j : result) {
                System.out.print(j + " ");
            }
            System.out.println();

        } catch (NumberFormatException e) {
            System.out.println("Error: Invalid number format in input.");
        }
    }

    // Функция для вставки числа b в отсортированную последовательность
    public static int[] insertAndSort(int[] sequence, int b) {
        int[] result = new int[sequence.length + 1];
        int i = 0;
        int j = 0;

        while (i < sequence.length && sequence[i] <= b) {
            result[j] = sequence[i];
            i++;
            j++;
        }

        result[j] = b;
        j++;

        while (i < sequence.length) {
            result[j] = sequence[i];
            i++;
            j++;
        }

        return result;
    }


    // Функция для проверки, что последовательность неубывающая
    public static boolean isSorted(int[] arr) {
        for (int i = 1; i < arr.length; i++) {
            if (arr[i] < arr[i - 1]) {
                return false;
            }
        }
        return true;
    }
}