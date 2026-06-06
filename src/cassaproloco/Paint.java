
package cassaproloco;
import java.awt.*;
import javax.swing.JPanel;

public class Paint extends JPanel{
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        Graphics2D g2d = (Graphics2D) g.create();
        // Miglior qualità grafica
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int arcWidth = 20;
        int arcHeight = 20;
        
        // Colore cornice
        g2d.setColor(Color.BLACK);
        
        // Spessore bordo
        g2d.setStroke(new BasicStroke(2));
        
        // Disegna rettangolo arrotondato (lascia 1-2px margine per bordo)
        int strokeWidth = 2;       // spessore del bordo
        int offset = strokeWidth;  // offset per evitare il clipping

        g2d.setStroke(new BasicStroke(strokeWidth));
        g2d.drawRoundRect(
            offset,                // x = 2 pixel dentro
            offset,                // y = 2 pixel dentro
            getWidth() - 2*offset, // larghezza meno margini
            getHeight() - 2*offset,// altezza meno margini
            arcWidth, arcHeight);        
        g2d.dispose();
    }
}

