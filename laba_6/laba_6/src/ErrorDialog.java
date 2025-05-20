import java.awt.*;

class ErrorDialog extends Dialog {
    public ErrorDialog(Frame parent, String message) {
        super(parent, "Ошибка", true);
        setSize(530, 100);
        setLayout(new BorderLayout());

        add(new Label(message, Label.CENTER), BorderLayout.CENTER);

        Button okButton = new Button("OK");
        okButton.addActionListener(e -> setVisible(false));
        add(okButton, BorderLayout.SOUTH);
    }
}
