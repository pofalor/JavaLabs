import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;

public class ClassroomManager extends JFrame {
    private Connection conn;
    private JTextField buildingField, roomField, nameField, areaField;
    private JTextField fullNameField, positionField, phoneField, ageField;
    private JTextArea outputArea;
    private JComboBox<String> classroomCombo, responsibleCombo;

    public ClassroomManager() {
        super("Управление учебными аудиториями");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);

        try {
            Class.forName("org.apache.derby.jdbc.ClientDriver");
            conn = DriverManager.getConnection(
                    "jdbc:derby://localhost:1527/databases/ClassroomsDB",
                    "daniil", "admin");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Ошибка подключения к БД: " + e.getMessage());
            System.exit(1);
        }

        initUI();
        loadClassrooms();
        loadResponsibles();
    }

    private void assignResponsible() {
        try {
            conn.setAutoCommit(false);

            // Получаем выбранные ID из ComboBox
            String selectedClassroom = (String) classroomCombo.getSelectedItem();
            String selectedResponsible = (String) responsibleCombo.getSelectedItem();

            int classroomId = Integer.parseInt(selectedClassroom.split(" - ")[0]);
            int responsibleId = Integer.parseInt(selectedResponsible.split(" - ")[0]);

            // Проверяем, не существует ли уже такая связь
            String checkSql = "SELECT 1 FROM CLASSROOM_RESPONSIBLES WHERE ID_CLASSROOM = ? AND ID_RESPONSIBLE = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, classroomId);
                checkStmt.setInt(2, responsibleId);

                if (checkStmt.executeQuery().next()) {
                    outputArea.append("Эта связь уже существует!\n");
                    return;
                }
            }

            // Добавляем новую связь
            String insertSql = "INSERT INTO CLASSROOM_RESPONSIBLES VALUES (?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                pstmt.setInt(1, classroomId);
                pstmt.setInt(2, responsibleId);
                pstmt.executeUpdate();
            }

            conn.commit();
            outputArea.append("Ответственный успешно назначен на аудиторию\n");
        } catch (SQLException | NumberFormatException e) {
            try {
                conn.rollback();
                outputArea.append("Ошибка при назначении ответственного: " + e.getMessage() + "\n");
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Панель для ввода данных об аудитории
        JPanel classroomPanel = new JPanel(new GridLayout(5, 2));
        classroomPanel.setBorder(BorderFactory.createTitledBorder("Данные аудитории"));
        classroomPanel.add(new JLabel("Здание:"));
        buildingField = new JTextField();
        classroomPanel.add(buildingField);
        classroomPanel.add(new JLabel("Номер аудитории:"));
        roomField = new JTextField();
        classroomPanel.add(roomField);
        classroomPanel.add(new JLabel("Наименование:"));
        nameField = new JTextField();
        classroomPanel.add(nameField);
        classroomPanel.add(new JLabel("Площадь (кв.м):"));
        areaField = new JTextField();
        classroomPanel.add(areaField);

        // Панель для ввода данных об ответственном
        JPanel responsiblePanel = new JPanel(new GridLayout(5, 2));
        responsiblePanel.setBorder(BorderFactory.createTitledBorder("Данные ответственного"));
        responsiblePanel.add(new JLabel("ФИО:"));
        fullNameField = new JTextField();
        responsiblePanel.add(fullNameField);
        responsiblePanel.add(new JLabel("Должность:"));
        positionField = new JTextField();
        responsiblePanel.add(positionField);
        responsiblePanel.add(new JLabel("Телефон:"));
        phoneField = new JTextField();
        responsiblePanel.add(phoneField);
        responsiblePanel.add(new JLabel("Возраст:"));
        ageField = new JTextField();
        responsiblePanel.add(ageField);

        // Панель кнопок - с 7 кнопками
        JPanel buttonPanel = new JPanel(new GridLayout(2, 4)); // Изменили на 2 строки

        // Первая строка кнопок
        JButton addClassroomBtn = new JButton("Добавить аудиторию");
        addClassroomBtn.addActionListener(e -> addClassroom());
        buttonPanel.add(addClassroomBtn);

        JButton addResponsibleBtn = new JButton("Добавить ответственного");
        addResponsibleBtn.addActionListener(e -> addResponsible());
        buttonPanel.add(addResponsibleBtn);

        JButton assignBtn = new JButton("Назначить ответственного");
        assignBtn.addActionListener(e -> assignResponsible());
        buttonPanel.add(assignBtn);

        JButton showAssignmentsBtn = new JButton("Показать назначения");
        showAssignmentsBtn.addActionListener(e -> showAssignments());
        buttonPanel.add(showAssignmentsBtn);

        // Вторая строка кнопок
        JButton showPhonebookBtn = new JButton("Телефонный справочник");
        showPhonebookBtn.addActionListener(e -> showPhonebook());
        buttonPanel.add(showPhonebookBtn);

        JButton avgAreaBtn = new JButton("Средняя площадь");
        avgAreaBtn.addActionListener(e -> showAverageArea());
        buttonPanel.add(avgAreaBtn);

        JButton resetBtn = new JButton("Сброс");
        resetBtn.addActionListener(e -> resetFields());
        buttonPanel.add(resetBtn);

        // Добавляем ComboBox для выбора аудиторий и ответственных
        JPanel selectionPanel = new JPanel(new GridLayout(1, 2));
        classroomCombo = new JComboBox<>();
        responsibleCombo = new JComboBox<>();

        JPanel classroomSelectPanel = new JPanel(new BorderLayout());
        classroomSelectPanel.add(new JLabel("Выберите аудиторию:"), BorderLayout.NORTH);
        classroomSelectPanel.add(classroomCombo, BorderLayout.CENTER);

        JPanel responsibleSelectPanel = new JPanel(new BorderLayout());
        responsibleSelectPanel.add(new JLabel("Выберите ответственного:"), BorderLayout.NORTH);
        responsibleSelectPanel.add(responsibleCombo, BorderLayout.CENTER);

        selectionPanel.add(classroomSelectPanel);
        selectionPanel.add(responsibleSelectPanel);

        // Панель вывода
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);

        // Компоновка главного окна
        JPanel inputPanel = new JPanel(new GridLayout(2, 1));
        inputPanel.add(classroomPanel);
        inputPanel.add(responsiblePanel);

        mainPanel.add(inputPanel, BorderLayout.NORTH);
        mainPanel.add(selectionPanel, BorderLayout.CENTER); // Добавили панель выбора
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Добавляем область вывода в отдельный скролл
        add(mainPanel);
        add(scrollPane, BorderLayout.SOUTH);

        // Загружаем данные в ComboBox
        loadClassrooms();
        loadResponsibles();
    }

    private void loadClassrooms() {
        classroomCombo.removeAllItems();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT ID_CLASSROOM, BUILDING, ROOM_NUMBER FROM CLASSROOMS")) {
            while (rs.next()) {
                classroomCombo.addItem(rs.getInt("ID_CLASSROOM") + " - " +
                        rs.getString("BUILDING") + " " +
                        rs.getString("ROOM_NUMBER"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadResponsibles() {
        responsibleCombo.removeAllItems();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT ID_RESPONSIBLE, FULL_NAME FROM RESPONSIBLES ORDER BY FULL_NAME")) {
            while (rs.next()) {
                responsibleCombo.addItem(rs.getInt("ID_RESPONSIBLE") + " - " + rs.getString("FULL_NAME"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showAssignments() {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT C.BUILDING, C.ROOM_NUMBER, R.FULL_NAME " +
                             "FROM CLASSROOMS C " +
                             "JOIN CLASSROOM_RESPONSIBLES CR ON C.ID_CLASSROOM = CR.ID_CLASSROOM " +
                             "JOIN RESPONSIBLES R ON CR.ID_RESPONSIBLE = R.ID_RESPONSIBLE " +
                             "ORDER BY R.FULL_NAME, C.BUILDING, C.ROOM_NUMBER")) {

            outputArea.setText("Текущие назначения:\n");
            while (rs.next()) {
                outputArea.append(String.format("%s (%s %s)\n",
                        rs.getString("FULL_NAME"),
                        rs.getString("BUILDING"),
                        rs.getString("ROOM_NUMBER")));
            }
        } catch (SQLException e) {
            outputArea.append("Ошибка при получении назначений: " + e.getMessage() + "\n");
        }
    }

    private void addClassroom() {
        try {
            conn.setAutoCommit(false);

            // Получаем максимальный ID для новой записи
            int newId = 1;
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT MAX(ID_CLASSROOM) FROM CLASSROOMS")) {
                if (rs.next()) {
                    newId = rs.getInt(1) + 1;
                }
            }

            // Вставляем новую аудиторию
            String sql = "INSERT INTO CLASSROOMS VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, newId);
                pstmt.setString(2, buildingField.getText());
                pstmt.setString(3, roomField.getText());
                pstmt.setString(4, nameField.getText());
                pstmt.setDouble(5, Double.parseDouble(areaField.getText()));
                pstmt.executeUpdate();
            }

            conn.commit();
            loadClassrooms();
            outputArea.append("Аудитория добавлена успешно\n");
        } catch (SQLException | NumberFormatException e) {
            try {
                conn.rollback();
                outputArea.append("Ошибка при добавлении аудитории: " + e.getMessage() + "\n");
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void addResponsible() {
        try {
            conn.setAutoCommit(false);

            // Получаем максимальный ID для новой записи
            int newId = 1;
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT MAX(ID_RESPONSIBLE) FROM RESPONSIBLES")) {
                if (rs.next()) {
                    newId = rs.getInt(1) + 1;
                }
            }

            // Получаем ID аудитории из ComboBox
            String selected = (String) classroomCombo.getSelectedItem();
            int classroomId = Integer.parseInt(selected.split(" - ")[0]);

            // Вставляем нового ответственного
            String sql = "INSERT INTO RESPONSIBLES VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, newId);
                pstmt.setString(2, fullNameField.getText());
                pstmt.setString(3, positionField.getText());
                pstmt.setString(4, phoneField.getText());
                pstmt.setInt(5, Integer.parseInt(ageField.getText()));
                pstmt.setInt(6, classroomId);
                pstmt.executeUpdate();
            }

            conn.commit();
            loadResponsibles();
            outputArea.append("Ответственный добавлен успешно\n");
        } catch (SQLException | NumberFormatException e) {
            try {
                conn.rollback();
                outputArea.append("Ошибка при добавлении ответственного: " + e.getMessage() + "\n");
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void showPhonebook() {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT DISTINCT R.FULL_NAME, R.PHONE " +
                             "FROM RESPONSIBLES R " +
                             "JOIN CLASSROOM_RESPONSIBLES CR ON R.ID_RESPONSIBLE = CR.ID_RESPONSIBLE " +
                             "ORDER BY R.FULL_NAME")) {

            outputArea.setText("Телефонный справочник (ответственные с назначениями):\n");
            while (rs.next()) {
                outputArea.append(rs.getString("FULL_NAME") + ": " + rs.getString("PHONE") + "\n");
            }
        } catch (SQLException e) {
            outputArea.append("Ошибка при получении телефонного справочника: " + e.getMessage() + "\n");
        }
    }

    private void showAverageArea() {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT R.FULL_NAME, AVG(C.AREA) as AVG_AREA " +
                             "FROM CLASSROOMS C " +
                             "JOIN CLASSROOM_RESPONSIBLES CR ON C.ID_CLASSROOM = CR.ID_CLASSROOM " +
                             "JOIN RESPONSIBLES R ON CR.ID_RESPONSIBLE = R.ID_RESPONSIBLE " +
                             "GROUP BY R.ID_RESPONSIBLE, R.FULL_NAME")) {

            outputArea.setText("Средняя площадь на ответственного:\n");
            while (rs.next()) {
                outputArea.append(String.format("%s: %.2f кв.м\n",
                        rs.getString("FULL_NAME"),
                        rs.getDouble("AVG_AREA")));
            }
        } catch (SQLException e) {
            outputArea.append("Ошибка при расчете средней площади: " + e.getMessage() + "\n");
        }
    }


    private void resetFields() {
        buildingField.setText("");
        roomField.setText("");
        nameField.setText("");
        areaField.setText("");
        fullNameField.setText("");
        positionField.setText("");
        phoneField.setText("");
        ageField.setText("");
        outputArea.setText("");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ClassroomManager app = new ClassroomManager();
            app.setVisible(true);
        });
    }
}