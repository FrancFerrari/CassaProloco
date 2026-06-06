package cassaproloco;

import com.formdev.flatlaf.FlatLightLaf;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.print.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.swing.border.LineBorder;


public class Cassa extends javax.swing.JFrame {
    
    private class BasketActionListener implements java.awt.event.ActionListener {

        @Override
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            addElementToBasket(evt);
        }
    }
    protected static double fromCMToPPI(double cm) {            
        return toPPI(cm * 0.393700787);            
    }

    protected static double toPPI(double inch) {            
        return inch * 72d;            
    }

    protected static String dump(Paper paper) {            
        StringBuilder sb = new StringBuilder(64);
        sb.append(paper.getWidth()).append("x").append(paper.getHeight())
           .append("/").append(paper.getImageableX()).append("x").
           append(paper.getImageableY()).append(" - ").append(paper
       .getImageableWidth()).append("x").append(paper.getImageableHeight());            
        return sb.toString();            
    }

    protected static String dump(PageFormat pf) {    
        Paper paper = pf.getPaper();            
        return dump(paper);    
    }
    
    public static class UIConstants {
        public static final Color PRIMARY = new Color(82, 141, 164);
        public static final Color SECONDARY = new Color(128, 209, 195);
        public static final Color ACCENT = new Color(78, 108, 135);
        public static final Dimension BUTTON_SIZE = new Dimension(200, 50);
        public static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 15);
    }

    private JButton createMenuButton(String text, String actionCommand, ActionListener listener) {
        JButton b = new JButton(text);
        b.setActionCommand(actionCommand);
        b.setUI(new ModernButtonUI(UIConstants.PRIMARY,
                                   UIConstants.SECONDARY,
                                   UIConstants.ACCENT,
                                   Color.WHITE));
        b.setPreferredSize(UIConstants.BUTTON_SIZE);
        b.setFont(UIConstants.BUTTON_FONT);
        b.addActionListener(listener);
        return b;
    }

    private void readItemsFile(File file, ArrayList<Item> listItems, JPanel panel, ActionListener listener, int startIdx) {
        try {
            for (Item item : MenuConfigLoader.load(file)) {
                listItems.add(item);
                JButton b = createMenuButton(item.getText(), String.valueOf(startIdx++), listener);
                panel.add(b);
            }
        } catch (IOException ex) {
            Logger.getLogger(Cassa.class.getName())
                  .log(Level.SEVERE, "Errore lettura file " + file.getName(), ex);
            JOptionPane.showMessageDialog(this,
                "Errore lettura file " + file.getName() + ": " + ex.getMessage(),
                "ERRORE", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private JButton createMenuButton(String text, ActionListener listener) {
        JButton b = new JButton(text);
        b.setUI(new ModernButtonUI(
            UIConstants.PRIMARY,
            UIConstants.SECONDARY,
            UIConstants.ACCENT,
            Color.WHITE));

        b.setPreferredSize(UIConstants.BUTTON_SIZE);
        b.setMaximumSize(UIConstants.BUTTON_SIZE);
        b.setMinimumSize(UIConstants.BUTTON_SIZE);
        b.setFont(adjustFontToFit(b, text));
        b.addActionListener(listener);
        return b;
    }
    
    private Font adjustFontToFit(JButton button, String text) {
        Font baseFont = UIConstants.BUTTON_FONT;
        int availableWidth = UIConstants.BUTTON_SIZE.width - 20; // margine interno
        int fontSize = baseFont.getSize();

        FontMetrics fm = button.getFontMetrics(baseFont);

        // Scala finché il testo non sta dentro il bottone
        while (fm.stringWidth(text) > availableWidth && fontSize > 8) {
            fontSize--;
            baseFont = baseFont.deriveFont((float) fontSize);
            fm = button.getFontMetrics(baseFont);
        }

        return baseFont;
    }

    /* Importa un gruppo e aggiunge il pulsante con menu contestuale */
    public void importMenu(GroupedItem gi) {
        JButton b = createMenuButton(
            gi.getText(GroupedItem.Course.MENU),
            evt -> {
                basketPanel.addGroupedItem(gi);
                System.out.println("GroupedItem aggiunto: " + gi);
            }
        );

        // Aggiungi menu contestuale per rimozione
        JPopupMenu popup = new JPopupMenu();
        JMenuItem removeItem = new JMenuItem("Rimuovi menu");
        removeItem.addActionListener(e -> removeGroupedItem(gi, b));
        popup.add(removeItem);

        b.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) popup.show(b, e.getX(), e.getY());
            }
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) popup.show(b, e.getX(), e.getY());
            }
        });

        primi.add(b);
        primi.revalidate();
        primi.repaint();
    }

    /* Rimuove il menu sia dalla UI che dal file serializzato */
    private void removeGroupedItem(GroupedItem gi, JButton button) {
        // Rimuovi dal pannello
        primi.remove(button);
        primi.revalidate();
        primi.repaint();

        // Rimuovi dalla lista e aggiorna file
        groupedItemList.remove(gi);
        try {
            store.save(groupedItemList);
            System.out.println("GroupedItems aggiornati dopo rimozione.");
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Errore salvataggio dopo rimozione: " + ex.getMessage(),
                "ERRORE", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void loadGroupedItems() {
        groupedItemList = store.load();
        for (GroupedItem gi : groupedItemList) {
            importMenu(gi);
        }
    }

    public List<GroupedItem> getGroupedItemList() {
        return groupedItemList;
    }
    /**
     * Creates new form Cassa
     * @throws java.io.IOException
     */
    public Cassa() throws IOException {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        width = (int) screenSize.getWidth();
        height = (int) screenSize.getHeight();

        //setUndecorated(true);
        setSize(screenSize);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // opzionale: massimizza la finestra
        initComponents();
        Color bordoColor = new Color(58, 48, 66);
        int bordoSpessore = 20;

        SX.setBorder(new LineBorder(bordoColor, bordoSpessore));
        jPanel1.setBorder(new LineBorder(bordoColor, bordoSpessore));
        basket = new Basket();
        basket.setParent(basketPanel);
        basketPanel.setBasket(basket);
        basketPanel.setTotalLabel(lblTotal);
        basketPanel.clear();
        bere.removeAll();

        basketScroll.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(82,141,164);
                this.thumbDarkShadowColor = new Color(82,141,164);
                this.thumbLightShadowColor = new Color(82,141,164);
                this.trackColor = new Color(238, 238, 238);
        }
            @Override
        protected JButton createDecreaseButton(int orientation) {
            return createZeroButton();
        }
            @Override
        protected JButton createIncreaseButton(int orientation) {
            return createZeroButton();
        }
        private JButton createZeroButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(0, 0));
            button.setMinimumSize(new Dimension(0, 0));
            button.setMaximumSize(new Dimension(0, 0));
            return button;
        }
        });
        
        BasketActionListener a = new BasketActionListener();
        
        File Fbere = new File("bere.cfg"); 
        File Fprimi = new File("primi.cfg"); 
        File Fsecondi = new File("secondi.cfg"); 
        try{
            if(!Fbere.exists()) Fbere.createNewFile();
            if(!Fprimi.exists()) Fprimi.createNewFile();
            if(!Fsecondi.exists()) Fsecondi.createNewFile();
        } catch (IOException e) {
                System.out.println("An error occurred.");
                JOptionPane.showMessageDialog(null, "Errore creazione File Bere/Primi/Secondi" + e.getMessage(), "ERRORE", JOptionPane.ERROR_MESSAGE);
            }
        
        itemsPrimi = new ArrayList<>();
        itemsSecondi = new ArrayList<>();
        itemsBere = new ArrayList<>();
        itemListMenuBere = new ArrayList<>();
        itemListMenuPrimi = new ArrayList<>();
        itemListMenuSecondi = new ArrayList<>();
        groupedItemList = new ArrayList<>();

        readItemsFile(Fprimi, itemsPrimi, primi, a, itemsPrimi.size());
        readItemsFile(Fbere, itemsBere, bere, a, 0);
        readItemsFile(Fsecondi, itemsSecondi, secondi, a, itemsSecondi.size());
        
        setBoxTextPrimi(itemsPrimi);
        setBoxTextSecondi(itemsSecondi);
        setBoxTextBere(itemsBere);
        
        bere.setVisible(false);
        primi.setVisible(false);
        secondi.setVisible(false);
        setMenu.setVisible(false);
        
        boxPrimi.setEnabled(false);
        boxSecondi.setEnabled(false);
        loadGroupedItems();
        
    }
    
    private void setBoxTextBere(ArrayList<Item> listItems) {
        boxBere.removeAllItems();
        itemListMenuBere.clear();
        for (Item item : listItems) {
            boxBere.addItem(item.getText());
            itemListMenuBere.add(item);
        }
    }
    
    private void setBoxTextPrimi(ArrayList<Item> listItems) {
        boxPrimi.removeAllItems();
        itemListMenuPrimi.clear();

        for (Item item : listItems) {
            boxPrimi.addItem(item.getText());
            itemListMenuPrimi.add(item);
        }
    }
    
    private void setBoxTextSecondi(ArrayList<Item> listItems) {
        boxSecondi.removeAllItems();
        itemListMenuSecondi.clear();

        for (Item item : listItems) {
            boxSecondi.addItem(item.getText());
            itemListMenuSecondi.add(item);
        }
    }
    
    private void printItem(PrinterJob job, PageFormat pf, String name, String qty, String price, String label) {
        Book book = new Book();
        ModelloStampa ms = new ModelloStampa(price, qty, name, new Date());
        book.append(ms, pf);
        job.setPageable(book);
        try {
            job.print();
        } catch (PrinterException e) {
            JOptionPane.showMessageDialog(this,
                "Errore stampa " + label + ": " + e.getMessage(),
                "ERRORE", JOptionPane.ERROR_MESSAGE);
        }
    }
    @SuppressWarnings("unchecked")

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        Contenitore = new javax.swing.JPanel();
        SX = new javax.swing.JPanel();
        selezione = new javax.swing.JPanel();
        primi = new javax.swing.JPanel();
        setMenu = new javax.swing.JPanel();
        boxPrimi = new javax.swing.JComboBox<>();
        boxSecondi = new javax.swing.JComboBox<>();
        boxBere = new javax.swing.JComboBox<>();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        confermaBtn = new javax.swing.JButton();
        prezzoLabel = new javax.swing.JTextField();
        menuLabel1 = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        AbilitaPrimi = new javax.swing.JCheckBox();
        AbilitaSecondi = new javax.swing.JCheckBox();
        jLabel7 = new javax.swing.JLabel();
        AbilitaCaffe = new javax.swing.JCheckBox();
        AbilitaDolce = new javax.swing.JCheckBox();
        jLabel8 = new javax.swing.JLabel();
        bere = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        secondi = new javax.swing.JPanel();
        Blaterale = new javax.swing.JPanel();
        Secondi = new javax.swing.JButton();
        Bere = new javax.swing.JButton();
        Primi = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        ToolBar = new javax.swing.JPanel();
        resocontoBtn = new javax.swing.JButton();
        OmaggioBtn = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        basketScroll = new javax.swing.JScrollPane();
        basketPanel = new cassaproloco.JPanelBasket();
        jPanel2 = new javax.swing.JPanel();
        btnPrint = new javax.swing.JButton();
        lblTotal = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(58, 48, 66));
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setForeground(java.awt.Color.darkGray);

        Contenitore.setPreferredSize(new Dimension((int)(width), (int)(height)));
        Contenitore.setOpaque(true);
        Contenitore.setBackground(new java.awt.Color(58, 48, 66));
        Contenitore.setForeground(new java.awt.Color(58, 48, 66));
        Contenitore.setToolTipText("");
        Contenitore.setLayout(new java.awt.BorderLayout());

        SX.setPreferredSize(new Dimension((int)(width*0.4), (int)(height)));
        SX.setBackground(new java.awt.Color(58, 48, 66));
        SX.setLayout(new java.awt.BorderLayout());

        selezione.setBackground(new java.awt.Color(58, 48, 66));
        selezione.setPreferredSize(new java.awt.Dimension(100, 100));
        selezione.setLayout(new javax.swing.OverlayLayout(selezione));

        primi.setOpaque(true);
        primi.setBackground(new java.awt.Color(58, 48, 66));
        primi.setMinimumSize(new java.awt.Dimension(700, 661));
        primi.setOpaque(false);
        primi.setPreferredSize(new java.awt.Dimension(700, 661));
        primi.setLayout(new java.awt.GridLayout(4, 5, 4, 4));
        selezione.add(primi);

        setMenu.setOpaque(true);
        setMenu.setBackground(new java.awt.Color(58, 48, 66));
        setMenu.setMinimumSize(new java.awt.Dimension(500, 661));
        setMenu.setOpaque(false);
        setMenu.setPreferredSize(new java.awt.Dimension(500, 661));

        boxPrimi.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        boxPrimi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                boxPrimiActionPerformed(evt);
            }
        });

        boxSecondi.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        boxSecondi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                boxSecondiActionPerformed(evt);
            }
        });

        boxBere.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        boxBere.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                boxBereActionPerformed(evt);
            }
        });

        jLabel1.setBackground(new java.awt.Color(255, 255, 255));
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("PRIMI");
        jLabel1.setForeground(new java.awt.Color(221, 221, 221));

        jLabel2.setBackground(new java.awt.Color(255, 255, 255));
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("SECONDI");
        jLabel2.setForeground(new java.awt.Color(221, 221, 221));

        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("BERE");
        jLabel3.setForeground(new java.awt.Color(221, 221, 221));

        confermaBtn.setText("Conferma");
        confermaBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                confermaBtnActionPerformed(evt);
            }
        });

        prezzoLabel.setText("Inserire col punto");
        prezzoLabel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                prezzoLabelActionPerformed(evt);
            }
        });

        menuLabel1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuLabel1ActionPerformed(evt);
            }
        });

        jLabel6.setBackground(new java.awt.Color(255, 255, 255));
        jLabel6.setForeground(new java.awt.Color(255, 255, 255));
        jLabel6.setText("PREZZO");
        jLabel3.setForeground(new java.awt.Color(221, 221, 221));

        jLabel4.setBackground(new java.awt.Color(255, 255, 255));
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setText("NOME ");
        jLabel3.setForeground(new java.awt.Color(221, 221, 221));

        AbilitaPrimi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AbilitaPrimiActionPerformed(evt);
            }
        });

        AbilitaSecondi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AbilitaSecondiActionPerformed(evt);
            }
        });

        jLabel7.setForeground(new java.awt.Color(255, 255, 255));
        jLabel7.setText("CAFFE'");
        jLabel3.setForeground(new java.awt.Color(221, 221, 221));

        AbilitaCaffe.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AbilitaCaffeActionPerformed(evt);
            }
        });

        AbilitaDolce.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AbilitaDolceActionPerformed(evt);
            }
        });

        jLabel8.setForeground(new java.awt.Color(255, 255, 255));
        jLabel8.setText("DOLCE");
        jLabel3.setForeground(new java.awt.Color(221, 221, 221));

        javax.swing.GroupLayout setMenuLayout = new javax.swing.GroupLayout(setMenu);
        setMenu.setLayout(setMenuLayout);
        setMenuLayout.setHorizontalGroup(
            setMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(setMenuLayout.createSequentialGroup()
                .addGap(45, 45, 45)
                .addGroup(setMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addGroup(setMenuLayout.createSequentialGroup()
                        .addGroup(setMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(setMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addGroup(setMenuLayout.createSequentialGroup()
                                    .addComponent(jLabel6)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(prezzoLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 153, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(setMenuLayout.createSequentialGroup()
                                    .addGroup(setMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(setMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(setMenuLayout.createSequentialGroup()
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                            .addGroup(setMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(boxPrimi, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(boxSecondi, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                        .addGroup(setMenuLayout.createSequentialGroup()
                                            .addGap(13, 13, 13)
                                            .addComponent(boxBere, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                .addGroup(setMenuLayout.createSequentialGroup()
                                    .addComponent(jLabel4)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(menuLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 153, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(jLabel7)
                            .addComponent(jLabel8))
                        .addGap(30, 30, 30)
                        .addGroup(setMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(AbilitaCaffe, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(AbilitaPrimi, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(AbilitaSecondi, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(confermaBtn, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(AbilitaDolce, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        setMenuLayout.setVerticalGroup(
            setMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(setMenuLayout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addGroup(setMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(AbilitaPrimi, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(setMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(boxPrimi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel1)))
                .addGap(26, 26, 26)
                .addGroup(setMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(AbilitaSecondi, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(setMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(boxSecondi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel2)))
                .addGap(26, 26, 26)
                .addGroup(setMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(boxBere, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addGap(18, 18, 18)
                .addGroup(setMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel7)
                    .addComponent(AbilitaCaffe))
                .addGap(18, 18, 18)
                .addGroup(setMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(AbilitaDolce)
                    .addComponent(jLabel8))
                .addGap(19, 19, 19)
                .addGroup(setMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(prezzoLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(28, 28, 28)
                .addGroup(setMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(confermaBtn)
                    .addComponent(menuLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addContainerGap(196, Short.MAX_VALUE))
        );

        selezione.add(setMenu);

        bere.setOpaque(true);
        bere.setBackground(new java.awt.Color(58, 48, 66));
        bere.setMinimumSize(new java.awt.Dimension(500, 661));
        bere.setOpaque(false);
        bere.setPreferredSize(new java.awt.Dimension(500, 661));
        bere.setLayout(new java.awt.GridLayout(5, 5, 4, 4));

        jButton1.setUI(new ModernButtonUI(Color.getHSBColor(Color.RGBtoHSB(255,120,79, null)[0],Color.RGBtoHSB(255,120,79, null)[1],Color.RGBtoHSB(255,120,79, null)[2]),Color.getHSBColor(Color.RGBtoHSB(255,225,156, null)[0],Color.RGBtoHSB(255,225,156, null)[1],Color.RGBtoHSB(255,225,156, null)[2]),Color.getHSBColor(Color.RGBtoHSB(219,157,71, null)[0],Color.RGBtoHSB(219,157,71, null)[1],Color.RGBtoHSB(219,157,71, null)[2]), Color.WHITE));
        jButton1.setLabel("Acqua");
        jButton1.setMaximumSize(new java.awt.Dimension(52, 22));
        jButton1.setMinimumSize(new java.awt.Dimension(52, 22));
        jButton1.setOpaque(true);
        jButton1.setVerifyInputWhenFocusTarget(false);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addElementToBasket(evt);
            }
        });
        bere.add(jButton1);

        jButton2.setUI(new ModernButtonUI(Color.getHSBColor(Color.RGBtoHSB(255,120,79, null)[0],Color.RGBtoHSB(255,120,79, null)[1],Color.RGBtoHSB(255,120,79, null)[2]),Color.getHSBColor(Color.RGBtoHSB(255,225,156, null)[0],Color.RGBtoHSB(255,225,156, null)[1],Color.RGBtoHSB(255,225,156, null)[2]),Color.getHSBColor(Color.RGBtoHSB(219,157,71, null)[0],Color.RGBtoHSB(219,157,71, null)[1],Color.RGBtoHSB(219,157,71, null)[2]), Color.WHITE));
        jButton2.setLabel("Acqua");
        jButton2.setOpaque(true);
        jButton2.setVerifyInputWhenFocusTarget(false);
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2addElementToBasket(evt);
            }
        });
        bere.add(jButton2);

        selezione.add(bere);

        secondi.setOpaque(true);
        secondi.setBackground(new java.awt.Color(58, 48, 66));
        secondi.setMinimumSize(new java.awt.Dimension(700, 661));
        secondi.setOpaque(false);
        secondi.setPreferredSize(new java.awt.Dimension(700, 661));
        secondi.setLayout(new java.awt.GridLayout(5, 5, 4, 4));
        selezione.add(secondi);

        SX.add(selezione, java.awt.BorderLayout.CENTER);

        Blaterale.setPreferredSize(new Dimension(width/2, height/6)); // 20% della larghezza
        Blaterale.setBackground(new java.awt.Color(58, 48, 66));
        Blaterale.setLayout(new java.awt.GridLayout(1, 0, 2, 0));

        Secondi.setUI(new ModernButtonUI(Color.getHSBColor(Color.RGBtoHSB(228,136,106, null)[0],Color.RGBtoHSB(228,136,106, null)[1],Color.RGBtoHSB(228,136,106, null)[2]),Color.getHSBColor(Color.RGBtoHSB(255,225,156, null)[0],Color.RGBtoHSB(255,225,156, null)[1],Color.RGBtoHSB(255,225,156, null)[2]),Color.getHSBColor(Color.RGBtoHSB(219,157,71, null)[0],Color.RGBtoHSB(219,157,71, null)[1],Color.RGBtoHSB(219,157,71, null)[2]), Color.WHITE));
        bottnSize = this.getSize();
        int fontSize = (int)(bottnSize.height * 0.03);
        Secondi.setFont(new java.awt.Font("Segoe UI", 1, fontSize));
        Secondi.setPreferredSize(new Dimension(width/4, height/10));
        Secondi.setText("PRIMI");
        Secondi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SecondiActionPerformed(evt);
            }
        });
        Blaterale.add(Secondi);

        Bere.setPreferredSize(new Dimension(width/4, height/10));
        Bere.setUI(new ModernButtonUI(Color.getHSBColor(Color.RGBtoHSB(228,136,106, null)[0],Color.RGBtoHSB(228,136,106, null)[1],Color.RGBtoHSB(228,136,106, null)[2]),Color.getHSBColor(Color.RGBtoHSB(255,225,156, null)[0],Color.RGBtoHSB(255,225,156, null)[1],Color.RGBtoHSB(255,225,156, null)[2]),Color.getHSBColor(Color.RGBtoHSB(219,157,71, null)[0],Color.RGBtoHSB(219,157,71, null)[1],Color.RGBtoHSB(219,157,71, null)[2]), Color.WHITE));
        Bere.setFont(new java.awt.Font("Segoe UI", 1, fontSize));
        Bere.setLabel("BERE");
        Bere.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BereActionPerformed(evt);
            }
        });
        Blaterale.add(Bere);

        Primi.setFont(new java.awt.Font("Segoe UI", 1, fontSize));
        Primi.setUI(new ModernButtonUI(Color.getHSBColor(Color.RGBtoHSB(228,136,106, null)[0],Color.RGBtoHSB(228,136,106, null)[1],Color.RGBtoHSB(228,136,106, null)[2]),Color.getHSBColor(Color.RGBtoHSB(255,225,156, null)[0],Color.RGBtoHSB(255,225,156, null)[1],Color.RGBtoHSB(255,225,156, null)[2]),Color.getHSBColor(Color.RGBtoHSB(219,157,71, null)[0],Color.RGBtoHSB(219,157,71, null)[1],Color.RGBtoHSB(219,157,71, null)[2]), Color.WHITE));
        Primi.setText("SECONDI");
        Primi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PrimiActionPerformed(evt);
            }
        });
        Blaterale.add(Primi);

        SX.add(Blaterale, java.awt.BorderLayout.PAGE_START);

        Contenitore.add(SX, java.awt.BorderLayout.LINE_START);

        jPanel1.setOpaque(true);
        jPanel1.setPreferredSize(new Dimension((int)(width*0.6), (int)(height)));
        jPanel1.setBackground(new java.awt.Color(58, 48, 66));
        jPanel1.setToolTipText("");
        jPanel1.setLayout(new java.awt.BorderLayout(0, 10));

        ToolBar.setBackground(new java.awt.Color(58, 48, 66));
        ToolBar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                ToolBarMouseClicked(evt);
            }
        });

        OmaggioBtn.setUI(new ModernButtonUI(
            new Color(70, 130, 180),   // colore base
            new Color(100, 160, 210),  // hover
            new Color(40, 90, 140),    // click
            Color.WHITE                // colore testo
        ));

        toolbarSize = this.getSize();
        int toolbarFontSize = (int)(toolbarSize.height * 0.02);

        OmaggioBtn.setFont(new Font("Helvetica", Font.BOLD, toolbarFontSize));
        OmaggioBtn.setFocusPainted(false);
        OmaggioBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        OmaggioBtn.setPreferredSize(new Dimension(120, 40));
        resocontoBtn.setText("RESOCONTO");
        resocontoBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resocontoBtnActionPerformed(evt);
            }
        });
        ToolBar.add(resocontoBtn);

        OmaggioBtn.setUI(new ModernButtonUI(
            new Color(70, 130, 180),   // colore base
            new Color(100, 160, 210),  // hover
            new Color(40, 90, 140),    // click
            Color.WHITE                // colore testo
        ));

        OmaggioBtn.setFont(new Font("Helvetica", Font.BOLD, toolbarFontSize));
        OmaggioBtn.setFocusPainted(false);
        OmaggioBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        OmaggioBtn.setPreferredSize(new Dimension(120, 40));
        OmaggioBtn.setText("OMAGGIO");
        OmaggioBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OmaggioBtnActionPerformed(evt);
            }
        });
        ToolBar.add(OmaggioBtn);

        jButton4.setUI(new ModernButtonUI(
            new Color(90, 150, 90),    // colore base verde
            new Color(120, 180, 120),  // hover
            new Color(60, 120, 60),    // click
            Color.WHITE                // testo bianco
        ));
        jButton4.setFont(new Font("Helvetica", Font.BOLD, toolbarFontSize));
        jButton4.setFocusPainted(false);
        jButton4.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        jButton4.setPreferredSize(new Dimension(120, 40));
        jButton4.setText("MENU'");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });
        ToolBar.add(jButton4);

        jPanel1.add(ToolBar, java.awt.BorderLayout.PAGE_START);

        basketScroll.setBackground(new java.awt.Color(220, 234, 244));
        basketScroll.setForeground(new java.awt.Color(220, 234, 244));
        basketScroll.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        basketScroll.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        basketScroll.setPreferredSize(new java.awt.Dimension(260, 300));

        btnPrint.setPreferredSize(new Dimension(width/2, height/10));
        basketPanel.setBackground(new java.awt.Color(220, 234, 244));
        basketPanel.setLayout(new cassaproloco.VerticalFlowLayout());
        basketScroll.setViewportView(basketPanel);
        //basketPanel.setPreferredSize(new java.awt.Dimension(40, 500));

        jPanel1.add(basketScroll, java.awt.BorderLayout.CENTER);
        DragScrollListener dl = new DragScrollListener(basketPanel);
        basketScroll.addMouseListener(dl);
        basketScroll.addMouseMotionListener(dl);
        basketScroll.setBorder(null);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBorder(new EmptyBorder(20, 0, 0, 0));  // margine superiore 20 px
        wrapper.add(basketPanel, BorderLayout.CENTER);
        wrapper.setBackground(new java.awt.Color(220, 234, 244));

        basketScroll.setViewportView(wrapper);

        jPanel2.setPreferredSize(new Dimension(width/2, height/10));
        jPanel2.setBackground(new java.awt.Color(58, 48, 66));
        jPanel2.setToolTipText("");
        jPanel2.setLayout(new java.awt.GridLayout(1, 0));

        btnPrint.setUI(new ModernButtonUI(Color.getHSBColor(Color.RGBtoHSB(228,136,106, null)[0],Color.RGBtoHSB(228,136,106, null)[1],Color.RGBtoHSB(228,136,106, null)[2]),Color.getHSBColor(Color.RGBtoHSB(255,225,156, null)[0],Color.RGBtoHSB(255,225,156, null)[1],Color.RGBtoHSB(255,225,156, null)[2]),Color.getHSBColor(Color.RGBtoHSB(219,157,71, null)[0],Color.RGBtoHSB(219,157,71, null)[1],Color.RGBtoHSB(219,157,71, null)[2]), Color.WHITE));
        btnPrint.setFont(new java.awt.Font("Segoe UI", 1, 25)); // NOI18N
        btnPrint.setText("STAMPA");
        btnPrint.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btnPrint.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnPrint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPrintActionPerformed(evt);
            }
        });
        jPanel2.add(btnPrint);

        lblTotal.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblTotal.setText("lblTotal");
        jPanel2.add(lblTotal);

        jPanel1.add(jPanel2, java.awt.BorderLayout.PAGE_END);

        Contenitore.add(jPanel1, java.awt.BorderLayout.LINE_END);

        getContentPane().add(Contenitore, java.awt.BorderLayout.CENTER);

        setBounds(0, 0, 872, 539);
    }// </editor-fold>//GEN-END:initComponents

    private void BereActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BereActionPerformed
            control = 0;
            bere.setVisible(true);
            primi.setVisible(false);
            secondi.setVisible(false);
            setMenu.setVisible(false);
    }//GEN-LAST:event_BereActionPerformed

    private void SecondiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SecondiActionPerformed
            control = 1;
            bere.setVisible(false);
            primi.setVisible(true);
            secondi.setVisible(false); 
            setMenu.setVisible(false);
    }//GEN-LAST:event_SecondiActionPerformed

    private void PrimiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PrimiActionPerformed
            control = 2;
            bere.setVisible(false);
            primi.setVisible(false);
            secondi.setVisible(true); 
            setMenu.setVisible(false);
    }//GEN-LAST:event_PrimiActionPerformed

    private void addElementToBasket(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addElementToBasket
        if (!(evt.getSource() instanceof javax.swing.JButton)) {
            return;
        }
        int idx = Integer.parseInt(evt.getActionCommand());
        if (control == 0)
        basketPanel.addItem(itemsBere.get(idx));
        else if (control == 1)
            basketPanel.addItem(itemsPrimi.get(idx));
        else if (control == 2)
            basketPanel.addItem(itemsSecondi.get(idx));

    }//GEN-LAST:event_addElementToBasket

    private void jButton2addElementToBasket(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2addElementToBasket
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton2addElementToBasket

    private void ToolBarMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_ToolBarMouseClicked

    }//GEN-LAST:event_ToolBarMouseClicked

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
            bere.setVisible(false);
            primi.setVisible(false);
            secondi.setVisible(false);
            setMenu.setVisible(true);
    }//GEN-LAST:event_jButton4ActionPerformed

    private void confermaBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_confermaBtnActionPerformed
    // 1) Parsing prezzo
    float prezzo;
    try {
        prezzo = Float.parseFloat(prezzoLabel.getText());
    } catch (NumberFormatException ex) {
        JOptionPane.showMessageDialog(this,
            "Il prezzo deve essere un numero.", "Errore", JOptionPane.ERROR_MESSAGE);
        return;
    }

    // 2) Costruzione del GroupedItem
    Item menu = new Item(1, prezzo, menuLabel1.getText(), menuLabel1.getText(), 1);
    GroupedItem.Builder builder = new GroupedItem.Builder()
        .withMenu(menu);

    if (boxBere.getSelectedIndex() != -1) {
        builder.withBeverage(itemListMenuBere.get(boxBere.getSelectedIndex()));
    }
    if (boxPrimi.getSelectedIndex() != -1) {
        builder.withFirst(itemListMenuPrimi.get(boxPrimi.getSelectedIndex()));
    }
    if (boxSecondi.getSelectedIndex() != -1) {
        builder.withSecond(itemListMenuSecondi.get(boxSecondi.getSelectedIndex()));
    }

    //  Aggiungo Dolce se abilitato
    if (AbilitaDolce.isSelected()) {
        // creo un Item con id “‐1” (o altro id “speciale”), prezzo 0, testo “Dolce”
        Item dolce = new Item(
            -1,          // id “fake” per il dolce
            0f,          // prezzo nullo
            "Dolce",     // textToPrint
            "Dolce",     // textLong o simile
            1            // quantità iniziale
        );
        builder.withDessert(dolce);
    }

    //  Aggiungo Caffè se abilitato
    if (AbilitaCaffe.isSelected()) {
        Item caffe = new Item(
            -2,           // id “fake” per il caffè
            0f,           // prezzo nullo
            "Caffè",
            "Caffè",
            1
        );
        builder.withCoffee(caffe);
    }
    // Costruisco l’oggetto finale
    GroupedItem newItem;
    try {
        newItem = builder.build();
    } catch (IllegalStateException ex) {
        JOptionPane.showMessageDialog(this,
            "Devi selezionare almeno il menu principale.", "Errore", JOptionPane.ERROR_MESSAGE);
        return;
    }

    // 3) Aggiungi il nuovo menu alla lista e salva
    groupedItemList.add(newItem);
    try {
        store.save(groupedItemList);
        System.out.println("GroupedItems salvati correttamente.");
    } catch (IOException ex) {
        ex.printStackTrace();
    }

    // 4) Aggiorna UI
    importMenu(newItem);
    JOptionPane.showMessageDialog(this,
        "MENU' CREATO", "CONFERMA", JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_confermaBtnActionPerformed

    private void prezzoLabelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_prezzoLabelActionPerformed
        
    }//GEN-LAST:event_prezzoLabelActionPerformed

    private void menuLabel1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuLabel1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_menuLabel1ActionPerformed

    private void AbilitaPrimiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AbilitaPrimiActionPerformed
        if (AbilitaPrimi.isSelected()) {
            boxPrimi.setEnabled(true);
        } else{
            boxPrimi.setEnabled(false);
            boxPrimi.setSelectedItem(null);
        }
    }//GEN-LAST:event_AbilitaPrimiActionPerformed

    private void AbilitaSecondiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AbilitaSecondiActionPerformed
        if (AbilitaSecondi.isSelected()) {
            boxSecondi.setEnabled(true);
        } else{
            boxSecondi.setEnabled(false);
            boxSecondi.setSelectedItem(null);
        }
        
    }//GEN-LAST:event_AbilitaSecondiActionPerformed

    private void boxPrimiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_boxPrimiActionPerformed
        if (AbilitaPrimi.isSelected()) {
            boxPrimi.setEnabled(true);
        } else{
            boxPrimi.setEnabled(false);
            boxPrimi.setSelectedItem(null);
        }
    }//GEN-LAST:event_boxPrimiActionPerformed

    private void boxBereActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_boxBereActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_boxBereActionPerformed

    private void boxSecondiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_boxSecondiActionPerformed
        if (AbilitaSecondi.isSelected()) {
            boxSecondi.setEnabled(true);
        } else{
            boxSecondi.setEnabled(false);
            boxSecondi.setSelectedItem(null);
        }
        
    }//GEN-LAST:event_boxSecondiActionPerformed

    private void OmaggioBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_OmaggioBtnActionPerformed
       basket.setPricesToZero();
    }//GEN-LAST:event_OmaggioBtnActionPerformed

    private void AbilitaCaffeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AbilitaCaffeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_AbilitaCaffeActionPerformed

    private void btnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrintActionPerformed

    // Setup stampa
    PrinterJob job = PrinterJob.getPrinterJob();
    PageFormat pf = job.defaultPage();
    Paper paper = pf.getPaper();
    double width = fromCMToPPI(6.2), height = fromCMToPPI(4);
    paper.setSize(width, height);
    paper.setImageableArea(fromCMToPPI(0.25), fromCMToPPI(0),
            width, height - fromCMToPPI(1));
    pf.setOrientation(PageFormat.PORTRAIT);
    pf.setPaper(paper);

    // Prepara CSV
    LocalDate today = LocalDate.now();
    String todayStr = today.format(DateTimeFormatter.ISO_DATE); // "2025-06-28"
    File file = new File("report_" + todayStr + ".csv");


    boolean fileExists = file.exists();
    ArrayList<String[]> toWrite = new ArrayList<>();
    if (!fileExists) {
        toWrite.add(new String[] {"Data", "Nome", "Quantità", "PrezzoUnitario"});
    }

    // Ciclo prodotti
    int count = basketPanel.getArticlesCount();
    for (int idx = 0; idx < count; idx++) {
        JPanelBasketLine line = basketPanel.getArticles(idx);
        boolean isGroup = line.isGrouped();
        int qty = isGroup
                ? basket.getGroupedItemQty(line.getGroupedItem())
                : basket.getItemQty(line.getItem());

        if (qty <= 0) continue;

        if (line.isUnitPrinting()) {
            if (!isGroup) {
                Item item = line.getItem();
                printOnce(item, pf, job, qty);
                toWrite.add(new String[] {
                        todayStr,
                        item.getText(),
                        String.valueOf(qty),
                        String.format(Locale.ROOT, "%.2f", basket.getEffectivePrice(item))
                });
            } else {
                    GroupedItem gi = line.getGroupedItem();

                    // Stampa le singole portate
                    for (GroupedItem.Course c : Arrays.asList(
                            GroupedItem.Course.BEVERAGE,
                            GroupedItem.Course.FIRST,
                            GroupedItem.Course.SECOND,
                            GroupedItem.Course.DESSERT,
                            GroupedItem.Course.COFFEE)) {
                                gi.getItem(c).ifPresent(item ->
                                    printItem(job, pf, gi.getText(c), String.valueOf(qty), "", c.name().toLowerCase())
                                );
                    }

                    // CSV: salva solo il menu principale (nome + prezzo)
                    toWrite.add(new String[] {
                            todayStr,
                            gi.getMenu().getText(),
                            String.valueOf(qty),
                            String.format(Locale.ROOT, "%.2f", basket.getEffectivePrice(gi.getMenu()))
                    });
            }
        } else {
            for (int i = 0; i < qty; i++) {
                if (!isGroup) {
                    Item item = line.getItem();
                    printOnce(item, pf, job, 1);
                    toWrite.add(new String[] {
                            todayStr,
                            item.getText(),
                            "1",
                            String.format(Locale.ROOT, "%.2f", basket.getEffectivePrice(item))
                    });
                } else {
                        GroupedItem gi = line.getGroupedItem();

                        // STAMPA → mantieni la stampa delle singole portate
                        for (GroupedItem.Course c : Arrays.asList(
                            GroupedItem.Course.BEVERAGE,
                            GroupedItem.Course.FIRST,
                            GroupedItem.Course.SECOND,
                            GroupedItem.Course.DESSERT,
                            GroupedItem.Course.COFFEE
                        )) {
                                gi.getItem(c).ifPresent(item ->
                                    printItem(job, pf, gi.getText(c), "1", "", c.name().toLowerCase())
                                );
                        }

                        // CSV → salva solo il menu principale
                        toWrite.add(new String[] {
                            todayStr,
                            gi.getMenu().getText(),                     // nome menu
                            String.valueOf(1),
                            String.format(Locale.ROOT, "%.2f", basket.getEffectivePrice(gi.getMenu()))  // prezzo menu
                        });

                }
            }
        }
    }

    // Salva su CSV in append mode
    try {
        new SalesRecorder().append(file, toWrite);
    } catch (IOException e) {
        JOptionPane.showMessageDialog(this,
                "Errore durante il salvataggio del CSV:\n" + e.getMessage(),
                "Errore CSV", JOptionPane.ERROR_MESSAGE);
    }

    // Pulisce
    basket.restorePrices();
    basket.clear();
    basketPanel.clear();
    }//GEN-LAST:event_btnPrintActionPerformed

    private void AbilitaDolceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AbilitaDolceActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_AbilitaDolceActionPerformed

    private void resocontoBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resocontoBtnActionPerformed
    File directoryCSV = new File("."); // cartella corrente
    JPanel pannello = new PannelloResocontoVendite(directoryCSV);

    JFrame frame = new JFrame("Resoconto Vendite");
    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    frame.setContentPane(pannello);
    frame.setSize(500, 400);
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);        
    }//GEN-LAST:event_resocontoBtnActionPerformed

    /** Stampa un singolo Item con la quantità specificata */
    private void printOnce(Item item, PageFormat pf, PrinterJob job, int qty) {
        String qtyStr   = String.valueOf(qty);
        String priceStr = String.format("%.2f", basket.getEffectivePrice(item));
        String name     = basket.getName(item);
        ModelloStampa ms = new ModelloStampa(priceStr, qtyStr, name, new Date());
        Book book = new Book();
        book.append(ms, pf);
        job.setPageable(book);
        try {
            job.print();
        } catch (PrinterException e) {
            JOptionPane.showMessageDialog(this,
                "Errore stampa item: " + e.getMessage(), "ERRORE", JOptionPane.ERROR_MESSAGE);
        }
    }


    /**
     * @param args the command line arguments
     */

    public static void main(String args[]) {
        /* Look & Feel moderno (FlatLaf) con angoli arrotondati */
        FlatLightLaf.setup();
        UIManager.put("Button.arc", 12);
        UIManager.put("Component.arc", 12);
        UIManager.put("TextComponent.arc", 10);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.width", 12);
        UIManager.put("CheckBox.icon.style", "filled");

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            try {
                Cassa cassa = new Cassa();
                cassa.setVisible(true);
            } catch (IOException ex) {
                Logger.getLogger(Cassa.class.getName()).log(Level.SEVERE, null, ex);
                 JOptionPane.showMessageDialog(null, "Errore avvio cassa" + ex.getMessage(), "ERRORE", JOptionPane.ERROR_MESSAGE);

            }
        });
        
    }

  
    private Basket basket;
    private final ArrayList<Item> itemsPrimi;
    private final ArrayList<Item> itemsSecondi;
    private final ArrayList<Item> itemsBere;
    private int control;
    private ArrayList<Item> itemListMenuBere;
    private ArrayList<Item> itemListMenuPrimi;
    private ArrayList<Item> itemListMenuSecondi;
    private List<GroupedItem> groupedItemList;
    private static final String GROUPED_ITEMS_JSON = "groupedItems.json";
    private static final String GROUPED_ITEMS_LEGACY = "groupedItems.ser";
    private final GroupedItemStore store =
            new GroupedItemStore(new File(GROUPED_ITEMS_JSON), new File(GROUPED_ITEMS_LEGACY));
    private int width;
    private int height;
    private Dimension bottnSize;
    private Dimension toolbarSize;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox AbilitaCaffe;
    private javax.swing.JCheckBox AbilitaDolce;
    private javax.swing.JCheckBox AbilitaPrimi;
    private javax.swing.JCheckBox AbilitaSecondi;
    private javax.swing.JButton Bere;
    private javax.swing.JPanel Blaterale;
    private javax.swing.JPanel Contenitore;
    private javax.swing.JButton OmaggioBtn;
    private javax.swing.JButton Primi;
    private javax.swing.JPanel SX;
    private javax.swing.JButton Secondi;
    private javax.swing.JPanel ToolBar;
    public static cassaproloco.JPanelBasket basketPanel;
    private javax.swing.JScrollPane basketScroll;
    private javax.swing.JPanel bere;
    private javax.swing.JComboBox<String> boxBere;
    private javax.swing.JComboBox<String> boxPrimi;
    private javax.swing.JComboBox<String> boxSecondi;
    private javax.swing.JButton btnPrint;
    private javax.swing.JButton confermaBtn;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel lblTotal;
    private javax.swing.JTextField menuLabel1;
    private javax.swing.JTextField prezzoLabel;
    public static javax.swing.JPanel primi;
    private javax.swing.JButton resocontoBtn;
    private javax.swing.JPanel secondi;
    private javax.swing.JPanel selezione;
    private javax.swing.JPanel setMenu;
    // End of variables declaration//GEN-END:variables
}
