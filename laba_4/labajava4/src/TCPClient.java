import java.io.*;
import java.net.*;
import java.util.Properties;
import java.util.Scanner;

//15 вариант. 012325
//0 - TCP протокол
//1 - Реализовать в клиенте указание адреса и порта сервера, так: 1 - с консоли ввода приложения.
//2 - Реализовать указание порта для сервера, так: 2 - из командной строки
//3 - Сообщения, получаемые клиентом с сервера должны записываться в файл
//«Журнала клиента» путь к которому определяется следующим образом: 3 - из файла настроек.
//2 - Сообщения, получаемые сервером от клиента должны записываться в файл
//«Журнала сервера» путь к которому определяется следующим образом: 2 – из командной строки
//5. Сервер возвращает клиенту результат решения задачи, полученной как задание в первой лабораторной работе.
//Исходные данные с клиента передавать не менее чем за два запроса, запрещается
// передавать исходные данные на сервер за один запрос.
public class TCPClient {
    private static String clientLogPath;

    public static void main(String[] args) {
        loadConfig(); // Загружаем путь к логу из конфига

        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter server address: ");
        String serverAddress = scanner.nextLine();

        System.out.print("Enter server port: ");
        int port = Integer.parseInt(scanner.nextLine());

        try (Socket socket = new Socket(serverAddress, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // 1. Отправляем число `b`
            System.out.print("Enter number b: ");
            String b = scanner.nextLine();
            out.println(b);
            logToClientFile("Sent b: " + b);

            // 2. Отправляем последовательность `sequence`
            System.out.print("Enter sequence (space-separated): ");
            String sequence = scanner.nextLine();
            out.println(sequence);
            logToClientFile("Sent sequence: " + sequence);

            // 3. Получаем и выводим результат
            String response = in.readLine();
            System.out.println("Server response: " + response);
            logToClientFile("Received: " + response);

        } catch (UnknownHostException e) {
            System.err.println("Unknown host: " + serverAddress);
            logToClientFile("Error: Unknown host");
        } catch (IOException e) {
            System.err.println("Connection error: " + e.getMessage());
            logToClientFile("Error: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }

    private static void loadConfig() {
        Properties prop = new Properties();
        try (InputStream input = new FileInputStream("client_config.properties")) {
            prop.load(input);
            clientLogPath = prop.getProperty("log.path", "client_log.txt");
        } catch (IOException ex) {
            System.err.println("Config not found, using default log path.");
            clientLogPath = "client_log.txt";
        }
    }

    private static void logToClientFile(String message) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(clientLogPath, true))) {
            writer.println(message);
        } catch (IOException e) {
            System.err.println("Failed to write to client log: " + e.getMessage());
        }
    }
}