package cassaproloco;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Pannello che contiene tutte le righe del carrello, sia Item che GroupedItem.
 */
public class JPanelBasket extends JPanel {
    private final ArrayList<JPanelBasketLine> articles;
    private Basket basket;
    private JLabel lblTotal;

    public JPanelBasket() {
        initComponents();
        setOpaque(false);
        articles = new ArrayList<>();
        clear();
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

    /** Restituisce quante righe sono state aggiunte al pannello */
    public int getArticlesCount() {
        return articles.size();
    }
    
    public JPanelBasketLine getArticles(int idx) {
        return articles.get(idx);
    }
    
    /** Aggiunge un singolo Item. */
    public void addItem(Item i) {
        basket.addItem(i);
        updateOrAddLine(i, null);
    }

    /** Aggiunge un GroupedItem. */
    public void addGroupedItem(GroupedItem gi) {
        basket.addGroupedItem(gi);
        updateOrAddLine(gi.getMenu(), gi);
    }

    private void updateOrAddLine(Item i, GroupedItem gi) {
        for (JPanelBasketLine line : articles) {
            if (line.getItem().equals(i) && line.isGrouped() == (gi != null)) {
                line.updateText();
                refreshUI();
                return;
            }
        }
        JPanelBasketLine newLine = new JPanelBasketLine(gi != null, i, gi, this);
        add(newLine);
        articles.add(newLine);
        refreshUI();
    }

    /** Rimuove tutte le righe e svuota il basket. */
    public final void clear() {
        for (JPanelBasketLine line : articles) {
            super.remove(line);
        }
        articles.clear();
        if (basket != null) basket.clear();
        refreshUI();
    }

    /** Aggiorna tutte le righe e il totale. */
    public void updateAll() {
        for (JPanelBasketLine line : articles) {
            line.updateText();
        }
        updateTotalText();
    }

    /** Mostra il totale nel label impostato. */
    public void updateTotalText() {
        if (lblTotal != null && basket != null) {
            lblTotal.setText(String.format("%.2f€", basket.getTotalPrice()));
            lblTotal.setFont(new Font("Helvetica", Font.BOLD, 50));
            lblTotal.setForeground(Color.WHITE);
            lblTotal.setOpaque(true);
            lblTotal.setBackground(new Color(58, 48, 66));
        }
    }

    private void refreshUI() {
        updateAll();  // ricalcola totali e righe
        revalidate();
        repaint();
    }
    
    /** Rimuove la riga sia dalla UI Swing sia dalla lista interna */
    public void removeLine(JPanelBasketLine line) {
        articles.remove(line);
        super.remove(line);
        refreshUI(); 
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
        Shape rounded = new RoundRectangle2D.Float(
            0, 0, getWidth(), getHeight(), 20, 20
        );
        g2.setColor(getBackground());
        g2.fill(rounded);
        g2.dispose();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
