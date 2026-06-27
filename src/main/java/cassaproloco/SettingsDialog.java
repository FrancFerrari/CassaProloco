package cassaproloco;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Impostazioni della cassa: <b>ID cassa</b>, <b>formato rullino</b> e <b>cartella
 * condivisa</b> (per il database comune fra più casse). In più i pulsanti per
 * allineare listino/menu/scontrino con la cartella condivisa.
 *
 * <p>Sono preferenze <b>locali</b> di questo PC (salvate in {@code settings.properties}):
 * ogni cassa ha le proprie.
 */
public class SettingsDialog extends JDialog {

    /** File di listino/menu/scontrino tenuti "in comune" tramite la cartella condivisa. */
    private static final String[] SYNC_FILES = {
            "primi.cfg", "secondi.cfg", "bere.cfg", "groupedItems.json", "receiptTemplate.json"
    };

    private final Settings settings;
    private final File localDir;
    private final Runnable onApplied;
    private final Consumer<Window> openScontrino;
    private final Consumer<Window> openMenuListino;

    private final JTextField cassaIdField = new JTextField();
    private final JComboBox<RollSize> rollBox = new JComboBox<>(RollSize.values());
    private final JTextField sharedField = new JTextField();

    public SettingsDialog(Window owner, Settings settings, File localDir, Runnable onApplied,
                          Consumer<Window> openScontrino, Consumer<Window> openMenuListino) {
        super(owner, "Impostazioni cassa", ModalityType.APPLICATION_MODAL);
        this.settings = settings;
        this.localDir = localDir;
        this.onApplied = onApplied;
        this.openScontrino = openScontrino;
        this.openMenuListino = openMenuListino;

        cassaIdField.setText(settings.getCassaId());
        rollBox.setSelectedItem(settings.getRollSize());
        sharedField.setText(settings.getSharedDirPath());

        JComponent content = (JComponent) getContentPane();
        content.setLayout(new BorderLayout(0, 12));
        content.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        content.add(buildForm(), BorderLayout.CENTER);
        content.add(buildButtons(), BorderLayout.SOUTH);

        setSize(620, 380);
        setMinimumSize(new Dimension(560, 360));
        setLocationRelativeTo(owner);
    }

    private JComponent buildForm() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        cassaIdField.setColumns(8);
        addRow(p, c, row++, "ID cassa", cassaIdField,
                "Es. 1 sulla prima cassa, 2 sulla seconda. Vuoto = cassa singola.");
        addRow(p, c, row++, "Rullino", rollBox,
                "Formato del rullino di QUESTA stampante.");

        JPanel personalizza = new JPanel(new java.awt.GridLayout(1, 2, 8, 0));
        personalizza.setOpaque(false);
        JButton scontrinoBtn = new JButton("Modifica scontrino…");
        scontrinoBtn.addActionListener(e -> {
            if (openScontrino != null) { openScontrino.accept(this); }
        });
        JButton menuBtn = new JButton("Menu e listino…");
        menuBtn.addActionListener(e -> {
            if (openMenuListino != null) { openMenuListino.accept(this); }
        });
        personalizza.add(scontrinoBtn);
        personalizza.add(menuBtn);
        addRow(p, c, row++, "Personalizza", personalizza,
                "Layout dello scontrino e voci di menu/listino.");

        JPanel sharedRow = new JPanel(new BorderLayout(6, 0));
        sharedRow.setOpaque(false);
        sharedRow.add(sharedField, BorderLayout.CENTER);
        JButton browse = new JButton("Sfoglia…");
        browse.addActionListener(e -> chooseSharedDir());
        sharedRow.add(browse, BorderLayout.EAST);
        addRow(p, c, row++, "Cartella condivisa", sharedRow,
                "Cartella in rete vista da entrambe le casse. Vuoto = nessuna condivisione.");

        // pulsanti di allineamento listino/menu/scontrino
        c.gridx = 1; c.gridy = row++; c.weightx = 1;
        JPanel sync = new JPanel(new java.awt.GridLayout(1, 2, 8, 0));
        sync.setOpaque(false);
        JButton push = new JButton("Invia listino/menu → condivisa");
        push.addActionListener(e -> syncFiles(true));
        JButton pull = new JButton("Prendi listino/menu ← condivisa");
        pull.addActionListener(e -> syncFiles(false));
        sync.add(push);
        sync.add(pull);
        p.add(sync, c);

        return p;
    }

    private void addRow(JPanel p, GridBagConstraints c, int row, String label, Component field, String hint) {
        c.gridx = 0; c.gridy = row; c.weightx = 0;
        JLabel l = new JLabel(label);
        l.setPreferredSize(new Dimension(130, 24));
        p.add(l, c);
        c.gridx = 1; c.weightx = 1;
        JPanel cell = new JPanel(new BorderLayout(0, 2));
        cell.setOpaque(false);
        cell.add(field, BorderLayout.CENTER);
        JLabel h = new JLabel(hint);
        h.setForeground(Theme.TEXT_LIGHT);
        h.setFont(h.getFont().deriveFont(h.getFont().getSize2D() - 1f));
        cell.add(h, BorderLayout.SOUTH);
        p.add(cell, c);
    }

    private JComponent buildButtons() {
        JPanel p = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 0));
        JButton save = new JButton("Salva");
        save.setBackground(Theme.GREEN_BASE);
        save.setForeground(Theme.TEXT_ON_DARK);
        save.addActionListener(e -> onSave());
        JButton close = new JButton("Chiudi");
        close.addActionListener(e -> dispose());
        p.add(save);
        p.add(close);
        return p;
    }

    private void chooseSharedDir() {
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (!sharedField.getText().trim().isEmpty()) {
            fc.setCurrentDirectory(new File(sharedField.getText().trim()));
        }
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            sharedField.setText(fc.getSelectedFile().getAbsolutePath());
        }
    }

    private void onSave() {
        RollSize roll = (RollSize) rollBox.getSelectedItem();
        settings.update(cassaIdField.getText(), roll, sharedField.getText());
        if (onApplied != null) {
            onApplied.run();
        }
        JOptionPane.showMessageDialog(this, "Impostazioni salvate.", "Impostazioni",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /** Copia i file di listino/menu/scontrino fra cartella locale e condivisa. */
    private void syncFiles(boolean toShared) {
        String path = sharedField.getText().trim();
        if (path.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Imposta prima la cartella condivisa.",
                    "Cartella condivisa", JOptionPane.WARNING_MESSAGE);
            return;
        }
        File shared = new File(path);
        File from = toShared ? localDir : shared;
        File to = toShared ? shared : localDir;
        if (!from.isDirectory()) {
            JOptionPane.showMessageDialog(this, "Cartella di origine non raggiungibile:\n" + from,
                    "Errore", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!to.isDirectory() && !to.mkdirs()) {
            JOptionPane.showMessageDialog(this, "Cartella di destinazione non raggiungibile:\n" + to,
                    "Errore", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int copied = 0;
        StringBuilder errors = new StringBuilder();
        for (String name : SYNC_FILES) {
            File src = new File(from, name);
            if (!src.isFile()) {
                continue;
            }
            try {
                Files.copy(src.toPath(), new File(to, name).toPath(), StandardCopyOption.REPLACE_EXISTING);
                copied++;
            } catch (IOException ex) {
                errors.append("\n").append(name).append(": ").append(ex.getMessage());
            }
        }
        if (errors.length() > 0) {
            JOptionPane.showMessageDialog(this, "Alcuni file non copiati:" + errors,
                    "Allineamento", JOptionPane.WARNING_MESSAGE);
        } else if (!toShared && copied > 0 && onApplied != null) {
            onApplied.run(); // dopo "Prendi", ricarica listino/menu nell'app
        }
        JOptionPane.showMessageDialog(this, copied + " file allineati.",
                "Allineamento", JOptionPane.INFORMATION_MESSAGE);
    }
}
