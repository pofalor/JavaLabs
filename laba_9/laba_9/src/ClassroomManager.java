import javax.swing.*;
import javax.swing.table.DefaultTableModel;
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

        // Инициализация подключения к БД
        initializeDatabaseConnection();

        initUI();

        // Правильный порядок завершения инициализации
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initializeDatabaseConnection() {
        try {
            Class.forName("org.apache.derby.jdbc.ClientDriver");
            conn = DriverManager.getConnection(
                    "jdbc:derby://localhost:1527/databases/ClassroomsDB",
                    "daniil", "admin");
            checkAndInitializeDatabase();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Ошибка подключения: " + e.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void checkAndInitializeDatabase() throws SQLException {
        // Проверяем, есть ли данные в основных таблицах
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT total FROM (" +
                             "VALUES ((SELECT COUNT(*) FROM CLASSROOMS) + (SELECT COUNT(*) FROM RESPONSIBLES))" +
                             ") AS t(total)")) {
            if (rs.next() && rs.getInt("total") == 0) {
                resetToDefaults();
            }
        }
    }


    private void resetToDefaults() {
        try {
            conn.setAutoCommit(false);

            // 2. Добавление аудиторий по умолчанию
            String insertClassroom = "INSERT INTO CLASSROOMS (BUILDING, ROOM_NUMBER, NAME, AREA) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertClassroom)) {

                for (Object[] room : DefaultValues.DEFAULT_CLASSROOMS) {
                    pstmt.setString(1, (String)room[0]);
                    pstmt.setString(2, (String)room[1]);
                    pstmt.setString(3, (String)room[2]);
                    pstmt.setDouble(4, (Double)room[3]);
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            }

            // 3. Добавление ответственных по умолчанию
            String insertResponsible = "INSERT INTO RESPONSIBLES (FULL_NAME, POSITION, PHONE, AGE) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertResponsible)) {

                for (Object[] person : DefaultValues.DEFAULT_RESPONSIBLES) {
                    pstmt.setString(1, (String)person[0]);
                    pstmt.setString(2, (String)person[1]);
                    pstmt.setString(3, (String)person[2]);
                    pstmt.setInt(4, (Integer)person[3]);
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            }

            // 4. Добавление назначений по умолчанию
            String insertAssignment = "INSERT INTO CLASSROOM_RESPONSIBLES (ID_CLASSROOM, ID_RESPONSIBLE) " +
                    "VALUES ((SELECT ID_CLASSROOM FROM CLASSROOMS WHERE BUILDING=? AND ROOM_NUMBER=?), " +
                    "(SELECT ID_RESPONSIBLE FROM RESPONSIBLES WHERE FULL_NAME=?))";
            try (PreparedStatement pstmt = conn.prepareStatement(insertAssignment)) {

                for (Object[] assign : DefaultValues.DEFAULT_ASSIGNMENTS) {
                    pstmt.setString(1, (String)assign[0]);
                    pstmt.setString(2, (String)assign[1]);
                    pstmt.setString(3, (String)assign[2]);
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            }

            conn.commit();

            // Обновляем списки в интерфейсе
            loadClassrooms();
            loadResponsibles();

        } catch (SQLException e) {
            try {
                conn.rollback();
                JOptionPane.showMessageDialog(this,
                        "Ошибка при загрузке данных по умолчанию: " + e.getMessage(),
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
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

    private void assignResponsible() {
        if (classroomCombo.getSelectedItem() == null || responsibleCombo.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Выберите аудиторию и ответственного");
            return;
        }

        try {
            conn.setAutoCommit(false);

            // Получаем выбранные ID
            int classroomId = getIdFromCombo(classroomCombo.getSelectedItem());
            int responsibleId = getIdFromCombo(responsibleCombo.getSelectedItem());

            // Проверяем, есть ли уже ответственный у этой аудитории
            String checkSql = "SELECT 1 FROM CLASSROOM_RESPONSIBLES WHERE ID_CLASSROOM = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, classroomId);
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    // Если ответственный уже есть - переназначаем
                    String updateSql = "UPDATE CLASSROOM_RESPONSIBLES SET ID_RESPONSIBLE = ? WHERE ID_CLASSROOM = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                        updateStmt.setInt(1, responsibleId);
                        updateStmt.setInt(2, classroomId);
                        int rows = updateStmt.executeUpdate();

                        if (rows > 0) {
                            conn.commit();
                            outputArea.append("Ответственный переназначен на аудиторию " +
                                    classroomCombo.getSelectedItem() + "\n");
                            showAssignments();
                        }
                    }
                } else {
                    // Если ответственного нет - добавляем новую запись
                    String insertSql = "INSERT INTO CLASSROOM_RESPONSIBLES (ID_CLASSROOM, ID_RESPONSIBLE) VALUES (?, ?)";
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                        insertStmt.setInt(1, classroomId);
                        insertStmt.setInt(2, responsibleId);
                        int rows = insertStmt.executeUpdate();

                        if (rows > 0) {
                            conn.commit();
                            outputArea.append("Ответственный назначен на аудиторию " +
                                    classroomCombo.getSelectedItem() + "\n");
                            showAssignments();
                        }
                    }
                }
            }
        } catch (SQLException e) {
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
        // Главная панель с BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 1. Верхняя панель - формы ввода
        JPanel inputPanel = new JPanel(new GridLayout(1, 2, 10, 0));

        // Панель аудиторий
        JPanel classroomPanel = createClassroomPanel();
        inputPanel.add(classroomPanel);

        // Панель ответственных
        JPanel responsiblePanel = createResponsiblePanel();
        inputPanel.add(responsiblePanel);

        mainPanel.add(inputPanel, BorderLayout.NORTH);

        // 2. Центральная панель - назначения и действия
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));

        // Панель назначений
        JPanel assignmentPanel = createAssignmentPanel();
        centerPanel.add(assignmentPanel, BorderLayout.NORTH);

        // Панель действий
        JPanel actionPanel = createActionPanel();
        centerPanel.add(actionPanel, BorderLayout.SOUTH);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // 3. Нижняя панель - вывод информации
        outputArea = new JTextArea(10, 60);
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Информация"));
        mainPanel.add(scrollPane, BorderLayout.SOUTH);

        // Устанавливаем главную панель в окно
        add(mainPanel);

        // Загружаем данные
        loadClassrooms();
        loadResponsibles();
    }

    private JPanel createClassroomPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Добавление аудитории"));

        JPanel fields = new JPanel(new GridLayout(4, 2, 5, 5));
        fields.add(new JLabel("Здание:"));
        buildingField = new JTextField();
        fields.add(buildingField);
        fields.add(new JLabel("Номер:"));
        roomField = new JTextField();
        fields.add(roomField);
        fields.add(new JLabel("Наименование:"));
        nameField = new JTextField();
        fields.add(nameField);
        fields.add(new JLabel("Площадь:"));
        areaField = new JTextField();
        fields.add(areaField);

        panel.add(fields);

        JButton addBtn = new JButton("Добавить аудиторию");
        addBtn.addActionListener(e -> addClassroom());
        addBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(Box.createVerticalStrut(10));
        panel.add(addBtn);

        return panel;
    }

    private JPanel createResponsiblePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Добавление ответственного"));

        JPanel fields = new JPanel(new GridLayout(4, 2, 5, 5));
        fields.add(new JLabel("ФИО:"));
        fullNameField = new JTextField();
        fields.add(fullNameField);
        fields.add(new JLabel("Должность:"));
        positionField = new JTextField();
        fields.add(positionField);
        fields.add(new JLabel("Телефон:"));
        phoneField = new JTextField();
        fields.add(phoneField);
        fields.add(new JLabel("Возраст:"));
        ageField = new JTextField();
        fields.add(ageField);

        panel.add(fields);

        JButton addBtn = new JButton("Добавить ответственного");
        addBtn.addActionListener(e -> addResponsible());
        addBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(Box.createVerticalStrut(10));
        panel.add(addBtn);

        return panel;
    }

    private JPanel createAssignmentPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder("Назначение ответственных"));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel comboPanel = new JPanel(new GridLayout(1, 2, 10, 0));

        classroomCombo = new JComboBox<>();
        JPanel classroomSelect = new JPanel(new BorderLayout());
        classroomSelect.add(new JLabel("Аудитория:"), BorderLayout.NORTH);
        classroomSelect.add(classroomCombo, BorderLayout.CENTER);
        comboPanel.add(classroomSelect);

        responsibleCombo = new JComboBox<>();
        JPanel responsibleSelect = new JPanel(new BorderLayout());
        responsibleSelect.add(new JLabel("Ответственный:"), BorderLayout.NORTH);
        responsibleSelect.add(responsibleCombo, BorderLayout.CENTER);
        comboPanel.add(responsibleSelect);

        panel.add(comboPanel);
        panel.add(Box.createVerticalStrut(10));

        JButton assignBtn = new JButton("Назначить");
        assignBtn.addActionListener(e -> assignResponsible());
        assignBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(assignBtn);

        return panel;
    }

    private JPanel createActionPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 5, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Действия"));

        // Первая строка
        JButton showClassroomsBtn = new JButton("Все аудитории");
        showClassroomsBtn.addActionListener(e -> showAllClassrooms());
        panel.add(showClassroomsBtn);

        JButton showResponsiblesBtn = new JButton("Все ответственные");
        showResponsiblesBtn.addActionListener(e -> showAllResponsibles());
        panel.add(showResponsiblesBtn);

        JButton showAssignBtn = new JButton("Назначения");
        showAssignBtn.addActionListener(e -> showAssignments());
        panel.add(showAssignBtn);

        JButton phonebookBtn = new JButton("Телефонный справочник");
        phonebookBtn.addActionListener(e -> showPhonebook());
        panel.add(phonebookBtn);

        JButton avgAreaBtn = new JButton("Средняя площадь");
        avgAreaBtn.addActionListener(e -> showAverageArea());
        panel.add(avgAreaBtn);

        // Вторая строка - операции редактирования/удаления
        JButton editClassroomBtn = new JButton("Изм. аудиторию");
        editClassroomBtn.addActionListener(e -> editClassroom());
        panel.add(editClassroomBtn);

        JButton deleteClassroomBtn = new JButton("Уд. аудиторию");
        deleteClassroomBtn.addActionListener(e -> deleteClassroom());
        panel.add(deleteClassroomBtn);

        JButton editResponsibleBtn = new JButton("Изм. ответственного");
        editResponsibleBtn.addActionListener(e -> editResponsible());
        panel.add(editResponsibleBtn);

        JButton deleteResponsibleBtn = new JButton("Уд. ответственного");
        deleteResponsibleBtn.addActionListener(e -> deleteResponsible());
        panel.add(deleteResponsibleBtn);

        JButton resetBtn = new JButton("Сбросить данные");
        resetBtn.addActionListener(e -> resetToDefaults());
        panel.add(resetBtn);

        return panel;
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

    private void showAllResponsibles() {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT ID_RESPONSIBLE, FULL_NAME " +
                             "FROM RESPONSIBLES ORDER BY FULL_NAME")) {

            outputArea.setText("Список всех ответственных:\n");
            outputArea.append(String.format("%-5s %-30s\n",
                    "ID", "ФИО"));
            outputArea.append("------------------------------------------------------------\n");

            while (rs.next()) {
                outputArea.append(String.format("%-5d %-30s\n",
                        rs.getInt("ID_RESPONSIBLE"),
                        rs.getString("FULL_NAME")));
            }
        } catch (SQLException e) {
            outputArea.append("Ошибка при получении списка ответственных: " + e.getMessage() + "\n");
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
        if (buildingField.getText().isEmpty() || roomField.getText().isEmpty() || areaField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Заполните обязательные поля: здание, номер и площадь");
            return;
        }

        try {
            conn.setAutoCommit(false);

            String sql = "INSERT INTO CLASSROOMS (BUILDING, ROOM_NUMBER, NAME, AREA) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, buildingField.getText());
                pstmt.setString(2, roomField.getText());
                pstmt.setString(3, nameField.getText().isEmpty() ? null : nameField.getText());
                pstmt.setDouble(4, Double.parseDouble(areaField.getText()));

                pstmt.executeUpdate();

                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        outputArea.append("Добавлена аудитория: " + buildingField.getText() +
                                " " + roomField.getText() + "\n");
                    }
                }
            }

            conn.commit();
            resetFields();
            loadClassrooms();
        } catch (SQLIntegrityConstraintViolationException e) {
            try {
                conn.rollback();
                JOptionPane.showMessageDialog(this, "Аудитория с таким номером в этом здании уже существует");
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } catch (SQLException | NumberFormatException e) {
            try {
                conn.rollback();
                JOptionPane.showMessageDialog(this, "Ошибка при добавлении аудитории: " + e.getMessage());
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

    private void editClassroom() {
        String id = JOptionPane.showInputDialog(this, "Введите ID аудитории для редактирования:");
        if (id == null || id.trim().isEmpty()) return;

        try {
            // Получаем текущие данные
            String sql = "SELECT * FROM CLASSROOMS WHERE ID_CLASSROOM = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, Integer.parseInt(id));
                ResultSet rs = pstmt.executeQuery();

                if (!rs.next()) {
                    JOptionPane.showMessageDialog(this, "Аудитория с таким ID не найдена");
                    return;
                }

                // Создаем диалог редактирования
                JPanel panel = new JPanel(new GridLayout(4, 2));
                JTextField buildingField = new JTextField(rs.getString("BUILDING"));
                JTextField roomField = new JTextField(rs.getString("ROOM_NUMBER"));
                JTextField nameField = new JTextField(rs.getString("NAME"));
                JTextField areaField = new JTextField(rs.getString("AREA"));

                panel.add(new JLabel("Здание:"));
                panel.add(buildingField);
                panel.add(new JLabel("Номер:"));
                panel.add(roomField);
                panel.add(new JLabel("Наименование:"));
                panel.add(nameField);
                panel.add(new JLabel("Площадь:"));
                panel.add(areaField);

                int result = JOptionPane.showConfirmDialog(this, panel,
                        "Редактирование аудитории ID: " + id,
                        JOptionPane.OK_CANCEL_OPTION);

                if (result == JOptionPane.OK_OPTION) {
                    // Обновляем данные
                    String updateSql = "UPDATE CLASSROOMS SET BUILDING = ?, ROOM_NUMBER = ?, " +
                            "NAME = ?, AREA = ? WHERE ID_CLASSROOM = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                        updateStmt.setString(1, buildingField.getText());
                        updateStmt.setString(2, roomField.getText());
                        updateStmt.setString(3, nameField.getText());
                        updateStmt.setDouble(4, Double.parseDouble(areaField.getText()));
                        updateStmt.setInt(5, Integer.parseInt(id));

                        int rows = updateStmt.executeUpdate();
                        if (rows > 0) {
                            outputArea.append("Аудитория ID " + id + " успешно обновлена\n");
                            loadClassrooms();
                        }
                    }
                }
            }
        } catch (SQLException | NumberFormatException e) {
            outputArea.append("Ошибка при редактировании аудитории: " + e.getMessage() + "\n");
        }
    }

    private void showAllClassrooms() {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT C.ID_CLASSROOM as \"ID\", C.BUILDING as \"Здание\", " +
                             "C.ROOM_NUMBER as \"Номер\", C.NAME as \"Наименование\", " +
                             "C.AREA as \"Площадь\", R.FULL_NAME as \"Ответственный\" " +
                             "FROM CLASSROOMS C " +
                             "LEFT JOIN CLASSROOM_RESPONSIBLES CR ON C.ID_CLASSROOM = CR.ID_CLASSROOM " +
                             "LEFT JOIN RESPONSIBLES R ON CR.ID_RESPONSIBLE = R.ID_RESPONSIBLE " +
                             "ORDER BY 1")) {

            // Создаем модель таблицы на основе ResultSet
            JTable table = new JTable(buildTableModel(rs));
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

            // Настраиваем ширину колонок
            table.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
            table.getColumnModel().getColumn(1).setPreferredWidth(150); // Здание
            table.getColumnModel().getColumn(2).setPreferredWidth(80);  // Номер
            table.getColumnModel().getColumn(3).setPreferredWidth(200); // Наименование
            table.getColumnModel().getColumn(4).setPreferredWidth(80);  // Площадь
            table.getColumnModel().getColumn(5).setPreferredWidth(250); // Ответственный

            // Создаем диалоговое окно с таблицей
            JScrollPane scrollPane = new JScrollPane(table);
            JOptionPane.showMessageDialog(this, scrollPane,
                    "Список всех аудиторий", JOptionPane.PLAIN_MESSAGE);

        } catch (SQLException e) {
            outputArea.append("Ошибка при получении списка аудиторий: " + e.getMessage() + "\n");
        }
    }

    // Вспомогательный метод для создания модели таблицы из ResultSet
    private DefaultTableModel buildTableModel(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();

        // Получаем названия колонок
        int columnCount = metaData.getColumnCount();
        String[] columnNames = new String[columnCount];
        for (int i = 1; i <= columnCount; i++) {
            columnNames[i-1] = metaData.getColumnName(i);
        }

        // Получаем данные
        java.util.List<Object[]> data = new ArrayList<>();
        while (rs.next()) {
            Object[] row = new Object[columnCount];
            for (int i = 1; i <= columnCount; i++) {
                row[i-1] = rs.getObject(i);
                if (row[i-1] == null) row[i-1] = "-";
            }
            data.add(row);
        }

        // Создаем модель таблицы
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        for (Object[] row : data) {
            model.addRow(row);
        }

        return model;
    }

    private void deleteClassroom() {
        String id = JOptionPane.showInputDialog(this, "Введите ID аудитории для удаления:");
        if (id == null || id.trim().isEmpty()) return;

        try {
            conn.setAutoCommit(false);

            // Сначала удаляем связанные назначения
            String deleteAssignSql = "DELETE FROM CLASSROOM_RESPONSIBLES WHERE ID_CLASSROOM = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteAssignSql)) {
                pstmt.setInt(1, Integer.parseInt(id));
                pstmt.executeUpdate();
            }

            // Затем удаляем саму аудиторию
            String deleteSql = "DELETE FROM CLASSROOMS WHERE ID_CLASSROOM = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteSql)) {
                pstmt.setInt(1, Integer.parseInt(id));
                int rows = pstmt.executeUpdate();

                if (rows > 0) {
                    conn.commit();
                    outputArea.append("Аудитория ID " + id + " и связанные назначения удалены\n");
                    loadClassrooms();
                } else {
                    conn.rollback();
                    JOptionPane.showMessageDialog(this, "Аудитория с таким ID не найдена");
                }
            }
        } catch (SQLException | NumberFormatException e) {
            try {
                conn.rollback();
                outputArea.append("Ошибка при удалении аудитории: " + e.getMessage() + "\n");
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
        if (fullNameField.getText().isEmpty() || phoneField.getText().isEmpty() || ageField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Заполните обязательные поля: ФИО, телефон и возраст");
            return;
        }

        try {
            conn.setAutoCommit(false);

            String sql = "INSERT INTO RESPONSIBLES (FULL_NAME, POSITION, PHONE, AGE) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, fullNameField.getText());
                pstmt.setString(2, positionField.getText().isEmpty() ? null : positionField.getText());
                pstmt.setString(3, phoneField.getText());
                pstmt.setInt(4, Integer.parseInt(ageField.getText()));

                pstmt.executeUpdate();

                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        outputArea.append("Добавлен ответственный: " + fullNameField.getText() + "\n");
                    }
                }
            }

            conn.commit();
            resetFields();
            loadResponsibles();
        } catch (SQLIntegrityConstraintViolationException e) {
            try {
                conn.rollback();
                JOptionPane.showMessageDialog(this, "Такой ответственный уже существует (ФИО, телефон и возраст должны быть уникальными)");
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } catch (SQLException | NumberFormatException e) {
            try {
                conn.rollback();
                JOptionPane.showMessageDialog(this, "Ошибка при добавлении ответственного: " + e.getMessage());
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

    private void editResponsible() {
        String id = JOptionPane.showInputDialog(this, "Введите ID ответственного для редактирования:");
        if (id == null || id.trim().isEmpty()) return;

        try {
            // Получаем текущие данные
            String sql = "SELECT * FROM RESPONSIBLES WHERE ID_RESPONSIBLE = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, Integer.parseInt(id));
                ResultSet rs = pstmt.executeQuery();

                if (!rs.next()) {
                    JOptionPane.showMessageDialog(this, "Ответственный с таким ID не найден");
                    return;
                }

                // Создаем диалог редактирования
                JPanel panel = new JPanel(new GridLayout(4, 2));
                JTextField nameField = new JTextField(rs.getString("FULL_NAME"));
                JTextField positionField = new JTextField(rs.getString("POSITION"));
                JTextField phoneField = new JTextField(rs.getString("PHONE"));
                JTextField ageField = new JTextField(String.valueOf(rs.getInt("AGE")));

                panel.add(new JLabel("ФИО:"));
                panel.add(nameField);
                panel.add(new JLabel("Должность:"));
                panel.add(positionField);
                panel.add(new JLabel("Телефон:"));
                panel.add(phoneField);
                panel.add(new JLabel("Возраст:"));
                panel.add(ageField);

                int result = JOptionPane.showConfirmDialog(this, panel,
                        "Редактирование ответственного ID: " + id,
                        JOptionPane.OK_CANCEL_OPTION);

                if (result == JOptionPane.OK_OPTION) {
                    // Обновляем данные
                    String updateSql = "UPDATE RESPONSIBLES SET FULL_NAME = ?, POSITION = ?, " +
                            "PHONE = ?, AGE = ? WHERE ID_RESPONSIBLE = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                        updateStmt.setString(1, nameField.getText());
                        updateStmt.setString(2, positionField.getText());
                        updateStmt.setString(3, phoneField.getText());
                        updateStmt.setInt(4, Integer.parseInt(ageField.getText()));
                        updateStmt.setInt(5, Integer.parseInt(id));

                        int rows = updateStmt.executeUpdate();
                        if (rows > 0) {
                            outputArea.append("Ответственный ID " + id + " успешно обновлен\n");
                            loadResponsibles();
                        }
                    }
                }
            }
        } catch (SQLException | NumberFormatException e) {
            outputArea.append("Ошибка при редактировании ответственного: " + e.getMessage() + "\n");
        }
    }

    private void deleteResponsible() {
        String id = JOptionPane.showInputDialog(this, "Введите ID ответственного для удаления:");
        if (id == null || id.trim().isEmpty()) return;

        try {
            conn.setAutoCommit(false);

            // Сначала удаляем связанные назначения
            String deleteAssignSql = "DELETE FROM CLASSROOM_RESPONSIBLES WHERE ID_RESPONSIBLE = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteAssignSql)) {
                pstmt.setInt(1, Integer.parseInt(id));
                pstmt.executeUpdate();
            }

            // Затем удаляем самого ответственного
            String deleteSql = "DELETE FROM RESPONSIBLES WHERE ID_RESPONSIBLE = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteSql)) {
                pstmt.setInt(1, Integer.parseInt(id));
                int rows = pstmt.executeUpdate();

                if (rows > 0) {
                    conn.commit();
                    outputArea.append("Ответственный ID " + id + " и связанные назначения удалены\n");
                    loadResponsibles();
                } else {
                    conn.rollback();
                    JOptionPane.showMessageDialog(this, "Ответственный с таким ID не найден");
                }
            }
        } catch (SQLException | NumberFormatException e) {
            try {
                conn.rollback();
                outputArea.append("Ошибка при удалении ответственного: " + e.getMessage() + "\n");
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

    //Для назначений
    private void editAssignment() {
        String id = JOptionPane.showInputDialog(this, "Введите ID назначения для редактирования:");
        if (id == null || id.trim().isEmpty()) return;

        try {
            // Получаем текущие данные
            String sql = "SELECT * FROM CLASSROOM_RESPONSIBLES WHERE ID = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, Integer.parseInt(id));
                ResultSet rs = pstmt.executeQuery();

                if (!rs.next()) {
                    JOptionPane.showMessageDialog(this, "Назначение с таким ID не найдено");
                    return;
                }

                // Получаем списки для выбора
                java.util.List<String> classrooms = getClassroomsList();
                java.util.List<String> responsibles = getResponsiblesList();

                // Создаем диалог редактирования
                JPanel panel = new JPanel(new GridLayout(2, 2));
                JComboBox<String> classroomCombo = new JComboBox<>(classrooms.toArray(new String[0]));
                JComboBox<String> responsibleCombo = new JComboBox<>(responsibles.toArray(new String[0]));

                // Устанавливаем текущие значения
                setComboSelection(classroomCombo, rs.getInt("ID_CLASSROOM"));
                setComboSelection(responsibleCombo, rs.getInt("ID_RESPONSIBLE"));

                panel.add(new JLabel("Аудитория:"));
                panel.add(classroomCombo);
                panel.add(new JLabel("Ответственный:"));
                panel.add(responsibleCombo);

                int result = JOptionPane.showConfirmDialog(this, panel,
                        "Редактирование назначения ID: " + id,
                        JOptionPane.OK_CANCEL_OPTION);

                if (result == JOptionPane.OK_OPTION) {
                    // Обновляем данные
                    String updateSql = "UPDATE CLASSROOM_RESPONSIBLES SET ID_CLASSROOM = ?, " +
                            "ID_RESPONSIBLE = ? WHERE ID = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                        updateStmt.setInt(1, getIdFromCombo(classroomCombo.getSelectedItem()));
                        updateStmt.setInt(2, getIdFromCombo(responsibleCombo.getSelectedItem()));
                        updateStmt.setInt(3, Integer.parseInt(id));

                        int rows = updateStmt.executeUpdate();
                        if (rows > 0) {
                            outputArea.append("Назначение ID " + id + " успешно обновлено\n");
                            showAssignments();
                        }
                    }
                }
            }
        } catch (SQLException | NumberFormatException e) {
            outputArea.append("Ошибка при редактировании назначения: " + e.getMessage() + "\n");
        }
    }

    private void deleteAssignment() {
        String id = JOptionPane.showInputDialog(this, "Введите ID назначения для удаления:");
        if (id == null || id.trim().isEmpty()) return;

        try {
            String deleteSql = "DELETE FROM CLASSROOM_RESPONSIBLES WHERE ID = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteSql)) {
                pstmt.setInt(1, Integer.parseInt(id));
                int rows = pstmt.executeUpdate();

                if (rows > 0) {
                    outputArea.append("Назначение ID " + id + " удалено\n");
                    showAssignments();
                } else {
                    JOptionPane.showMessageDialog(this, "Назначение с таким ID не найдено");
                }
            }
        } catch (SQLException | NumberFormatException e) {
            outputArea.append("Ошибка при удалении назначения: " + e.getMessage() + "\n");
        }
    }

    private java.util.List<String> getClassroomsList() throws SQLException {
        java.util.List<String> classrooms = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT ID_CLASSROOM, BUILDING, ROOM_NUMBER FROM CLASSROOMS ORDER BY BUILDING, ROOM_NUMBER")) {
            while (rs.next()) {
                classrooms.add(rs.getInt("ID_CLASSROOM") + " - " +
                        rs.getString("BUILDING") + " " +
                        rs.getString("ROOM_NUMBER"));
            }
        }
        return classrooms;
    }

    private java.util.List<String> getResponsiblesList() throws SQLException {
        java.util.List<String> responsibles = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT ID_RESPONSIBLE, FULL_NAME FROM RESPONSIBLES ORDER BY FULL_NAME")) {
            while (rs.next()) {
                responsibles.add(rs.getInt("ID_RESPONSIBLE") + " - " +
                        rs.getString("FULL_NAME"));
            }
        }
        return responsibles;
    }

    private void setComboSelection(JComboBox<String> combo, int id) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (combo.getItemAt(i).startsWith(id + " - ")) {
                combo.setSelectedIndex(i);
                break;
            }
        }
    }

    private int getIdFromCombo(Object item) {
        String str = (String)item;
        return Integer.parseInt(str.split(" - ")[0]);
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