package cassaproloco;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Date;
import java.util.List;

import javax.swing.JComponent;

/**
 * Anteprima interattiva dello scontrino per l'editor: mostra lo scontrino
 * ingrandito, evidenzia gli elementi, e permette di <b>selezionarli col clic</b>
 * e <b>spostarli trascinandoli</b>. Il rendering è quello reale di stampa
 * ({@link ReceiptLayout#render}), così l'anteprima coincide con la stampa.
 */
class ReceiptCanvas extends JComponent {

    /** Notifiche verso la finestra dell'editor. */
    interface Listener {
        void onSelected(ReceiptElement el);
        void onChanged();
    }

    private static final int PAD = 12;
    private static final int SCALE = 4;

    // dati di esempio mostrati nell'anteprima
    private static final String SMP_QTY = "1";
    private static final String SMP_NAME = "Coca-cola alla spina";
    private static final String SMP_PRICE = "2.50";

    private ReceiptTemplate template;
    private RollSize roll = RollSize.MM62;
    private final Date now = new Date();
    private Listener listener;

    private ReceiptElement selected;
    private BufferedImage image;
    private List<ReceiptLayout.Placed> placed;

    private boolean dragging;
    private Point lastPoint;

    ReceiptCanvas(ReceiptTemplate template) {
        this.template = template;
        setOpaque(true);
        MouseAdapter ma = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { onPress(e.getPoint()); }
            @Override public void mouseDragged(MouseEvent e) { onDrag(e.getPoint()); }
            @Override public void mouseReleased(MouseEvent e) { dragging = false; }
        };
        addMouseListener(ma);
        addMouseMotionListener(ma);
        refresh();
    }

    void setListener(Listener l) {
        this.listener = l;
    }

    void setTemplate(ReceiptTemplate t) {
        this.template = t;
        this.selected = null;
        refresh();
    }

    void setRoll(RollSize r) {
        this.roll = r;
        refresh();
    }

    RollSize getRoll() {
        return roll;
    }

    /** Imposta l'elemento selezionato (es. dalla lista nella finestra). */
    void setSelected(ReceiptElement el) {
        this.selected = el;
        repaint();
    }

    ReceiptElement getSelected() {
        return selected;
    }

    /** Ricostruisce immagine e posizioni e ridisegna. */
    final void refresh() {
        image = ReceiptLayout.render(template, roll, SMP_QTY, SMP_NAME, SMP_PRICE, now, SCALE);
        placed = ReceiptLayout.place(template, roll, SMP_QTY, SMP_NAME, SMP_PRICE, now);
        revalidate();
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(ReceiptLayout.panelWidth(roll) * SCALE + 2 * PAD,
                ReceiptLayout.panelHeight() * SCALE + 2 * PAD);
    }

    private void onPress(Point p) {
        ReceiptElement hit = elementAt(p);
        selected = hit;
        if (listener != null) {
            listener.onSelected(hit);
        }
        if (hit != null) {
            dragging = true;
            lastPoint = p;
        }
        repaint();
    }

    private void onDrag(Point p) {
        if (!dragging || selected == null || lastPoint == null) {
            return;
        }
        int dx = Math.round((p.x - lastPoint.x) / (float) SCALE);
        int dy = Math.round((p.y - lastPoint.y) / (float) SCALE);
        if (dx == 0 && dy == 0) {
            return;
        }
        int panelW = ReceiptLayout.panelWidth(roll);
        int panelH = ReceiptLayout.panelHeight();
        selected.x = clamp(selected.x + dx, -30, panelW);
        selected.y = clamp(selected.y + dy, -5, panelH - 4);
        lastPoint = p;
        refresh();
        if (listener != null) {
            listener.onChanged();
        }
    }

    private static int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    /** Elemento (topmost) sotto il punto schermo, o null. */
    private ReceiptElement elementAt(Point p) {
        if (placed == null) {
            return null;
        }
        int rx = (p.x - PAD) / SCALE;
        int ry = (p.y - PAD) / SCALE;
        for (int i = placed.size() - 1; i >= 0; i--) {
            if (placed.get(i).bounds.contains(rx, ry)) {
                return placed.get(i).el;
            }
        }
        return null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // sfondo
        g2.setColor(new Color(228, 231, 236));
        g2.fillRect(0, 0, getWidth(), getHeight());

        int w = ReceiptLayout.panelWidth(roll) * SCALE;
        int h = ReceiptLayout.panelHeight() * SCALE;

        // ombra + scontrino
        g2.setColor(new Color(0, 0, 0, 40));
        g2.fillRoundRect(PAD + 3, PAD + 3, w, h, 10, 10);
        if (image != null) {
            g2.drawImage(image, PAD, PAD, w, h, null);
        }
        g2.setColor(new Color(180, 184, 190));
        g2.drawRect(PAD, PAD, w, h);

        // riquadri elementi: tutti tenui, il selezionato in blu
        if (placed != null) {
            for (ReceiptLayout.Placed pl : placed) {
                Rectangle b = pl.bounds;
                int x = PAD + b.x * SCALE, y = PAD + b.y * SCALE;
                int bw = b.width * SCALE, bh = b.height * SCALE;
                if (pl.el == selected) {
                    g2.setColor(new Color(62, 124, 177));
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawRect(x - 1, y - 1, bw + 2, bh + 2);
                } else {
                    g2.setColor(new Color(120, 140, 165, 90));
                    g2.setStroke(new BasicStroke(1f));
                    g2.drawRect(x, y, bw, bh);
                }
            }
        }

        // linea di taglio del rullino: oltre questa quota il rullino corrente non stampa
        int labelH = ReceiptLayout.labelHeight(roll);
        if (labelH < ReceiptLayout.panelHeight()) {
            int cy = PAD + labelH * SCALE;
            g2.setColor(new Color(120, 120, 120, 70));
            g2.fillRect(PAD, cy, w, (PAD + h) - cy);
            g2.setColor(new Color(200, 70, 66));
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                    1f, new float[] {6f, 5f}, 0f));
            g2.drawLine(PAD, cy, PAD + w, cy);
            g2.setStroke(new BasicStroke(1f));
            g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g2.drawString("taglio " + roll.label, PAD + 6, cy + 16);
        }
        g2.dispose();
    }
}
