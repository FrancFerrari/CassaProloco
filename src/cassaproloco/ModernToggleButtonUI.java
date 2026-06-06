package cassaproloco;

import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.basic.BasicToggleButtonUI;

public class ModernToggleButtonUI extends BasicToggleButtonUI {

    private final Color buttonColor;
    private final Color hoverColor;
    private final Color clickColor;
    private final Color selectedColor;
    private final Color textColor;
    private final int cornerRadius;
    private final int paddingX = 5;  // aumenta lo sfondo in larghezza
    private final int paddingY = 2;   // aumenta lo sfondo in altezza

    public ModernToggleButtonUI(Color buttonColor, Color hoverColor, Color clickColor, Color selectedColor, Color textColor) {
        this(buttonColor, hoverColor, clickColor, selectedColor, textColor, 12);
    }

    public ModernToggleButtonUI(Color buttonColor, Color hoverColor, Color clickColor, Color selectedColor, Color textColor, int cornerRadius) {
        this.buttonColor = buttonColor;
        this.hoverColor = hoverColor;
        this.clickColor = clickColor;
        this.selectedColor = selectedColor;
        this.textColor = textColor;
        this.cornerRadius = cornerRadius;
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        AbstractButton button = (AbstractButton) c;
        button.setOpaque(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setForeground(textColor);
        button.setFont(button.getFont().deriveFont(Font.BOLD, 11f));  // font più piccolo
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    @Override
public void paint(Graphics g, JComponent c) {
    Graphics2D g2d = (Graphics2D) g.create();
    AbstractButton button = (AbstractButton) c;
    ButtonModel model = button.getModel();

    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    FontMetrics fm = g2d.getFontMetrics();
    String text = button.getText();
    int textWidth = fm.stringWidth(text);
    int textHeight = fm.getHeight();

    // Usa dimensioni reali del componente
    int bgWidth = c.getWidth();
    int bgHeight = c.getHeight();

    // Centra il testo nel componente
    int textX = (bgWidth - textWidth) / 2;
    int textY = (bgHeight - textHeight) / 2 + fm.getAscent();

    int cornerRadius = this.cornerRadius;

    Color background = model.isPressed() ? clickColor
                      : model.isRollover() ? hoverColor
                      : model.isSelected() ? selectedColor
                      : buttonColor;

    // Ombra (puoi regolare le dimensioni dell'ombra)
    g2d.setColor(new Color(0, 0, 0, 30));
    g2d.fillRoundRect(2, 4, bgWidth - 4, bgHeight - 4, cornerRadius, cornerRadius);

    // Sfondo
    g2d.setColor(background);
    g2d.fillRoundRect(0, 0, bgWidth, bgHeight, cornerRadius, cornerRadius);

    // Testo
    g2d.setColor(textColor);
    g2d.drawString(text, textX, textY);

    g2d.dispose();
}

}
