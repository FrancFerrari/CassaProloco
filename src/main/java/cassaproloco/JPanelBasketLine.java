package cassaproloco;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Toolkit;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

/**
 * Una riga del carrello: nome a sinistra, a destra i controlli
 * (− quantità +), prezzo, toggle Separato/Unito e cancella.
 *
 * <p>Usa un {@code GridBagLayout}: il nome ha {@code weightx=1} (si comprime),
 * i controlli mantengono dimensione fissa sul lato destro e restano sempre
 * visibili anche quando la riga viene stesa alla larghezza del carrello.
 */
public class JPanelBasketLine extends JPanel {

    private final boolean isGrouped;
    private final Item i;
    private final GroupedItem gi;
    private final JPanelBasket parent;
    private final int fontSize;

    private final JLabel lblText = new JLabel();
    private final JLabel lblPrice = new JLabel();
    private final RoundedTextField lblQty = new RoundedTextField(1);
    private final JButton btnAdd = new JButton("+");
    private final JButton btnRemove = new JButton("−");
    private final JButton btnDelete = new JButton("X");
    private final JToggleButton selectBtn = new JToggleButton("S");

    public JPanelBasketLine(boolean isGrouped, Item i, GroupedItem gi, JPanelBasket parent) {
        this.isGrouped = isGrouped;
        this.i = i;
        this.gi = gi;
        this.parent = parent;

        int rowHeight = rowHeight(parent);
        this.fontSize = Math.max(14, (int) (rowHeight * 0.42));
        int btnSize = Math.max(28, (int) (rowHeight * 0.6));

        setOpaque(false);
        setBackground(Theme.BASKET_LINE_BG);
        setLayout(new GridBagLayout());
        setPreferredSize(new Dimension(400, rowHeight));
        setMinimumSize(new Dimension(200, rowHeight));
        // larghezza massima ampia: BoxLayout stende la riga alla larghezza del carrello
        setMaximumSize(new Dimension(Short.MAX_VALUE, rowHeight));

        buildLayout(btnSize);
        updateText();
    }

    private static int rowHeight(JPanelBasket parent) {
        int h = parent.getHeight();
        if (h <= 0) {
            h = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight();
        }
        return Math.max(60, h / 11);
    }

    private void buildLayout(int btnSize) {
        // Nome (colonna 0): si espande/comprime, lasciando spazio fisso ai controlli
        lblText.setText(i.getTextToPrint());
        lblText.setForeground(Theme.TEXT_ON_DARK);
        lblText.setFont(new Font("Helvetica", Font.BOLD, fontSize));

        styleButton(btnRemove, btnSize, new Color(115, 72, 97), new Color(255, 225, 156), new Color(78, 108, 135));
        btnRemove.addActionListener(e -> subtractAction());

        lblQty.setEditable(false);
        lblQty.setFocusable(false);
        lblQty.setOpaque(false);
        lblQty.setForeground(Color.BLACK);
        lblQty.setHorizontalAlignment(SwingConstants.CENTER);
        lblQty.setBorder(null);
        lblQty.setText("1");
        lblQty.setFont(new Font("Helvetica", Font.BOLD, fontSize));
        lblQty.setPreferredSize(new Dimension(btnSize, btnSize));

        styleButton(btnAdd, btnSize, new Color(115, 72, 97), new Color(255, 225, 156), new Color(78, 108, 135));
        btnAdd.addActionListener(e -> addAction());

        lblPrice.setForeground(Theme.TEXT_ON_DARK);
        lblPrice.setHorizontalAlignment(SwingConstants.RIGHT);
        lblPrice.setFont(new Font("Helvetica", Font.BOLD, fontSize));
        lblPrice.setPreferredSize(new Dimension(Math.max(110, btnSize * 2), btnSize));

        selectBtn.setUI(new ModernToggleButtonUI(
                new Color(70, 90, 120), new Color(100, 130, 170),
                new Color(40, 60, 90), new Color(80, 110, 150), Color.WHITE));
        selectBtn.setMargin(new Insets(0, 0, 0, 0));
        selectBtn.setPreferredSize(new Dimension(btnSize, btnSize));
        selectBtn.addActionListener(e -> selectBtn.setText(selectBtn.isSelected() ? "U" : "S"));

        styleButton(btnDelete, btnSize, Theme.WARM_BASE, Theme.WARM_HOVER, Theme.WARM_CLICK);
        btnDelete.addActionListener(e -> removeAction());

        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.fill = GridBagConstraints.NONE;

        c.gridx = 0;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 16, 0, 8);
        add(lblText, c);

        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        c.insets = new Insets(0, 4, 0, 4);
        c.gridx = 1; add(btnRemove, c);
        c.gridx = 2; add(lblQty, c);
        c.gridx = 3; add(btnAdd, c);
        c.gridx = 4; add(lblPrice, c);
        c.gridx = 5; add(selectBtn, c);
        c.gridx = 6; c.insets = new Insets(0, 4, 0, 14); add(btnDelete, c);
    }

    private void styleButton(JButton b, int size, Color base, Color hover, Color click) {
        b.setUI(new ModernButtonUI(base, hover, click, Color.WHITE));
        b.setFont(new Font("Tahoma", Font.BOLD, fontSize));
        b.setPreferredSize(new Dimension(size, size));
    }

    public Item getItem() {
        return i;
    }

    public GroupedItem getGroupedItem() {
        return gi;
    }

    public boolean isGrouped() {
        return isGrouped;
    }

    /** true = UNITO (un solo scontrino), false = SEPARATO (uno per unità). */
    public boolean isUnitPrinting() {
        return selectBtn.isSelected();
    }

    public void updateText() {
        int qty = isGrouped
                ? parent.getBasket().getGroupedItemQty(gi)
                : parent.getBasket().getItemQty(i);
        lblQty.setText(String.valueOf(qty));

        float price = isGrouped
                ? parent.getBasket().getGroupedItemTotalPrice(gi)
                : parent.getBasket().getItemTotalPrice(i);
        lblPrice.setText(String.format("%.2f€", price));

        parent.updateTotalText();
    }

    private void addAction() {
        if (isGrouped) parent.getBasket().addGroupedItem(gi);
        else           parent.getBasket().addItem(i);
        updateText();
    }

    private void subtractAction() {
        boolean nowZero = isGrouped
                ? parent.getBasket().subtractGroupedItem(gi) == 0
                : parent.getBasket().subtractItem(i) == 0;
        if (nowZero) removeAction();
        else         updateText();
    }

    private void removeAction() {
        if (isGrouped) parent.getBasket().removeGroupedItem(gi);
        else           parent.getBasket().removeItem(i);
        parent.removeLine(this);
        parent.updateAll();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
        g2.dispose();
    }
}
