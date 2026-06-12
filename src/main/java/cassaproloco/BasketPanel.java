package cassaproloco;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Scrollable;

/**
 * Pannello che contiene tutte le righe del carrello (Item singoli e GroupedItem)
 * e tiene aggiornato il totale. Usa un {@code BoxLayout} verticale.
 *
 * <p>Implementa {@link Scrollable} con {@code tracksViewportWidth = true} così le
 * righe seguono la larghezza del viewport e i controlli restano sempre visibili.
 */
public class BasketPanel extends JPanel implements Scrollable {

    private final List<BasketLinePanel> articles = new ArrayList<>();
    private Basket basket;
    private JLabel lblTotal;
    private JLabel lblCount;

    public BasketPanel() {
        setOpaque(false);
        setBackground(Theme.BASKET_BG);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    public void setBasket(Basket basket) {
        this.basket = basket;
        basket.setParent(this);
    }

    public Basket getBasket() {
        return basket;
    }

    public void setTotalLabel(JLabel lbl) {
        this.lblTotal = lbl;
    }

    public void setCountLabel(JLabel lbl) {
        this.lblCount = lbl;
    }

    /** Toglie un pezzo dall'ultima riga aggiunta ("annulla ultimo"). */
    public void removeLastUnit() {
        if (!articles.isEmpty()) {
            articles.get(articles.size() - 1).decrement();
        }
    }

    public int getArticlesCount() {
        return articles.size();
    }

    public BasketLinePanel getArticles(int idx) {
        return articles.get(idx);
    }

    /** Aggiunge un singolo Item al carrello. */
    public void addItem(Item i) {
        basket.addItem(i);
        updateOrAddLine(i, null);
    }

    /** Aggiunge un GroupedItem (menu combinato) al carrello. */
    public void addGroupedItem(GroupedItem gi) {
        basket.addGroupedItem(gi);
        updateOrAddLine(gi.getMenu(), gi);
    }

    private void updateOrAddLine(Item i, GroupedItem gi) {
        for (BasketLinePanel line : articles) {
            if (line.getItem().equals(i) && line.isGrouped() == (gi != null)) {
                line.updateText();
                updateTotalText();
                return;
            }
        }
        articles.add(new BasketLinePanel(gi != null, i, gi, this));
        rebuild();
    }

    /** Rimuove tutte le righe e svuota il basket. */
    public final void clear() {
        articles.clear();
        if (basket != null) {
            basket.clear();
        }
        rebuild();
    }

    /** Ricostruisce la lista di righe (BoxLayout verticale) con spaziatura. */
    private void rebuild() {
        removeAll();
        for (BasketLinePanel line : articles) {
            line.setAlignmentX(LEFT_ALIGNMENT);
            add(line);
            add(Box.createVerticalStrut(10));
        }
        updateAll();
        revalidate();
        repaint();
    }

    /** Aggiorna tutte le righe e il totale. */
    public void updateAll() {
        for (BasketLinePanel line : articles) {
            line.updateText();
        }
        updateTotalText();
    }

    /** Mostra il totale (e il contatore articoli) nei label impostati. */
    public void updateTotalText() {
        if (basket == null) {
            return;
        }
        if (lblTotal != null) {
            lblTotal.setText(Money.format(basket.getTotalPrice()) + "€");
            lblTotal.setFont(Theme.TOTAL_FONT);
            lblTotal.setForeground(Theme.PRIMARY);
            lblTotal.setOpaque(true);
            lblTotal.setBackground(Theme.BACKGROUND);
        }
        if (lblCount != null) {
            int n = basket.totalQuantity();
            lblCount.setText(n == 1 ? "1 articolo" : n + " articoli");
        }
    }

    /** Rimuove la riga sia dalla UI sia dalla lista interna. */
    public void removeLine(BasketLinePanel line) {
        articles.remove(line);
        rebuild();
    }

    // --- Scrollable: le righe seguono la larghezza del viewport ---

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 24;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return visibleRect.height;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Shape rounded = new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20);
        g2.setColor(getBackground());
        g2.fill(rounded);
        g2.dispose();
    }
}
