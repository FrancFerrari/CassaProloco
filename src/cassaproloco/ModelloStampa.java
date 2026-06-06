
package cassaproloco;

import static cassaproloco.Cassa.fromCMToPPI;
import java.awt.Graphics;
import java.awt.*;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JFrame;  
import javax.swing.*; 

public class ModelloStampa extends javax.swing.JPanel implements Printable{
    private final Date d;
    private final Paint panel;
    private final JFrame f ;
    
    public ModelloStampa(String price, String numelements, String Name, Date D) {
        d = D;

        f= new JFrame("Panel Example"); 

        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setLocationByPlatform(true);
        f.pack();
        
        panel=new Paint();
        panel.setLayout(null);   
        panel.setBackground(Color.white);  
        panel.setSize((int) fromCMToPPI(6.2), (int) fromCMToPPI(4));
        //(int) fromCMToPPI(6.2), (int) fromCMToPPI(3)
        if (price != null && !price.trim().isEmpty()) {
            System.out.println("DEBUG: Prezzo ricevuto = '" + price + "'");
            JLabel PriceModel = new JLabel(price + "€");
            panel.add(PriceModel);
            PriceModel.setFont(new java.awt.Font("Tahoma", 0, 12));
            Dimension size = PriceModel.getPreferredSize();
            PriceModel.setBounds(130, 55, size.width, size.height);
        }
        
        JLabel TextModel = new JLabel (numelements + "x " +Name);

        TextModel.setFont(new java.awt.Font("Tahoma", Font.BOLD, 12)); // NOI18N
        // Setto la posizione: x = 0 per partire da sinistra
        // larghezza uguale al pannello (panel.getWidth()), così può centrare
        TextModel.setBounds(-5, 30, panel.getWidth(), 18); // altezza fissa 20 o quella preferita
        TextModel.setHorizontalAlignment(SwingConstants.CENTER); // CENTRATO orizzontalmente
        panel.add(TextModel);
        
        JLabel Proloco  = new JLabel ("ProLoco Cogollo");
        panel.add(Proloco); 
        Proloco.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18
        Dimension size2 = Proloco.getPreferredSize();
        Proloco.setBounds(55, 17, size2.width+50, size2.height);
        
        JLabel Sagra  = new JLabel ("Antica Sagra di S.Luigi ");
        panel.add(Sagra); 
        Sagra.setFont(new java.awt.Font("Tahoma", Font.BOLD, 8)); // NOI18N
        Dimension size4 = Sagra.getPreferredSize();
        Sagra.setBounds(39, 7, size4.width+50, size4.height);
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String dateString = dateFormat.format(d);
        JLabel label = new JLabel("Data:    " + dateString);
        label.setFont(new java.awt.Font("Tahoma", 0, 6));
        Dimension size5 = label.getPreferredSize();

        // Calcola larghezza basata sul pannello, con un margine di 15px a sx e dx
        int labelWidth = panel.getWidth() - 30; 

        label.setBounds(15, 55, labelWidth, size5.height);
        panel.add(label);

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        String timeString = timeFormat.format(d);
        JLabel timeLabel = new JLabel("Ora:     " + timeString);
        timeLabel.setFont(new java.awt.Font("Tahoma", 0, 6));
        Dimension sizeTime = timeLabel.getPreferredSize();
        timeLabel.setBounds(15, 55 + size5.height + 2, labelWidth, sizeTime.height);
        panel.add(timeLabel);

        f.add(panel); 
          
    }

    private static void fitText(JLabel label) {
        Font font = label.getFont();
        String text = label.getText();
        Dimension labelSize = label.getSize();
        FontMetrics fontMetrics = label.getFontMetrics(font);
        
        int textWidth = fontMetrics.stringWidth(text);
        int textHeight = fontMetrics.getHeight();
        
        // Riduci la dimensione del carattere finché il testo non si adatta al JLabel
        while (textWidth > labelSize.width || textHeight > labelSize.height) {
            Font smallerFont = new Font(font.getName(), font.getStyle(), font.getSize() - 1);
            FontMetrics smallerFontMetrics = label.getFontMetrics(smallerFont);
            
            textWidth = smallerFontMetrics.stringWidth(text);
            textHeight = smallerFontMetrics.getHeight();
            
            font = smallerFont;
        }
        
        label.setFont(font);
    }
    

    @Override
    public int print(Graphics g, PageFormat pf, int page) throws PrinterException {
        if (page > 0) return Printable.NO_SUCH_PAGE;
         Graphics2D g2d = (Graphics2D)g;
         //g2d.translate(pf.getImageableX(), pf.getImageableY() - 5);
         //g2d.translate(pf.getImageableX(), pf.getImageableY());
         panel.print(g2d);
         printAll(g2d);  
        return Printable.PAGE_EXISTS;
    }
                 
}