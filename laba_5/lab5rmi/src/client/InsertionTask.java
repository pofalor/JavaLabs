package client;

import compute.Task;
import java.io.Serializable;

public class InsertionTask implements Task<int[]>, Serializable {
    private final int b;
    private final int[] sequence;

    public InsertionTask(int b, int[] sequence) {
        this.b = b;
        this.sequence = sequence;
    }

    public int[] execute() {
        if (!isSorted(sequence)) {
            throw new IllegalArgumentException("Input sequence is not non-decreasing");
        }

        return insertAndSort(sequence, b);
    }

    private int[] insertAndSort(int[] sequence, int b) {
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

    private boolean isSorted(int[] arr) {
        for (int i = 1; i < arr.length; i++) {
            if (arr[i] < arr[i - 1]) {
                return false;
            }
        }
        return true;
    }
}