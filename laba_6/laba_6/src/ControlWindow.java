import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

class ControlWindow extends Frame {
    private ShapeAnimation animation;
    private TextField shapeTypeField;
    private TextField shapeIdField;
    private Choice speedChoice;
    private ColorDialog colorDialog;

    public ControlWindow(ShapeAnimation animation) {
        super("Управляющее окно");
        this.animation = animation;
        setSize(300, 250);
        setLayout(new GridLayout(6, 2, 5, 5));

        // Обработчик закрытия окна
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        // Элементы управления
        add(new Label("Тип фигуры:"));
        shapeTypeField = new TextField("круг");
        add(shapeTypeField);

        add(new Label("Цвет:"));
        Button colorButton = new Button("Выбрать цвет");
        colorButton.addActionListener(e -> showColorDialog());
        add(colorButton);
        colorDialog = new ColorDialog(this);

        add(new Label("Скорость:"));
        speedChoice = new Choice();
        for (int i = 1; i <= 6; i++) {
            speedChoice.add("Скорость " + i);
        }
        add(speedChoice);

        add(new Label("ID фигуры:"));
        shapeIdField = new TextField();
        add(shapeIdField);

        Button startButton = new Button("Пуск");
        startButton.addActionListener(e -> startShape());
        add(startButton);

        Button updateButton = new Button("Обновить");
        updateButton.addActionListener(e -> updateShape());
        add(updateButton);
    }

    private void showColorDialog() {
        colorDialog.setVisible(true);
    }

    private void startShape() {
        try {
            String shapeType = shapeTypeField.getText().toLowerCase();
            if (!isValidShapeType(shapeType)) {
                showError("Недопустимый тип фигуры");
                return;
            }
            int id = animation.getNextShapeId();
            var idStr = shapeIdField.getText();
            if(!idStr.isEmpty()){
                // Получаем ID из существующего поля
                id = Integer.parseInt(idStr);
            }

            // Проверки ID
            if (id <= 0) {
                showError("ID должен быть положительным числом");
                return;
            }

            if (animation.isShapeIdExists(id)) {
                showError("Фигура с ID " + id + " уже существует");
                return;
            }

            int speed = speedChoice.getSelectedIndex() + 1;
            animation.addShape(id, shapeType, colorDialog.getSelectedColor(), speed);

        } catch (NumberFormatException e) {
            showError("ID должен быть числом");
        }
        catch (Exception e){
            showError("Неизвестная ошибка при создании фигуры. Системное сообщение: " + e.getMessage());
        }
    }

    private void updateShape() {
        try {
            int id = Integer.parseInt(shapeIdField.getText());
            int speed = speedChoice.getSelectedIndex() + 1;
            animation.updateShape(id, colorDialog.getSelectedColor(), speed);
        } catch (NumberFormatException e) {
            showError("Неверный ID фигуры");
        }
    }

    private boolean isValidShapeType(String type) {
        for (var figureName: FigureConstants.figureNames){
            var isValid = type.equals(figureName);
            if(isValid)
                return true;
        }
        return false;
    }

    private void showError(String message) {
        new ErrorDialog(this, message).setVisible(true);
    }
}