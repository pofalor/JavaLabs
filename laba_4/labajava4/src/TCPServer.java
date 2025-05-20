import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TCPServer {
    private static String serverLogPath;

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java Server <port> <server_log_path>");
            return;
        }

        int port = Integer.parseInt(args[0]);
        serverLogPath = args.length > 1 ? args[1] : "server_log.txt";

        // Пул потоков для обработки клиентов
        ExecutorService threadPool = Executors.newFixedThreadPool(10);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                threadPool.submit(() -> handleClient(clientSocket));
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    private static void handleClient(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            // 1. Получаем число `b` от клиента
            String bStr = in.readLine();
            logToServerFile("Received b: " + bStr);

            // 2. Получаем последовательность `sequence`
            String sequenceStr = in.readLine();
            logToServerFile("Received sequence: " + sequenceStr);

            // 3. Обрабатываем данные
            try {
                int b = Integer.parseInt(bStr);
                String[] parts = sequenceStr.split(" ");
                int[] sequence = new int[parts.length];

                for (int i = 0; i < parts.length; i++) {
                    sequence[i] = Integer.parseInt(parts[i]);
                }

                if (!isSorted(sequence)) {
                    out.println("Error: Sequence is not non-decreasing.");
                    return;
                }

                int[] result = insertAndSort(sequence, b);
                String response = Arrays.toString(result).replaceAll("[\\[\\],]", "");
                out.println(response);
                logToServerFile("Sent result: " + response);

            } catch (NumberFormatException e) {
                out.println("Error: Invalid number format.");
            }
        } catch (IOException e) {
            System.err.println("Client handling error: " + e.getMessage());
        }
    }

    private static void logToServerFile(String message) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(serverLogPath, true))) {
            writer.println(message);
        } catch (IOException e) {
            System.err.println("Failed to write to server log: " + e.getMessage());
        }
    }

    // Методы для вставки числа в последовательность
    public static int[] insertAndSort(int[] sequence, int b) {
        int[] result = new int[sequence.length + 1];
        int i = 0, j = 0;

        while (i < sequence.length && sequence[i] <= b) {
            result[j++] = sequence[i++];
        }
        result[j++] = b;
        while (i < sequence.length) {
            result[j++] = sequence[i++];
        }
        return result;
    }

    public static boolean isSorted(int[] arr) {
        for (int i = 1; i < arr.length; i++) {
            if (arr[i] < arr[i - 1]) return false;
        }
        return true;
    }
}