package cassaproloco;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

/**
 * Editor del listino (pannello riusabile): modifica le voci dei file
 * {@code primi.cfg} / {@code secondi.cfg} / {@code bere.cfg} (testo pulsante,
 * testo scontrino, prezzo) e le salva. In più espone "Aggiungi al menu", che
 * passa i prodotti selezionati al compositore ({@link MenuBuilderPanel}) nella
 * stessa finestra ({@link MenuManagerDialog}).
 */
public class CatalogEditorPanel extends JPanel {

    /** Notificato dopo il salvataggio (col nome del file .cfg modificato). */
    public interface SaveListener {
        void onSaved(String cfgFileName);
    }

    private static final String[] CATEGORIES = {"Primi", "Secondi", "Bere"};
    private static final String[] FILES = {"primi.cfg", "secondi.cfg", "bere.cfg"};

    private final File baseDir;
    private final SaveListener saveListener;
    private Consumer<List<Item>> addToMenu = items -> { };

    private final JComboBox<String> categoryBox = new JComboBox<>(CATEGORIES);
    private final DefaultTableModel model =
            new DefaultTableModel(new String[] {"Testo pulsante", "Testo scontrino", "Prezzo (€)"}, 0);
    private final JTable table = new JTable(model);

    public CatalogEditorPanel(File baseDir, SaveListener saveListener) {
        this.baseDir = baseDir;
        this.saveListener = saveListener;
        build();
        loadSelected();
    }

    /** Imposta cosa fare con i prodotti selezionati quando si preme "Aggiungi al menu". */
    public void setAddToMenuHandler(Consumer<List<Item>> handler) {
        this.addToMenu = handler != null ? handler : items -> { };
    }

    private void build() {
        setLayout(new BorderLayout(0, 8));
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        JLabel title = new JLabel("Listino");
        title.setFont(Theme.TITLE_FONT);
        title.setForeground(Theme.TEXT_DARK);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(title, BorderLayout.NORTH);
        JPanel catRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        catRow.setOpaque(false);
        catRow.add(new JLabel("Categoria:"));
        categoryBox.addActionListener(e -> loadSelected());
        catRow.add(categoryBox);
        top.add(catRow, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        table.setRowHeight(26);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton addRow = new JButton("Aggiungi");
        addRow.addActionListener(e -> model.addRow(new Object[] {"Nuovo", "Nuovo", "0.00"}));
        JButton delRow = new JButton("Rimuovi");
        delRow.addActionListener(e -> removeSelectedRows());
        JButton save = new JButton("Salva listino");
        save.addActionListener(e -> save());

        JButton toMenu = new JButton("Aggiungi al menu →");
        toMenu.setFont(new Font("Segoe UI", Font.BOLD, 14));
        toMenu.setBackground(Theme.PRIMARY);
        toMenu.setForeground(Theme.TEXT_ON_DARK);
        toMenu.addActionListener(e -> addSelectedToMenu());

        JPanel editBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        editBtns.setOpaque(false);
        editBtns.add(addRow);
        editBtns.add(delRow);
        editBtns.add(save);

        JPanel bottom = new JPanel(new BorderLayout(0, 6));
        bottom.setOpaque(false);
        bottom.add(editBtns, BorderLayout.NORTH);
        bottom.add(toMenu, BorderLayout.SOUTH);
        add(bottom, BorderLayout.SOUTH);
    }

    private void removeSelectedRows() {
        stopEditing();
        int[] sel = table.getSelectedRows();
        for (int i = sel.length - 1; i >= 0; i--) {
            model.removeRow(sel[i]);
        }
    }

    private File selectedFile() {
        return new File(baseDir, FILES[categoryBox.getSelectedIndex()]);
    }

    private void loadSelected() {
        model.setRowCount(0);
        File f = selectedFile();
        if (!f.exists()) {
            return;
        }
        try {
            for (Item it : MenuConfigLoader.load(f)) {
                model.addRow(new Object[] {it.getText(), it.getTextToPrint(), Money.formatRoot(it.getPriceCents())});
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Errore lettura: " + ex.getMessage());
        }
    }

    private void stopEditing() {
        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }
    }

    /** Converte una riga della tabella in Item; null (con messaggio) se prezzo non valido. */
    private Item rowToItem(int r) {
        String text = String.valueOf(model.getValueAt(r, 0)).trim();
        String print = String.valueOf(model.getValueAt(r, 1)).trim();
        String priceStr = String.valueOf(model.getValueAt(r, 2)).trim();
        if (text.isEmpty()) {
            return null;
        }
        if (text.contains(";") || print.contains(";")) {
            JOptionPane.showMessageDialog(this, "I testi non possono contenere ';' (riga " + (r + 1) + ").");
            return null;
        }
        int cents;
        try {
            cents = Money.parse(priceStr);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Prezzo non valido alla riga " + (r + 1) + ": " + priceStr);
            return null;
        }
        return new Item(cents, text, print.isEmpty() ? text : print);
    }

    private void save() {
        stopEditing();
        List<Item> items = new ArrayList<>();
        for (int r = 0; r < model.getRowCount(); r++) {
            String text = String.valueOf(model.getValueAt(r, 0)).trim();
            if (text.isEmpty()) {
                continue; // riga senza nome: ignorata
            }
            Item it = rowToItem(r);
            if (it == null) {
                return; // errore già segnalato
            }
            items.add(it);
        }
        try {
            MenuConfigLoader.save(selectedFile(), items);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Errore salvataggio: " + ex.getMessage());
            return;
        }
        if (saveListener != null) {
            saveListener.onSaved(FILES[categoryBox.getSelectedIndex()]);
        }
        JOptionPane.showMessageDialog(this, "Listino salvato.");
    }

    private void addSelectedToMenu() {
        stopEditing();
        int[] sel = table.getSelectedRows();
        if (sel.length == 0) {
            JOptionPane.showMessageDialog(this,
                "Seleziona uno o più prodotti dalla tabella, poi premi \"Aggiungi al menu\".",
                "Nessun prodotto selezionato", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        List<Item> picked = new ArrayList<>();
        for (int r : sel) {
            Item it = rowToItem(r);
            if (it == null) {
                return; // riga non valida: errore già segnalato
            }
            picked.add(it);
        }
        addToMenu.accept(picked);
    }
}
