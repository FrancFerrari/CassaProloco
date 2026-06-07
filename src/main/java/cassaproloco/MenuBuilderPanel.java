package cassaproloco;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Collections;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Pannello per comporre un "menu combinato" ({@link GroupedItem}): si sceglie un
 * primo/secondo/bevanda (opzionali), si attivano dolce/caffè, si dà un nome e un
 * prezzo, e alla conferma viene notificato il {@link Listener}.
 *
 * <p>Sostituisce il vecchio pannello {@code setMenu} generato da NetBeans
 * (GroupLayout) con un layout scritto a mano e leggibile.
 */
public class MenuBuilderPanel extends JPanel {

    /** Notificato quando l'utente crea un nuovo menu combinato. */
    public interface Listener {
        void onMenuCreated(GroupedItem menu);
    }

    private final JComboBox<String> boxPrimi = new JComboBox<>();
    private final JComboBox<String> boxSecondi = new JComboBox<>();
    private final JComboBox<String> boxBere = new JComboBox<>();
    private final JCheckBox abilitaPrimi = new JCheckBox();
    private final JCheckBox abilitaSecondi = new JCheckBox();
    private final JCheckBox abilitaCaffe = new JCheckBox("Caffè");
    private final JCheckBox abilitaDolce = new JCheckBox("Dolce");
    private final JTextField nomeField = new JTextField();
    private final JTextField prezzoField = new JTextField("Inserire col punto");
    private final JButton confermaBtn = new JButton("Conferma");

    private List<Item> bereItems = Collections.emptyList();
    private List<Item> primiItems = Collections.emptyList();
    private List<Item> secondiItems = Collections.emptyList();
    private Listener listener;

    public MenuBuilderPanel() {
        setBackground(Theme.BACKGROUND);
        buildLayout();
        wireEvents();
    }

    /** Imposta le voci selezionabili nei menu a tendina. */
    public void setItems(List<Item> bere, List<Item> primi, List<Item> secondi) {
        this.bereItems = bere;
        this.primiItems = primi;
        this.secondiItems = secondi;
        fillCombo(boxBere, bere);
        fillCombo(boxPrimi, primi);
        fillCombo(boxSecondi, secondi);
        boxPrimi.setEnabled(false);
        boxSecondi.setEnabled(false);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    private static void fillCombo(JComboBox<String> combo, List<Item> items) {
        combo.removeAllItems();
        for (Item it : items) {
            combo.addItem(it.getText());
        }
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(Theme.TEXT_LIGHT);
        return l;
    }

    private JCheckBox styled(JCheckBox cb) {
        cb.setOpaque(false);
        cb.setForeground(Theme.TEXT_LIGHT);
        return cb;
    }

    private void buildLayout() {
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        addRow(c, row++, label("PRIMI"), boxPrimi, styled(abilitaPrimi));
        addRow(c, row++, label("SECONDI"), boxSecondi, styled(abilitaSecondi));
        addRow(c, row++, label("BERE"), boxBere, null);
        addRow(c, row++, label("CAFFÈ"), styled(abilitaCaffe), null);
        addRow(c, row++, label("DOLCE"), styled(abilitaDolce), null);
        addRow(c, row++, label("PREZZO"), prezzoField, null);
        addRow(c, row++, label("NOME"), nomeField, confermaBtn);
    }

    /** Aggiunge una riga: etichetta (col 0), campo principale (col 1), extra opzionale (col 2). */
    private void addRow(GridBagConstraints c, int row, Component labelComp, Component main, Component extra) {
        c.gridy = row;
        c.gridx = 0;
        c.weightx = 0;
        add(labelComp, c);

        c.gridx = 1;
        c.weightx = 1;
        add(main, c);

        c.gridx = 2;
        c.weightx = 0;
        if (extra != null) {
            add(extra, c);
        } else {
            add(javax.swing.Box.createHorizontalStrut(90), c);
        }
    }

    private void wireEvents() {
        abilitaPrimi.addActionListener(e -> toggleCombo(abilitaPrimi, boxPrimi));
        abilitaSecondi.addActionListener(e -> toggleCombo(abilitaSecondi, boxSecondi));
        confermaBtn.addActionListener(e -> onConferma());
    }

    private void toggleCombo(JCheckBox cb, JComboBox<String> combo) {
        combo.setEnabled(cb.isSelected());
        if (!cb.isSelected()) {
            combo.setSelectedItem(null);
        }
    }

    private void onConferma() {
        String nome = nomeField.getText().trim();
        if (nome.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Inserisci un nome per il menu.", "Errore", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int prezzoCents;
        try {
            // accetta sia il punto sia la virgola come separatore decimale
            prezzoCents = Money.parse(prezzoField.getText());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                "Il prezzo deve essere un numero (es. 7.50 o 7,50).", "Errore", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Item menu = new Item(1, prezzoCents, nome, nome, 1);
        GroupedItem.Builder builder = new GroupedItem.Builder().withMenu(menu);

        if (abilitaPrimi.isSelected() && boxPrimi.getSelectedIndex() != -1) {
            builder.withFirst(primiItems.get(boxPrimi.getSelectedIndex()));
        }
        if (abilitaSecondi.isSelected() && boxSecondi.getSelectedIndex() != -1) {
            builder.withSecond(secondiItems.get(boxSecondi.getSelectedIndex()));
        }
        if (boxBere.getSelectedIndex() != -1) {
            builder.withBeverage(bereItems.get(boxBere.getSelectedIndex()));
        }
        if (abilitaDolce.isSelected()) {
            builder.withDessert(new Item(-1, 0, "Dolce", "Dolce", 1));
        }
        if (abilitaCaffe.isSelected()) {
            builder.withCoffee(new Item(-2, 0, "Caffè", "Caffè", 1));
        }

        GroupedItem created;
        try {
            created = builder.build();
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this,
                "Devi indicare almeno il menu principale.", "Errore", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (listener != null) {
            listener.onMenuCreated(created);
        }
        resetForm();
        JOptionPane.showMessageDialog(this, "MENU' CREATO", "Conferma", JOptionPane.INFORMATION_MESSAGE);
    }

    private void resetForm() {
        nomeField.setText("");
        prezzoField.setText("");
        abilitaPrimi.setSelected(false);
        abilitaSecondi.setSelected(false);
        abilitaCaffe.setSelected(false);
        abilitaDolce.setSelected(false);
        boxPrimi.setSelectedItem(null);
        boxSecondi.setSelectedItem(null);
        boxPrimi.setEnabled(false);
        boxSecondi.setEnabled(false);
    }
}
