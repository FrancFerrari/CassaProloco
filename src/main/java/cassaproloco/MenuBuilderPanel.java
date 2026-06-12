package cassaproloco;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

/**
 * Compositore di un "menu" ({@link GroupedItem}): si dà un nome e un prezzo unico,
 * si aggiungono i prodotti (un elenco libero, pescato dal listino col pulsante
 * "Aggiungi al menu") e alla conferma viene notificato il {@link Listener}.
 *
 * <p>Alla stampa, ogni prodotto del menu produrrà uno scontrino separato; il
 * prezzo indicato qui è quello del menu (registrato una sola volta nel resoconto).
 *
 * <p>È pensato per stare nel pannello destro di {@link MenuManagerDialog}, accanto
 * al catalogo modificabile a sinistra.
 */
public class MenuBuilderPanel extends JPanel {

    /** Notificato quando l'utente crea un nuovo menu. */
    public interface Listener {
        void onMenuCreated(GroupedItem menu);
    }

    private final JTextField nomeField = new JTextField();
    private final JTextField prezzoField = new JTextField();
    private final DefaultListModel<Item> compModel = new DefaultListModel<>();
    private final JList<Item> compList = new JList<>(compModel);
    private final JButton removeBtn = new JButton("Rimuovi prodotto");
    private final JButton clearBtn = new JButton("Svuota");
    private final JButton confermaBtn = new JButton("Crea menu");

    private Listener listener;

    public MenuBuilderPanel() {
        setBackground(Theme.BACKGROUND);
        buildLayout();
        wireEvents();
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    /** Aggiunge un prodotto all'elenco del menu in costruzione. */
    public void addProduct(Item product) {
        if (product != null) {
            compModel.addElement(product);
        }
    }

    private void buildLayout() {
        setLayout(new BorderLayout(0, 8));
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        JLabel title = new JLabel("Nuovo menu");
        title.setFont(Theme.TITLE_FONT);
        title.setForeground(Theme.TEXT_DARK);
        add(title, BorderLayout.NORTH);

        // Centro: nome, prezzo, elenco prodotti
        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0; c.gridy = 0; c.weightx = 0;
        center.add(label("Nome"), c);
        c.gridx = 1; c.weightx = 1;
        center.add(nomeField, c);

        c.gridx = 0; c.gridy = 1; c.weightx = 0;
        center.add(label("Prezzo €"), c);
        c.gridx = 1; c.weightx = 1;
        prezzoField.setToolTipText("Prezzo unico del menu, es. 7.50 o 7,50");
        center.add(prezzoField, c);

        c.gridx = 0; c.gridy = 2; c.weightx = 0; c.anchor = GridBagConstraints.NORTHWEST;
        center.add(label("Prodotti"), c);
        c.gridx = 1; c.gridy = 2; c.weightx = 1; c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        compList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        compList.setCellRenderer(new ItemCellRenderer());
        JScrollPane listScroll = new JScrollPane(compList);
        listScroll.setPreferredSize(new Dimension(280, 220));
        center.add(listScroll, c);

        add(center, BorderLayout.CENTER);

        // Sud: pulsanti rimuovi/svuota + crea
        JPanel south = new JPanel(new BorderLayout(0, 6));
        south.setOpaque(false);

        JPanel listBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        listBtns.setOpaque(false);
        listBtns.add(removeBtn);
        listBtns.add(clearBtn);
        south.add(listBtns, BorderLayout.NORTH);

        confermaBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        confermaBtn.setBackground(Theme.GREEN_BASE);
        confermaBtn.setForeground(Theme.TEXT_ON_DARK);
        confermaBtn.setPreferredSize(new Dimension(0, 44));
        south.add(confermaBtn, BorderLayout.SOUTH);

        add(south, BorderLayout.SOUTH);
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(Theme.TEXT_LIGHT);
        return l;
    }

    private void wireEvents() {
        removeBtn.addActionListener(e -> removeSelected());
        clearBtn.addActionListener(e -> compModel.clear());
        confermaBtn.addActionListener(e -> onConferma());
    }

    private void removeSelected() {
        int[] sel = compList.getSelectedIndices();
        for (int i = sel.length - 1; i >= 0; i--) {
            compModel.remove(sel[i]);
        }
    }

    private void onConferma() {
        String nome = nomeField.getText().trim();
        if (nome.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Inserisci un nome per il menu.", "Errore", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (compModel.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Aggiungi almeno un prodotto al menu (seleziona dal listino e premi \"Aggiungi al menu\").",
                "Errore", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int prezzoCents;
        try {
            prezzoCents = Money.parse(prezzoField.getText());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                "Il prezzo deve essere un numero (es. 7.50 o 7,50).", "Errore", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Item menu = new Item(prezzoCents, nome, nome);
        List<Item> components = new ArrayList<>();
        for (int i = 0; i < compModel.size(); i++) {
            components.add(compModel.get(i));
        }
        GroupedItem created = new GroupedItem(menu, components);

        if (listener != null) {
            listener.onMenuCreated(created);
        }
        resetForm();
        JOptionPane.showMessageDialog(this, "Menu creato.", "Conferma", JOptionPane.INFORMATION_MESSAGE);
    }

    private void resetForm() {
        nomeField.setText("");
        prezzoField.setText("");
        compModel.clear();
    }

    /** Mostra il prodotto come "Testo scontrino" (cade sul testo pulsante se vuoto). */
    private static final class ItemCellRenderer extends javax.swing.DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Item) {
                Item it = (Item) value;
                String label = it.getTextToPrint() == null || it.getTextToPrint().isEmpty()
                        ? it.getText() : it.getTextToPrint();
                setText((index + 1) + ".  " + label);
            }
            return this;
        }
    }
}
