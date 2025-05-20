/*
1. В зависимости от варианта выполняется разработка приложения. Пример
приложения похожего на то, которое нужно разработать см. в примере 1.
2. Разрабатываемое приложение должно состоять из двух окон: управляющего (УО) и
дочернего, т.е. демонстрационного (ДО). В первом должны отображаться
интерфейсные элементы (кнопки, текстовые поля, выпадающие списки, элементы
выбора цвета), во втором в зависимости от настроек, сделанных в первом окне,
должны передвигаться фигурки или простые объекты (ФиО).
3. При выставлении в УО некоторых настроек запуска очередной ФиО и нажатии на
кнопку «Пуск» в ДО из левого верхнего угла в случайном направлении должен
начать двигаться ФиО (!!! т.е. приращение координат «икс» и «игрек» ФиО может
быть таким, что оно будет отличаться от траектории, совпадающей с биссектрисой
угла вылета ФиО).
4. ФиО при движении в ДО должны отражаться от сторон окна по правилу: «угол
отражения равен углу падения».
5. В УО должна быть предусмотрена возможность выбора уже запущенного ФиО и
изменения его параметров (например, цвет, скорость).
6. Номер, который присваивается ФиО должен отображаться рядом с ним (ФИО), когда
он (ФИО) перемещается в ДО.
7. Все ФиО должны быть пронумерованы, при чём, если предусмотрено изменение
номера ФиО, то необходимо обеспечить уникальность номера каждого ФиО.
8. Приложение должно закрываться при закрывании любого из окон.
ФиО: фигуры – 1 (круг, овал, треугольник, квадрат, прямоугольник) или объекты – 2 (маленькая простая картинка или надпись, которая задаётся из УО).
Число ФиО - задано в коде УО
ФиО - фигуры
Задание цвета текста и заливки ФиО - стандартный элемент выбора цвета;
Выбор запускаемого ФиО – из текстового поля (круг, овал…);
Задание начальной скорости ФиО – из выпадающего списка (шесть скоростей);
Способ выбора запущенного ФиО - из текстового поля, где вводится номер ФиО;
Присвоение номера ФиО - вручную из УО;
Возможность смены номера ФиО из УО - нет
Регулировка скорости перемещения выбран-го ФиО - указанием в текстовом поле;
Изменения размера окна отображения ФиО - да; (т.е. отражение ФиО в новых границах)
2122222112
*/

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class ShapeAnimation extends Frame {
    private ControlWindow controlWindow;
    private final List<MovingShape> shapes = new ArrayList<>();
    private int nextShapeId = 1;
    private Image buffer;
    private Graphics bufferGraphics;

    public int getNextShapeId(){
        return nextShapeId;
    }

    public ShapeAnimation() {
        setTitle("Демонстрационное окно");
        setSize(600, 400);
        setBackground(Color.WHITE);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        controlWindow = new ControlWindow(this);
        controlWindow.setVisible(true);
    }

    public void update(Graphics g) {
        // Двойная буферизация
        if (buffer == null) {
            buffer = createImage(getWidth(), getHeight());
            bufferGraphics = buffer.getGraphics();
        }

        bufferGraphics.setColor(getBackground());
        bufferGraphics.fillRect(0, 0, getWidth(), getHeight());


        paint(bufferGraphics);

        g.drawImage(buffer, 0, 0, this);
    }

    public void paint(Graphics g) {
        synchronized (shapes) {
            for (MovingShape shape : shapes) {
                shape.draw(g);
            }
        }
    }

    public boolean isShapeIdExists(int id) {
        synchronized (shapes) {
            return shapes.stream().anyMatch(shape -> shape.getShapeId() == id);
        }
    }

    public void addShape(int id, String shapeType, Color color, int speed) {
        // Обновляем автоинкремент только если новый ID больше текущего
        if (id >= nextShapeId) {
            nextShapeId = id + 1;
        }

        MovingShape shape = new MovingShape(id, shapeType, color, speed,
                getSize().width, getSize().height, this);
        synchronized (shapes) {
            shapes.add(shape);
        }
        shape.start();
        repaint();
    }

    public void updateShape(int id, Color color, int speed) {
        synchronized (shapes) {
            for (MovingShape shape : shapes) {
                if (shape.getShapeId() == id) {
                    shape.setColor(color);
                    shape.setSpeed(speed);
                    break;
                }
            }
        }
    }

    public static void main(String[] args) {
        ShapeAnimation animation = new ShapeAnimation();
        animation.setVisible(true);
    }
}