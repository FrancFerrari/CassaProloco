package cassaproloco;

import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;

public class ModernButtonUI extends BasicButtonUI {

    private final Color buttonColor;
    private final Color hoverColor;
    private final Color clickColor;
    private final Color textColor;
    private final int cornerRadius;

    public ModernButtonUI(Color buttonColor, Color hoverColor, Color clickColor, Color textColor) {
        this(buttonColor, hoverColor, clickColor, textColor, 12); // default radius
    }

    public ModernButtonUI(Color buttonColor, Color hoverColor, Color clickColor, Color textColor, int cornerRadius) {
        this.buttonColor = buttonColor;
        this.hoverColor = hoverColor;
        this.clickColor = clickColor;
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
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        Graphics2D g2d = (Graphics2D) g.create();
        AbstractButton button = (AbstractButton) c;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = c.getWidth();
        int height = c.getHeight();

        Color background = button.getModel().isPressed() ? clickColor
                : button.getModel().isRollover() ? hoverColor
                : buttonColor;

        // Ombra
        g2d.setColor(new Color(0, 0, 0, 30));
        g2d.fillRoundRect(2, 4, width - 4, height - 4, cornerRadius, cornerRadius);

        // Bottone
        g2d.setColor(background);
        g2d.fillRoundRect(0, 0, width - 4, height - 4, cornerRadius, cornerRadius);

        g2d.setColor(textColor);

        FontMetrics fm = g2d.getFontMetrics();
        String text = button.getText();

        // Splitta il testo in parole
        String[] words = text.split("\\s+");
        java.util.List<String> lines = new java.util.ArrayList<>();
        StringBuilder currentLine = new StringBuilder();

        int maxTextWidth = width - 20; // margine orizzontale per evitare bordi

        for (String word : words) {
            String testLine = currentLine.length() == 0 ? word : currentLine + " " + word;
            int testWidth = fm.stringWidth(testLine);
            if (testWidth > maxTextWidth) {
                // la linea attuale è piena, salva e inizia nuova linea
                if (currentLine.length() > 0) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder(word);
                } else {
                    // parola troppo lunga da sola, spezzala forzatamente
                    lines.add(word);
                    currentLine = new StringBuilder();
                }
            } else {
                currentLine = new StringBuilder(testLine);
            }
        }
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }

        // Calcola altezza totale del blocco di testo multilinea
        int lineHeight = fm.getHeight();
        int totalTextHeight = lineHeight * lines.size();

        // Punto di partenza verticale per centrare il testo multilinea
        int y = (height - totalTextHeight) / 2 + fm.getAscent();

        // Disegna ogni linea centrata orizzontalmente
        for (String line : lines) {
            int lineWidth = fm.stringWidth(line);
            int x = (width - lineWidth) / 2;
            g2d.drawString(line, x, y);
            y += lineHeight;
        }

        g2d.dispose();
    }
}
