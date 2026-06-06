package cassaproloco;

/**
 *
 * @author franc
 */
import javax.swing.JTextField;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class RoundedTextField extends JTextField {
    private int cornerRadius = 15;

    public RoundedTextField(int columns) {
        super(columns);
        setOpaque(false); // per disegnare noi il background arrotondato
        setBorder(null);  // rimuovi border di default
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();

        // Attiva anti-aliasing per bordi più morbidi
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Colore background, usa quello attuale o personalizza
        g2.setColor(getBackground());

        // Disegna rettangolo arrotondato per il background
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);

        super.paintComponent(g);

        g2.dispose();
    }

}
