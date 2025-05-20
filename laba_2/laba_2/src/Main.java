//63 (вариант) 100010001
//1. В массиве число элементов меньше указанного
//5. Меньше, чем некоторое число
//9. В строке есть литеры

public class Main {
    public static void main(String[] args) {
        try {
            var arrayChecker = new ArrayChecker();
            arrayChecker.CheckLiterals(args);
            arrayChecker.CheckLen(args);
            arrayChecker.CheckLessThan(args);

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


        }
        catch (LessThanException | ArrayLessThanException | ArrayHasLiteralException ex){
            System.out.println(ex.toString());
        }
        catch (NumberFormatException e) {
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