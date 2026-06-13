package cassaproloco;

import static cassaproloco.ReceiptGeometry.fromCMToPPI;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

/**
 * Modello dello scontrino: un {@link Printable} che disegna gli elementi del
 * {@link ReceiptTemplate} (intestazioni, "Nx nome", prezzo, data, ora, riga in
 * fondo, logo) a coordinate fisse su un pannello {@link Paint}.
 *
 * <p>Col template <b>predefinito</b> il layout è quello storico: lo scontrino
 * stampato resta identico. Le coordinate del template sono nello spazio a 62 mm;
 * su rullini più stretti gli elementi vengono traslati orizzontalmente in base al
 * loro {@link ReceiptElement.Anchor} (spostamenti nulli a 62 mm). La via di stampa
 * non cambia: questo resta l'unico punto che disegna lo scontrino.
 */
public class ReceiptModel extends javax.swing.JPanel implements Printable {

    private final Paint panel;

    /** Scontrino sul rullino storico da 62 mm, template predefinito (identico a prima). */
    public ReceiptModel(String price, String numelements, String name, Date d) {
        this(price, numelements, name, d, RollSize.MM62);
    }

    /** Scontrino sul rullino indicato, template predefinito. */
    public ReceiptModel(String price, String numelements, String name, Date d, RollSize roll) {
        this(price, numelements, name, d, roll, ReceiptTemplate.defaultTemplate());
    }

    public ReceiptModel(String price, String numelements, String name, Date d,
                        RollSize roll, ReceiptTemplate template) {
        panel = new Paint();
        panel.setLayout(null);
        panel.setBackground(Color.white);
        panel.setSize((int) fromCMToPPI(roll.widthCm), (int) fromCMToPPI(RollSize.HEIGHT_CM));

        // Traslazioni orizzontali rispetto al riferimento 62 mm (nulle a 62 mm).
        int wRef = (int) fromCMToPPI(RollSize.MM62.widthCm);
        int dW = panel.getWidth() - wRef;
        int dxCenter = dW / 2;

        String dateString = new SimpleDateFormat("dd/MM/yyyy").format(d);
        String timeString = new SimpleDateFormat("HH:mm:ss").format(d);

        for (ReceiptElement el : template.elements) {
            if (!el.visible) {
                continue;
            }
            int xOff = el.anchor == ReceiptElement.Anchor.RIGHT ? dW
                     : el.anchor == ReceiptElement.Anchor.CENTER ? dxCenter
                     : 0;
            int x = el.x + xOff;

            if (el.kind == ReceiptElement.Kind.LOGO) {
                addLogo(el, x);
                continue;
            }

            String content = resolveText(el, numelements, name, price, dateString, timeString);
            if (content.isEmpty()) {
                continue; // es. prezzo assente: come prima, non si stampa
            }
            addText(el, x, content);
        }
    }

    /** Testo da mostrare per l'elemento (fisso o dinamico). */
    private static String resolveText(ReceiptElement el, String qty, String name,
                                      String price, String dateString, String timeString) {
        switch (el.kind) {
            case ITEM:
                return qty + "x " + name;
            case PRICE:
                return (price == null || price.trim().isEmpty()) ? "" : price + "€";
            case DATE:
                return el.text + dateString;
            case TIME:
                return el.text + timeString;
            default: // HEADER1, HEADER2, FOOTER
                return el.text == null ? "" : el.text;
        }
    }

    private void addText(ReceiptElement el, int x, String content) {
        JLabel lbl = new JLabel(content);
        lbl.setFont(new Font(el.fontFamily, el.bold ? Font.BOLD : Font.PLAIN, el.fontSize));
        lbl.setHorizontalAlignment(swingAlign(el.align));
        Dimension pref = lbl.getPreferredSize();
        // +50 di margine come nel layout storico: evita che l'ultima lettera venga
        // tagliata (la larghezza preferita a volte sottostima di un paio di pixel).
        // Per testo allineato a sinistra non sposta nulla; serve solo a non tagliare.
        int w = el.width == ReceiptElement.WIDTH_FULL ? panel.getWidth()
              : el.width == ReceiptElement.WIDTH_AUTO ? pref.width + 50
              : el.width;
        int h = el.height == ReceiptElement.HEIGHT_AUTO ? pref.height : el.height;
        lbl.setBounds(x, el.y, w, h);
        panel.add(lbl);
    }

    private void addLogo(ReceiptElement el, int x) {
        if (el.imagePath == null || el.imagePath.trim().isEmpty()) {
            return;
        }
        File f = new File(el.imagePath);
        if (!f.isFile()) {
            return;
        }
        try {
            Image img = ImageIO.read(f);
            if (img == null) {
                return;
            }
            int w = el.imgW > 0 ? el.imgW : img.getWidth(null);
            int h = el.imgH > 0 ? el.imgH : img.getHeight(null);
            // Disegna l'immagine ORIGINALE scalata al riquadro alla risoluzione di
            // stampa (interpolazione liscia), invece di pre-ridurla a pochi pixel.
            ImagePanel ip = new ImagePanel(img);
            ip.setBounds(x, el.y, w, h);
            panel.add(ip);
        } catch (Exception ignored) {
            // logo illeggibile: semplicemente non lo si stampa (la stampa non si rompe)
        }
    }

    /** Disegna un'immagine scalata ai propri limiti a piena risoluzione (logo nitido). */
    private static final class ImagePanel extends JComponent {
        private final Image img;
        ImagePanel(Image img) {
            this.img = img;
            setOpaque(false);
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.drawImage(img, 0, 0, getWidth(), getHeight(), null);
            g2.dispose();
        }
    }

    private static int swingAlign(ReceiptElement.Align a) {
        switch (a) {
            case CENTER: return SwingConstants.CENTER;
            case RIGHT:  return SwingConstants.RIGHT;
            default:     return SwingConstants.LEFT;
        }
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
