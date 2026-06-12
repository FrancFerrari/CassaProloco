package cassaproloco;

import static cassaproloco.ReceiptGeometry.fromCMToPPI;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.print.PrinterException;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JLabel;

/**
 * Calcoli di disposizione dello scontrino <i>condivisi</i> tra la stampa
 * ({@link ReceiptModel}) e l'editor ({@link ReceiptCanvas}): dove finisce ogni
 * elemento (per selezionarlo/trascinarlo) e come rendere lo scontrino in
 * un'immagine per l'anteprima.
 *
 * <p>Le formule (margine +50, offset orizzontali per rullino, ecc.) rispecchiano
 * quelle di {@link ReceiptModel}, così l'anteprima coincide con la stampa.
 */
final class ReceiptLayout {

    private ReceiptLayout() {
    }

    /** Un elemento già posizionato: rettangolo nello spazio dello scontrino. */
    static final class Placed {
        final ReceiptElement el;
        final Rectangle bounds;

        Placed(ReceiptElement el, Rectangle bounds) {
            this.el = el;
            this.bounds = bounds;
        }
    }

    static int panelWidth(RollSize roll) {
        return (int) fromCMToPPI(roll.widthCm);
    }

    static int panelHeight() {
        return (int) fromCMToPPI(RollSize.HEIGHT_CM);
    }

    /** Altezza fisica dell'etichetta del rullino (dove taglia davvero). */
    static int labelHeight(RollSize roll) {
        return (int) fromCMToPPI(roll.labelHeightCm);
    }

    /** Posizione/dimensione di ogni elemento visibile (per hit-test e selezione). */
    static List<Placed> place(ReceiptTemplate t, RollSize roll,
                              String qty, String name, String price, Date d) {
        int panelW = panelWidth(roll);
        int dW = panelW - (int) fromCMToPPI(RollSize.MM62.widthCm);
        int dxCenter = dW / 2;
        String dateStr = new SimpleDateFormat("dd/MM/yyyy").format(d);
        String timeStr = new SimpleDateFormat("HH:mm:ss").format(d);

        List<Placed> out = new ArrayList<>();
        for (ReceiptElement el : t.elements) {
            if (!el.visible) {
                continue;
            }
            int xOff = el.anchor == ReceiptElement.Anchor.RIGHT ? dW
                     : el.anchor == ReceiptElement.Anchor.CENTER ? dxCenter
                     : 0;
            int x = el.x + xOff;

            if (el.kind == ReceiptElement.Kind.LOGO) {
                if (logoImage(el) == null) {
                    continue;
                }
                int w = el.imgW > 0 ? el.imgW : 40;
                int h = el.imgH > 0 ? el.imgH : 40;
                out.add(new Placed(el, new Rectangle(x, el.y, w, h)));
                continue;
            }

            String content = resolveText(el, qty, name, price, dateStr, timeStr);
            if (content.isEmpty()) {
                continue;
            }
            JLabel lbl = new JLabel(content);
            lbl.setFont(font(el));
            java.awt.Dimension pref = lbl.getPreferredSize();
            int w = el.width == ReceiptElement.WIDTH_FULL ? panelW
                  : el.width == ReceiptElement.WIDTH_AUTO ? pref.width + 50
                  : el.width;
            int h = el.height == ReceiptElement.HEIGHT_AUTO ? pref.height : el.height;
            out.add(new Placed(el, new Rectangle(x, el.y, w, h)));
        }
        return out;
    }

    static String resolveText(ReceiptElement el, String qty, String name,
                              String price, String dateStr, String timeStr) {
        switch (el.kind) {
            case ITEM:  return qty + "x " + name;
            case PRICE: return (price == null || price.trim().isEmpty()) ? "" : price + "€";
            case DATE:  return el.text + dateStr;
            case TIME:  return el.text + timeStr;
            default:    return el.text == null ? "" : el.text; // HEADER1/2, FOOTER
        }
    }

    static Font font(ReceiptElement el) {
        return new Font(el.fontFamily, el.bold ? Font.BOLD : Font.PLAIN, el.fontSize);
    }

    static Image logoImage(ReceiptElement el) {
        if (el.imagePath == null || el.imagePath.trim().isEmpty()) {
            return null;
        }
        File f = new File(el.imagePath);
        if (!f.isFile()) {
            return null;
        }
        try {
            return ImageIO.read(f);
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Rende lo scontrino in un'immagine ingrandita di {@code scale} (per
     * un'anteprima nitida): si disegna alla risoluzione finale invece di
     * ingrandire un'immagine piccola.
     */
    static BufferedImage render(ReceiptTemplate t, RollSize roll,
                                String qty, String name, String price, Date d, int scale) {
        int w = panelWidth(roll) * scale;
        int h = panelHeight() * scale;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, w, h);
        g.scale(scale, scale); // disegna nello spazio scontrino, ma a risoluzione scale×
        ReceiptModel rm = new ReceiptModel(price, qty, name, d, roll, t);
        try {
            rm.print(g, ReceiptGeometry.pageFormat(roll), 0);
        } catch (PrinterException ignored) {
            // rendering su immagine: non può davvero fallire
        }
        g.dispose();
        return img;
    }
}
