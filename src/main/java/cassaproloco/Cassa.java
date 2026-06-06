package cassaproloco;

import com.formdev.flatlaf.FlatLightLaf;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;

/**
 * Finestra principale della cassa: a sinistra le categorie (primi/secondi/bere)
 * e la barra di navigazione, a destra la toolbar, il carrello e il totale/stampa.
 *
 * <p>La logica di dominio è delegata ai servizi ({@link MenuConfigLoader},
 * {@link GroupedItemStore}, {@link SalesRecorder}); la creazione dei menu
 * combinati al {@link MenuBuilderPanel}.
 */
public class Cassa extends JFrame {

    // --- Dominio / servizi ---
    private final Basket basket = new Basket();
    private final GroupedItemStore store =
            new GroupedItemStore(AppPaths.file("groupedItems.json"), AppPaths.file("groupedItems.ser"));
    private List<GroupedItem> groupedItemList = new ArrayList<>();

    private final List<Item> itemsBere = new ArrayList<>();
    private final List<Item> itemsPrimi = new ArrayList<>();
    private final List<Item> itemsSecondi = new ArrayList<>();

    private final int width;
    private final int height;

    // --- Componenti UI ---
    private JPanel primi;       // griglia PRIMI (ospita anche i menu combinati)
    private JPanel bere;        // griglia BERE
    private JPanel secondi;     // griglia SECONDI
    private JPanel selezione;   // stack (OverlayLayout) delle categorie
    private MenuBuilderPanel menuBuilder;
    private JPanelBasket basketPanel;
    private JScrollPane basketScroll;
    private JLabel lblTotal;

    public Cassa() throws IOException {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        width = (int) screen.getWidth();
        height = (int) screen.getHeight();

        setTitle("CassaProloco");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        getContentPane().setBackground(Theme.BACKGROUND);
        setLayout(new BorderLayout());

        add(buildLeftSide(), BorderLayout.LINE_START);
        add(buildRightSide(), BorderLayout.CENTER);

        wireBasket();
        loadMenuItems();
        menuBuilder.setItems(itemsBere, itemsPrimi, itemsSecondi);
        loadGroupedItems();
        showCategory(primi);
    }

    // ============================ LATO SINISTRO ============================

    private JPanel buildLeftSide() {
        selezione = new JPanel(new java.awt.CardLayout());
        selezione.setBackground(Theme.BACKGROUND);

        primi = categoryGrid(new GridLayout(4, 5, 4, 4), new Dimension(700, 661));
        secondi = categoryGrid(new GridLayout(5, 5, 4, 4), new Dimension(700, 661));
        bere = categoryGrid(new GridLayout(5, 5, 4, 4), new Dimension(500, 661));
        menuBuilder = new MenuBuilderPanel();
        menuBuilder.setListener(this::onMenuCreated);

        selezione.add(primi, "primi");
        selezione.add(bere, "bere");
        selezione.add(secondi, "secondi");
        selezione.add(menuBuilder, "menu");

        JPanel sx = new JPanel(new BorderLayout());
        sx.setBackground(Theme.BACKGROUND);
        sx.setPreferredSize(new Dimension((int) (width * 0.4), height));
        sx.setBorder(new LineBorder(Theme.BACKGROUND, Theme.BORDER));
        sx.add(buildNavBar(), BorderLayout.PAGE_START);
        sx.add(selezione, BorderLayout.CENTER);
        return sx;
    }

    private JPanel categoryGrid(GridLayout layout, Dimension preferred) {
        JPanel grid = new JPanel(layout);
        grid.setOpaque(false);
        grid.setPreferredSize(preferred);
        return grid;
    }

    private JPanel buildNavBar() {
        JPanel nav = new JPanel(new GridLayout(1, 0, 2, 0));
        nav.setBackground(Theme.BACKGROUND);
        nav.setPreferredSize(new Dimension(width / 2, height / 6));
        int fontSize = (int) (height * 0.03);
        nav.add(navButton("PRIMI", fontSize, () -> showCategory(primi)));
        nav.add(navButton("BERE", fontSize, () -> showCategory(bere)));
        nav.add(navButton("SECONDI", fontSize, () -> showCategory(secondi)));
        return nav;
    }

    private JButton navButton(String text, int fontSize, Runnable action) {
        JButton b = new JButton(text);
        b.setUI(new ModernButtonUI(Theme.WARM_BASE, Theme.WARM_HOVER, Theme.WARM_CLICK, Color.WHITE));
        b.setFont(new Font("Segoe UI", Font.BOLD, fontSize));
        b.setPreferredSize(new Dimension(width / 4, height / 10));
        b.addActionListener(e -> action.run());
        return b;
    }

    /** Mostra solo la categoria indicata (CardLayout). */
    private void showCategory(JPanel toShow) {
        String name = "primi";
        if (toShow == bere) name = "bere";
        else if (toShow == secondi) name = "secondi";
        else if (toShow == menuBuilder) name = "menu";
        ((java.awt.CardLayout) selezione.getLayout()).show(selezione, name);
    }

    // ============================ LATO DESTRO ============================

    private JPanel buildRightSide() {
        JPanel right = new JPanel(new BorderLayout(0, 10));
        right.setBackground(Theme.BACKGROUND);
        right.setPreferredSize(new Dimension((int) (width * 0.6), height));
        right.setBorder(new LineBorder(Theme.BACKGROUND, Theme.BORDER));

        right.add(buildToolbar(), BorderLayout.PAGE_START);
        right.add(buildBasketArea(), BorderLayout.CENTER);
        right.add(buildBottomBar(), BorderLayout.PAGE_END);
        return right;
    }

    private JPanel buildToolbar() {
        JPanel toolbar = new JPanel();
        toolbar.setBackground(Theme.BACKGROUND);
        int fontSize = (int) (height * 0.02);

        toolbar.add(toolbarButton("RESOCONTO", Theme.PRIMARY, Theme.SECONDARY, Theme.ACCENT, fontSize,
                this::showSalesReport));
        toolbar.add(toolbarButton("OMAGGIO",
                new Color(70, 130, 180), new Color(100, 160, 210), new Color(40, 90, 140), fontSize,
                basket::setPricesToZero));
        toolbar.add(toolbarButton("MENU'",
                new Color(90, 150, 90), new Color(120, 180, 120), new Color(60, 120, 60), fontSize,
                () -> showCategory(menuBuilder)));
        return toolbar;
    }

    private JButton toolbarButton(String text, Color base, Color hover, Color click, int fontSize, Runnable action) {
        JButton b = new JButton(text);
        b.setUI(new ModernButtonUI(base, hover, click, Color.WHITE));
        b.setFont(new Font("Helvetica", Font.BOLD, fontSize));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(120, 40));
        b.addActionListener(e -> action.run());
        return b;
    }

    private JScrollPane buildBasketArea() {
        basketPanel = new JPanelBasket();
        basketPanel.setBackground(Theme.BASKET_BG);
        basketPanel.setLayout(new VerticalFlowLayout());

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBorder(new EmptyBorder(20, 0, 0, 0));
        wrapper.setBackground(Theme.BASKET_BG);
        wrapper.add(basketPanel, BorderLayout.CENTER);

        basketScroll = new JScrollPane(wrapper);
        basketScroll.setBackground(Theme.BASKET_BG);
        basketScroll.setBorder(null);
        basketScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        basketScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        basketScroll.setPreferredSize(new Dimension(260, 300));
        basketScroll.getVerticalScrollBar().setUI(slimScrollBarUI());

        DragScrollListener dl = new DragScrollListener(basketPanel);
        basketScroll.addMouseListener(dl);
        basketScroll.addMouseMotionListener(dl);
        return basketScroll;
    }

    private BasicScrollBarUI slimScrollBarUI() {
        return new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = Theme.PRIMARY;
                this.thumbDarkShadowColor = Theme.PRIMARY;
                this.thumbLightShadowColor = Theme.PRIMARY;
                this.trackColor = new Color(238, 238, 238);
            }
            @Override
            protected JButton createDecreaseButton(int orientation) {
                return zeroButton();
            }
            @Override
            protected JButton createIncreaseButton(int orientation) {
                return zeroButton();
            }
            private JButton zeroButton() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                b.setMinimumSize(new Dimension(0, 0));
                b.setMaximumSize(new Dimension(0, 0));
                return b;
            }
        };
    }

    private JPanel buildBottomBar() {
        JPanel bottom = new JPanel(new GridLayout(1, 0));
        bottom.setBackground(Theme.BACKGROUND);
        bottom.setPreferredSize(new Dimension(width / 2, height / 10));

        JButton btnPrint = new JButton("STAMPA");
        btnPrint.setUI(new ModernButtonUI(Theme.WARM_BASE, Theme.WARM_HOVER, Theme.WARM_CLICK, Color.WHITE));
        btnPrint.setFont(new Font("Segoe UI", Font.BOLD, 25));
        btnPrint.setHorizontalAlignment(SwingConstants.LEFT);
        btnPrint.addActionListener(e -> printAndRecord());
        bottom.add(btnPrint);

        lblTotal = new JLabel("0.00€");
        lblTotal.setHorizontalAlignment(SwingConstants.RIGHT);
        bottom.add(lblTotal);
        return bottom;
    }

    private void wireBasket() {
        basket.setParent(basketPanel);
        basketPanel.setBasket(basket);
        basketPanel.setTotalLabel(lblTotal);
        basketPanel.clear();
    }

    // ============================ MENU / DATI ============================

    /** Crea un pulsante categoria che aggiunge il proprio Item al carrello. */
    private JButton createMenuButton(String text, ActionListener listener) {
        JButton b = new JButton(text);
        b.setUI(new ModernButtonUI(Theme.PRIMARY, Theme.SECONDARY, Theme.ACCENT, Color.WHITE));
        Dimension size = new Dimension(200, 50);
        b.setPreferredSize(size);
        b.setMaximumSize(size);
        b.setMinimumSize(size);
        b.setFont(adjustFontToFit(b, text, size.width));
        b.addActionListener(listener);
        return b;
    }

    private Font adjustFontToFit(JButton button, String text, int boxWidth) {
        Font font = Theme.BUTTON_FONT;
        int available = boxWidth - 20;
        int size = font.getSize();
        while (button.getFontMetrics(font).stringWidth(text) > available && size > 8) {
            size--;
            font = font.deriveFont((float) size);
        }
        return font;
    }

    private void loadMenuItems() {
        readItemsFile(AppPaths.file("primi.cfg"), itemsPrimi, primi);
        readItemsFile(AppPaths.file("bere.cfg"), itemsBere, bere);
        readItemsFile(AppPaths.file("secondi.cfg"), itemsSecondi, secondi);
    }

    private void readItemsFile(File file, List<Item> listItems, JPanel panel) {
        try {
            for (Item item : MenuConfigLoader.load(file)) {
                listItems.add(item);
                panel.add(createMenuButton(item.getText(), e -> basketPanel.addItem(item)));
            }
        } catch (IOException ex) {
            Logger.getLogger(Cassa.class.getName())
                  .log(Level.SEVERE, "Errore lettura file " + file.getName(), ex);
            JOptionPane.showMessageDialog(this,
                "Errore lettura file " + file.getName() + ": " + ex.getMessage(),
                "ERRORE", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void loadGroupedItems() {
        groupedItemList = store.load();
        for (GroupedItem gi : groupedItemList) {
            importMenu(gi);
        }
    }

    /** Aggiunge il pulsante di un menu combinato (con menu contestuale di rimozione). */
    public void importMenu(GroupedItem gi) {
        JButton b = createMenuButton(gi.getText(GroupedItem.Course.MENU),
                e -> basketPanel.addGroupedItem(gi));

        JPopupMenu popup = new JPopupMenu();
        JMenuItem removeItem = new JMenuItem("Rimuovi menu");
        removeItem.addActionListener(e -> removeGroupedItem(gi, b));
        popup.add(removeItem);
        b.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) popup.show(b, e.getX(), e.getY());
            }
            @Override public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) popup.show(b, e.getX(), e.getY());
            }
        });

        primi.add(b);
        primi.revalidate();
        primi.repaint();
    }

    private void removeGroupedItem(GroupedItem gi, JButton button) {
        primi.remove(button);
        primi.revalidate();
        primi.repaint();
        groupedItemList.remove(gi);
        try {
            store.save(groupedItemList);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                "Errore salvataggio dopo rimozione: " + ex.getMessage(),
                "ERRORE", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Callback del MenuBuilderPanel: salva e mostra il nuovo menu combinato. */
    private void onMenuCreated(GroupedItem gi) {
        groupedItemList.add(gi);
        try {
            store.save(groupedItemList);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                "Errore salvataggio menu: " + ex.getMessage(), "ERRORE", JOptionPane.ERROR_MESSAGE);
        }
        importMenu(gi);
    }

    private void showSalesReport() {
        JPanel pannello = new PannelloResocontoVendite(AppPaths.base());
        JFrame frame = new JFrame("Resoconto Vendite");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setContentPane(pannello);
        frame.setSize(500, 400);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // ============================ STAMPA ============================

    /** Stampa gli scontrini del carrello e registra le vendite su CSV. */
    private void printAndRecord() {
        PrinterJob job = PrinterJob.getPrinterJob();
        PageFormat pf = job.defaultPage();
        Paper paper = pf.getPaper();
        double w = fromCMToPPI(6.2), h = fromCMToPPI(4);
        paper.setSize(w, h);
        paper.setImageableArea(fromCMToPPI(0.25), fromCMToPPI(0), w, h - fromCMToPPI(1));
        pf.setOrientation(PageFormat.PORTRAIT);
        pf.setPaper(paper);

        LocalDate today = LocalDate.now();
        String todayStr = today.format(DateTimeFormatter.ISO_DATE);
        File file = AppPaths.file("report_" + todayStr + ".csv");

        List<String[]> toWrite = new ArrayList<>();
        if (!file.exists()) {
            toWrite.add(new String[] {"Data", "Nome", "Quantità", "PrezzoUnitario"});
        }

        List<GroupedItem.Course> courses = Arrays.asList(
                GroupedItem.Course.BEVERAGE, GroupedItem.Course.FIRST, GroupedItem.Course.SECOND,
                GroupedItem.Course.DESSERT, GroupedItem.Course.COFFEE);

        for (int idx = 0; idx < basketPanel.getArticlesCount(); idx++) {
            JPanelBasketLine line = basketPanel.getArticles(idx);
            boolean isGroup = line.isGrouped();
            int qty = isGroup ? basket.getGroupedItemQty(line.getGroupedItem())
                              : basket.getItemQty(line.getItem());
            if (qty <= 0) continue;

            // "Unito" = un solo scontrino con la quantità; "Separato" = N scontrini da 1
            int copies = line.isUnitPrinting() ? 1 : qty;
            int qtyPerCopy = line.isUnitPrinting() ? qty : 1;

            for (int copy = 0; copy < copies; copy++) {
                if (!isGroup) {
                    Item item = line.getItem();
                    printOnce(item, pf, job, qtyPerCopy);
                    toWrite.add(new String[] {
                            todayStr, item.getText(), String.valueOf(qtyPerCopy),
                            String.format(Locale.ROOT, "%.2f", basket.getEffectivePrice(item))
                    });
                } else {
                    GroupedItem gi = line.getGroupedItem();
                    for (GroupedItem.Course c : courses) {
                        gi.getItem(c).ifPresent(item ->
                            printItem(job, pf, gi.getText(c), String.valueOf(qtyPerCopy), "", c.name().toLowerCase()));
                    }
                    toWrite.add(new String[] {
                            todayStr, gi.getMenu().getText(), String.valueOf(qtyPerCopy),
                            String.format(Locale.ROOT, "%.2f", basket.getEffectivePrice(gi.getMenu()))
                    });
                }
            }
        }

        try {
            new SalesRecorder().append(file, toWrite);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Errore durante il salvataggio del CSV:\n" + e.getMessage(),
                    "Errore CSV", JOptionPane.ERROR_MESSAGE);
        }

        basket.restorePrices();
        basket.clear();
        basketPanel.clear();
    }

    /** Stampa un singolo Item con la quantità indicata. */
    private void printOnce(Item item, PageFormat pf, PrinterJob job, int qty) {
        printItem(job, pf, basket.getName(item), String.valueOf(qty),
                String.format("%.2f", basket.getEffectivePrice(item)), "item");
    }

    /** Stampa una singola voce sullo scontrino (un'unica via di stampa). */
    private void printItem(PrinterJob job, PageFormat pf, String name, String qty, String price, String label) {
        ModelloStampa ms = new ModelloStampa(price, qty, name, new Date());
        Book book = new Book();
        book.append(ms, pf);
        job.setPageable(book);
        try {
            job.print();
        } catch (PrinterException e) {
            JOptionPane.showMessageDialog(this,
                "Errore stampa " + label + ": " + e.getMessage(), "ERRORE", JOptionPane.ERROR_MESSAGE);
        }
    }

    // --- helper conversioni di stampa (usati anche da ModelloStampa) ---
    protected static double fromCMToPPI(double cm) {
        return toPPI(cm * 0.393700787);
    }

    protected static double toPPI(double inch) {
        return inch * 72d;
    }

    // ============================ MAIN ============================

    public static void main(String[] args) {
        FlatLightLaf.setup();
        UIManager.put("Button.arc", 12);
        UIManager.put("Component.arc", 12);
        UIManager.put("TextComponent.arc", 10);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.width", 12);
        UIManager.put("CheckBox.icon.style", "filled");

        java.awt.EventQueue.invokeLater(() -> {
            try {
                new Cassa().setVisible(true);
            } catch (IOException ex) {
                Logger.getLogger(Cassa.class.getName()).log(Level.SEVERE, null, ex);
                JOptionPane.showMessageDialog(null,
                    "Errore avvio cassa: " + ex.getMessage(), "ERRORE", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
