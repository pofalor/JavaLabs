import java.awt.*;

class ColorDialog extends Dialog {
    private Color selectedColor = Color.RED;
    private final Button[] colorButtons;


    public ColorDialog(Frame parent)
    {
        super(parent, "Выберите цвет", true);
        setSize(300, 200);
        setLayout(new GridLayout(3, 3));

        colorButtons = new Button[FigureConstants.colors.length];
        for (int i = 0; i < FigureConstants.colors.length; i++) {
            colorButtons[i] = new Button();
            colorButtons[i].setBackground(FigureConstants.colors[i]);
            final int index = i;
            colorButtons[i].addActionListener(e -> {
                selectedColor = FigureConstants.colors[index];
                setVisible(false);
            });
            add(colorButtons[i]);
        }
    }

    public Color getSelectedColor() {
        return selectedColor;
    }
}
