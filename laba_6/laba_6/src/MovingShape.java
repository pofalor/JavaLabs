import java.awt.*;
import java.util.Random;

class MovingShape extends Thread {
    private final int id;
    private final String type;
    private Color color;
    private final int speed;
    private int maxWidth;
    private int maxHeight;
    private ShapeAnimation parent;

    private int x, y;
    private int dx, dy;
    private boolean running = true;

    public MovingShape(int id, String type, Color color, int speed,
                       int maxWidth, int maxHeight, ShapeAnimation parent) {
        this.id = id;
        this.type = type;
        this.color = color;
        this.speed = speed;
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        this.parent = parent;

        // Начальная позиция в левом верхнем углу
        x = 30;
        y = 50;

        // Случайное направление
        Random rand = new Random();
        dx = rand.nextInt(3) + 1;
        dy = rand.nextInt(3) + 1;

        // Учитываем скорость
        dx *= speed;
        dy *= speed;
    }

    public void run() {
        while (running) {
            long startTime = System.currentTimeMillis();

            move();
            parent.repaint();

            // Контроль FPS (примерно 30 кадров в секунду)
            long sleepTime = 33 - (System.currentTimeMillis() - startTime);
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void move() {
        x += dx;
        y += dy;

        // Проверка границ и отражение
        int shapeWidth = getShapeWidth();
        int shapeHeight = getShapeHeight();

        if (x <= 0 || x + shapeWidth >= maxWidth) {
            dx = -dx;
            x = Math.max(0, Math.min(x, maxWidth - shapeWidth));
        }

        if (y <= 30 || y + shapeHeight >= maxHeight) { // 30 - учитываем заголовок окна
            dy = -dy;
            y = Math.max(30, Math.min(y, maxHeight - shapeHeight));
        }
    }

    public void draw(Graphics g) {
        //Сглаживаем края фигур
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(color);

        switch (type) {
            case "круг":
                g.fillOval(x, y, 30, 30);
                break;
            case "овал":
                g.fillOval(x, y, 40, 30);
                break;
            case "треугольник":
                int[] xPoints = {x, x + 30, x + 15};
                int[] yPoints = {y + 30, y + 30, y};
                g.fillPolygon(xPoints, yPoints, 3);
                break;
            case "квадрат":
                g.fillRect(x, y, 30, 30);
                break;
            case "прямоугольник":
                g.fillRect(x, y, 40, 30);
                break;
        }

        // Отображаем ID фигуры
        g.setColor(Color.BLACK);
        g.drawString(String.valueOf(id), x + 5, y + 15);
    }

    private int getShapeWidth() {
        switch (type) {
            case "овал", "прямоугольник":
                return 40;
            case "круг", "треугольник", "квадрат":
            default:
                return 30;
        }
    }

    private int getShapeHeight() {
        return 30;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setSpeed(int speed) {
        // Сохраняем направление, меняем только величину
        dx = (dx < 0 ? -1 : 1) * speed;
        dy = (dy < 0 ? -1 : 1) * speed;
    }

    public int getShapeId(){
        return id;
    }
}
