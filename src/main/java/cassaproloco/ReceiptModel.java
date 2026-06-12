package cassaproloco;

import static cassaproloco.ReceiptGeometry.fromCMToPPI;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

/**
 * Modello dello scontrino: un {@link Printable} che disegna le etichette
 * (intestazione, "Nx Nome", prezzo, data, ora) a coordinate fisse su un pannello
 * {@link Paint}.
 *
 * <p>Il layout (posizioni/font delle etichette) sul rullino storico da 62 mm è
 * <b>invariato</b>: lo scontrino stampato resta identico. Su rullini più stretti
 * (es. 54 mm) cambia solo la larghezza e le poche etichette ad ancoraggio fisso
 * (prezzo, intestazioni) vengono traslate orizzontalmente: gli spostamenti sono
 * nulli a 62 mm, quindi il caso storico non cambia di un pixel. Rispetto a prima
 * non viene più creato un {@code JFrame} per ogni scontrino e la geometria è in
 * {@link ReceiptGeometry}.
 */
public class ReceiptModel extends javax.swing.JPanel implements Printable {

    private final Paint panel;

    /** Scontrino sul rullino storico da 62 mm (layout identico a prima). */
    public ReceiptModel(String price, String numelements, String name, Date d) {
        this(price, numelements, name, d, RollSize.MM62);
    }

    public ReceiptModel(String price, String numelements, String name, Date d, RollSize roll) {
        panel = new Paint();
        panel.setLayout(null);
        panel.setBackground(Color.white);
        panel.setSize((int) fromCMToPPI(roll.widthCm), (int) fromCMToPPI(RollSize.HEIGHT_CM));

        // Traslazioni orizzontali rispetto al layout di riferimento (62 mm):
        // a 62 mm dW=0 e dxCenter=0, quindi tutte le coordinate restano IDENTICHE.
        int wRef = (int) fromCMToPPI(RollSize.MM62.widthCm);
        int dW = panel.getWidth() - wRef;   // 0 a 62 mm, negativo su rullini più stretti
        int dxCenter = dW / 2;              // per le intestazioni centrate

        if (price != null && !price.trim().isEmpty()) {
            JLabel priceModel = new JLabel(price + "€");
            panel.add(priceModel);
            priceModel.setFont(new Font("Tahoma", 0, 12));
            Dimension size = priceModel.getPreferredSize();
            priceModel.setBounds(130 + dW, 55, size.width, size.height);
        }

        JLabel textModel = new JLabel(numelements + "x " + name);
        textModel.setFont(new Font("Tahoma", Font.BOLD, 12));
        textModel.setBounds(-5, 30, panel.getWidth(), 18);
        textModel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(textModel);

        JLabel proloco = new JLabel("ProLoco Cogollo");
        panel.add(proloco);
        proloco.setFont(new Font("Tahoma", 0, 8));
        Dimension size2 = proloco.getPreferredSize();
        proloco.setBounds(55 + dxCenter, 17, size2.width + 50, size2.height);

        JLabel sagra = new JLabel("Antica Sagra di S.Luigi ");
        panel.add(sagra);
        sagra.setFont(new Font("Tahoma", Font.BOLD, 8));
        Dimension size4 = sagra.getPreferredSize();
        sagra.setBounds(39 + dxCenter, 7, size4.width + 50, size4.height);

        String dateString = new SimpleDateFormat("dd/MM/yyyy").format(d);
        JLabel label = new JLabel("Data:    " + dateString);
        label.setFont(new Font("Tahoma", 0, 6));
        Dimension size5 = label.getPreferredSize();
        int labelWidth = panel.getWidth() - 30; // margine di 15px a sx e dx
        label.setBounds(15, 55, labelWidth, size5.height);
        panel.add(label);

        String timeString = new SimpleDateFormat("HH:mm:ss").format(d);
        JLabel timeLabel = new JLabel("Ora:     " + timeString);
        timeLabel.setFont(new Font("Tahoma", 0, 6));
        Dimension sizeTime = timeLabel.getPreferredSize();
        timeLabel.setBounds(15, 55 + size5.height + 2, labelWidth, sizeTime.height);
        panel.add(timeLabel);
    }

    @Override
    public int print(Graphics g, PageFormat pf, int page) throws PrinterException {
        if (page > 0) {
            return Printable.NO_SUCH_PAGE;
        }
        Graphics2D g2d = (Graphics2D) g;
        panel.print(g2d);
        printAll(g2d);
        return Printable.PAGE_EXISTS;
    }
}
