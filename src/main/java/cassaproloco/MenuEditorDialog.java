package cassaproloco;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 * Editor del listino: permette di modificare dall'app le voci dei file
 * {@code primi.cfg} / {@code secondi.cfg} / {@code bere.cfg} (testo pulsante,
 * testo scontrino, prezzo) senza editarli a mano. Alla conferma salva il file e
 * notifica il {@link SaveListener} per ricaricare la categoria.
 */
public class MenuEditorDialog extends JDialog {

    /** Notificato dopo il salvataggio (col nome del file .cfg modificato). */
    public interface SaveListener {
        void onSaved(String cfgFileName);
    }

    private static final String[] CATEGORIES = {"Primi", "Secondi", "Bere"};
    private static final String[] FILES = {"primi.cfg", "secondi.cfg", "bere.cfg"};

    private final File baseDir;
    private final SaveListener listener;
    private final JComboBox<String> categoryBox = new JComboBox<>(CATEGORIES);
    private final DefaultTableModel model =
            new DefaultTableModel(new String[] {"Testo pulsante", "Testo scontrino", "Prezzo (€)"}, 0);
    private final JTable table = new JTable(model);

    public MenuEditorDialog(Window owner, File baseDir, SaveListener listener) {
        super(owner, "Modifica listino", ModalityType.APPLICATION_MODAL);
        this.baseDir = baseDir;
        this.listener = listener;
        build();
        loadSelected();
        setSize(580, 520);
        setLocationRelativeTo(owner);
    }

    private void build() {
        setLayout(new BorderLayout(0, 8));
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        top.add(new JLabel("Categoria:"));
        categoryBox.addActionListener(e -> loadSelected());
        top.add(categoryBox);
        add(top, BorderLayout.NORTH);

        table.setRowHeight(26);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton addRow = new JButton("Aggiungi");
        addRow.addActionListener(e -> model.addRow(new Object[] {"Nuovo", "Nuovo", "0.00"}));
        JButton delRow = new JButton("Rimuovi");
        delRow.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r >= 0) {
                stopEditing();
                model.removeRow(r);
            }
        });
        JButton save = new JButton("Salva");
        save.addActionListener(e -> save());

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        bottom.add(addRow);
        bottom.add(delRow);
        bottom.add(save);
        add(bottom, BorderLayout.SOUTH);
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

    private void save() {
        stopEditing();
        List<Item> items = new ArrayList<>();
        for (int r = 0; r < model.getRowCount(); r++) {
            String text = String.valueOf(model.getValueAt(r, 0)).trim();
            String print = String.valueOf(model.getValueAt(r, 1)).trim();
            String priceStr = String.valueOf(model.getValueAt(r, 2)).trim();
            if (text.isEmpty()) {
                continue; // riga senza nome: ignorata
            }
            if (text.contains(";") || print.contains(";")) {
                JOptionPane.showMessageDialog(this, "I testi non possono contenere ';' (riga " + (r + 1) + ").");
                return;
            }
            int cents;
            try {
                cents = Money.parse(priceStr);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Prezzo non valido alla riga " + (r + 1) + ": " + priceStr);
                return;
            }
            items.add(new Item(0, cents, text, print.isEmpty() ? text : print, 1));
        }
        try {
            MenuConfigLoader.save(selectedFile(), items);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Errore salvataggio: " + ex.getMessage());
            return;
        }
        if (listener != null) {
            listener.onSaved(FILES[categoryBox.getSelectedIndex()]);
        }
        JOptionPane.showMessageDialog(this, "Listino salvato.");
    }
}
