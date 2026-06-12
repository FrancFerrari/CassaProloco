package cassaproloco;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.filechooser.FileNameExtensionFilter;

import cassaproloco.ReceiptElement.Align;
import cassaproloco.ReceiptElement.Anchor;
import cassaproloco.ReceiptElement.Kind;

/**
 * Editor dello scontrino: a sinistra l'anteprima interattiva
 * ({@link ReceiptCanvas}) dove trascinare gli elementi, a destra i controlli per
 * l'elemento selezionato (testo, font, dimensione, allineamento, posizione,
 * visibilità), più logo e riga in fondo. Salva su {@link ReceiptTemplateStore}.
 *
 * <p>Lavora su una <b>copia</b> del template: finché non premi "Salva" la stampa
 * reale non cambia. "Ripristina default" riporta allo scontrino storico.
 */
public class ReceiptEditorDialog extends JDialog {

    private final ReceiptTemplateStore store;
    private final Runnable onSaved;

    private ReceiptTemplate working;
    private final ReceiptCanvas canvas;

    private boolean updating; // evita loop mentre si popolano i controlli

    // controlli
    private final JComboBox<ElementItem> elementBox = new JComboBox<>();
    private final JCheckBox visibleBox = new JCheckBox("Visibile");
    private final JTextField textField = new JTextField();
    private final JComboBox<String> fontBox = new JComboBox<>(
            GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
    private final JSpinner sizeSpin = new JSpinner(new SpinnerNumberModel(8, 1, 200, 1));
    private final JCheckBox boldBox = new JCheckBox("Grassetto");
    private final JComboBox<String> alignBox = new JComboBox<>(new String[] {"Sinistra", "Centro", "Destra"});
    private final JComboBox<String> anchorBox = new JComboBox<>(new String[] {"Sinistra", "Centro", "Destra"});
    private final JSpinner xSpin = new JSpinner(new SpinnerNumberModel(0, -50, 400, 1));
    private final JSpinner ySpin = new JSpinner(new SpinnerNumberModel(0, -20, 300, 1));
    private final JButton logoBtn = new JButton("Scegli immagine…");
    private final JSpinner logoWSpin = new JSpinner(new SpinnerNumberModel(40, 4, 300, 1));
    private final JSpinner logoHSpin = new JSpinner(new SpinnerNumberModel(40, 4, 300, 1));

    public ReceiptEditorDialog(Window owner, ReceiptTemplateStore store, RollSize roll, Runnable onSaved) {
        super(owner, "Personalizza scontrino", ModalityType.APPLICATION_MODAL);
        this.store = store;
        this.onSaved = onSaved;
        this.working = store.load().copy();

        canvas = new ReceiptCanvas(working);
        canvas.setRoll(roll);
        canvas.setListener(new ReceiptCanvas.Listener() {
            @Override public void onSelected(ReceiptElement el) { selectInBox(el); }
            @Override public void onChanged() { if (!updating) syncXYFromSelection(); }
        });

        setLayout(new BorderLayout(10, 10));
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        getContentPane().setBackground(Theme.BACKGROUND);

        add(buildTop(roll), BorderLayout.NORTH);
        JScrollPane sp = new JScrollPane(canvas);
        sp.setBorder(null);
        add(sp, BorderLayout.CENTER);
        add(buildSide(), BorderLayout.EAST);

        wire();
        reloadElementBox();
        if (elementBox.getItemCount() > 0) {
            elementBox.setSelectedIndex(0);
        }

        setSize(1140, 620);
        setMinimumSize(new Dimension(1000, 540));
        setLocationRelativeTo(owner);
    }

    // ----- intestazione: scelta rullino per l'anteprima -----
    private JComponent buildTop(RollSize roll) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        p.setOpaque(false);
        JLabel l = new JLabel("Anteprima rullino:");
        l.setForeground(Theme.TEXT_LIGHT);
        JComboBox<RollSize> rollBox = new JComboBox<>(RollSize.values());
        rollBox.setSelectedItem(roll);
        rollBox.addActionListener(e -> {
            RollSize r = (RollSize) rollBox.getSelectedItem();
            if (r != null) { canvas.setRoll(r); }
        });
        p.add(l);
        p.add(rollBox);
        JLabel hint = new JLabel("   Trascina gli elementi sull'anteprima per spostarli.");
        hint.setForeground(Theme.TEXT_LIGHT);
        p.add(hint);
        return p;
    }

    // ----- pannello laterale dei controlli -----
    private JComponent buildSide() {
        JPanel side = new JPanel(new GridBagLayout());
        side.setOpaque(false);
        side.setPreferredSize(new Dimension(300, 0));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridy = 0; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(4, 4, 4, 4);

        JLabel title = new JLabel("Elemento");
        title.setFont(Theme.TITLE_FONT);
        title.setForeground(Theme.TEXT_DARK);
        side.add(title, c); c.gridy++;

        side.add(elementBox, c); c.gridy++;
        side.add(visibleBox, c); c.gridy++;
        side.add(row("Testo", textField), c); c.gridy++;
        side.add(row("Font", fontBox), c); c.gridy++;
        side.add(row("Dimensione", sizeSpin), c); c.gridy++;
        side.add(boldBox, c); c.gridy++;
        side.add(row("Allineamento", alignBox), c); c.gridy++;
        side.add(row("Adatta (54mm)", anchorBox), c); c.gridy++;
        side.add(row("X", xSpin), c); c.gridy++;
        side.add(row("Y", ySpin), c); c.gridy++;

        JLabel logoTitle = new JLabel("Logo");
        logoTitle.setForeground(Theme.TEXT_LIGHT);
        side.add(logoTitle, c); c.gridy++;
        side.add(logoBtn, c); c.gridy++;
        side.add(row("Largh.", logoWSpin), c); c.gridy++;
        side.add(row("Alt.", logoHSpin), c); c.gridy++;

        // spazio elastico
        c.weighty = 1; side.add(Box.createVerticalGlue(), c); c.gridy++; c.weighty = 0;

        // pulsanti
        JButton resetBtn = new JButton("Ripristina default");
        resetBtn.addActionListener(e -> onReset());
        JButton saveBtn = new JButton("Salva");
        saveBtn.setBackground(Theme.GREEN_BASE);
        saveBtn.setForeground(Theme.TEXT_ON_DARK);
        saveBtn.addActionListener(e -> onSave());
        JButton closeBtn = new JButton("Chiudi");
        closeBtn.addActionListener(e -> dispose());

        JPanel btns = new JPanel(new GridBagLayout());
        btns.setOpaque(false);
        GridBagConstraints bc = new GridBagConstraints();
        bc.gridx = 0; bc.gridy = 0; bc.weightx = 1; bc.fill = GridBagConstraints.HORIZONTAL; bc.insets = new Insets(2, 0, 2, 0);
        btns.add(saveBtn, bc); bc.gridy++;
        btns.add(resetBtn, bc); bc.gridy++;
        btns.add(closeBtn, bc);
        side.add(btns, c);

        return side;
    }

    private JComponent row(String label, Component field) {
        JPanel p = new JPanel(new BorderLayout(6, 0));
        p.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setForeground(Theme.TEXT_LIGHT);
        l.setPreferredSize(new Dimension(96, 22));
        p.add(l, BorderLayout.WEST);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    // ----- eventi dei controlli -----
    private void wire() {
        elementBox.addActionListener(e -> {
            if (updating) { return; }
            ElementItem it = (ElementItem) elementBox.getSelectedItem();
            ReceiptElement el = it == null ? null : it.el;
            canvas.setSelected(el);
            populate(el);
        });
        visibleBox.addActionListener(e -> apply(el -> el.visible = visibleBox.isSelected(), true));
        textField.getDocument().addDocumentListener(new SimpleDocListener(() ->
                apply(el -> el.text = textField.getText(), false)));
        fontBox.addActionListener(e -> apply(el -> el.fontFamily = (String) fontBox.getSelectedItem(), false));
        sizeSpin.addChangeListener(e -> apply(el -> el.fontSize = (Integer) sizeSpin.getValue(), false));
        boldBox.addActionListener(e -> apply(el -> el.bold = boldBox.isSelected(), false));
        alignBox.addActionListener(e -> apply(el -> el.align = alignFromIndex(alignBox.getSelectedIndex()), false));
        anchorBox.addActionListener(e -> apply(el -> el.anchor = anchorFromIndex(anchorBox.getSelectedIndex()), false));
        xSpin.addChangeListener(e -> apply(el -> el.x = (Integer) xSpin.getValue(), false));
        ySpin.addChangeListener(e -> apply(el -> el.y = (Integer) ySpin.getValue(), false));
        logoWSpin.addChangeListener(e -> apply(el -> el.imgW = (Integer) logoWSpin.getValue(), false));
        logoHSpin.addChangeListener(e -> apply(el -> el.imgH = (Integer) logoHSpin.getValue(), false));
        logoBtn.addActionListener(e -> chooseLogo());
    }

    /** Applica una modifica all'elemento selezionato e aggiorna l'anteprima. */
    private void apply(java.util.function.Consumer<ReceiptElement> change, boolean reloadList) {
        if (updating) { return; }
        ReceiptElement el = canvas.getSelected();
        if (el == null) { return; }
        change.accept(el);
        canvas.refresh();
        canvas.setSelected(el);
        if (reloadList) {
            // la visibilità non cambia la lista, ma teniamo la selezione coerente
            canvas.setSelected(el);
        }
    }

    /** Popola i controlli dall'elemento (senza far scattare gli eventi). */
    private void populate(ReceiptElement el) {
        updating = true;
        try {
            boolean isText = el != null && el.kind != Kind.LOGO;
            boolean isLogo = el != null && el.kind == Kind.LOGO;
            boolean textEditable = el != null && (el.kind == Kind.HEADER1 || el.kind == Kind.HEADER2
                    || el.kind == Kind.FOOTER || el.kind == Kind.DATE || el.kind == Kind.TIME);

            visibleBox.setEnabled(el != null);
            visibleBox.setSelected(el != null && el.visible);

            textField.setEnabled(textEditable);
            textField.setText(textEditable ? el.text : (el == null ? "" :
                    (el.kind == Kind.ITEM || el.kind == Kind.PRICE ? "(automatico)" : "")));

            fontBox.setEnabled(isText);
            sizeSpin.setEnabled(isText);
            boldBox.setEnabled(isText);
            alignBox.setEnabled(isText);
            if (el != null) {
                if (isText) {
                    fontBox.setSelectedItem(el.fontFamily);
                    sizeSpin.setValue(el.fontSize);
                    boldBox.setSelected(el.bold);
                    alignBox.setSelectedIndex(alignIndex(el.align));
                }
                anchorBox.setSelectedIndex(alignIndex(el.anchor == Anchor.CENTER ? Align.CENTER
                        : el.anchor == Anchor.RIGHT ? Align.RIGHT : Align.LEFT));
                xSpin.setValue(el.x);
                ySpin.setValue(el.y);
            }
            anchorBox.setEnabled(el != null);
            xSpin.setEnabled(el != null);
            ySpin.setEnabled(el != null);

            logoBtn.setEnabled(isLogo);
            logoWSpin.setEnabled(isLogo);
            logoHSpin.setEnabled(isLogo);
            if (isLogo) {
                logoWSpin.setValue(Math.max(4, el.imgW));
                logoHSpin.setValue(Math.max(4, el.imgH));
            }
        } finally {
            updating = false;
        }
    }

    private void syncXYFromSelection() {
        ReceiptElement el = canvas.getSelected();
        if (el == null) { return; }
        updating = true;
        try {
            xSpin.setValue(el.x);
            ySpin.setValue(el.y);
        } finally {
            updating = false;
        }
    }

    private void selectInBox(ReceiptElement el) {
        if (el == null) { return; }
        updating = true;
        try {
            for (int i = 0; i < elementBox.getItemCount(); i++) {
                if (elementBox.getItemAt(i).el == el) {
                    elementBox.setSelectedIndex(i);
                    break;
                }
            }
        } finally {
            updating = false;
        }
        populate(el);
    }

    private void reloadElementBox() {
        updating = true;
        try {
            DefaultComboBoxModel<ElementItem> m = new DefaultComboBoxModel<>();
            for (ReceiptElement el : working.elements) {
                m.addElement(new ElementItem(el));
            }
            elementBox.setModel(m);
        } finally {
            updating = false;
        }
    }

    private void chooseLogo() {
        ReceiptElement el = canvas.getSelected();
        if (el == null || el.kind != Kind.LOGO) { return; }
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("Immagini (PNG, JPG, GIF)", "png", "jpg", "jpeg", "gif"));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            el.imagePath = fc.getSelectedFile().getAbsolutePath();
            el.visible = true;
            updating = true;
            try { visibleBox.setSelected(true); } finally { updating = false; }
            canvas.refresh();
            canvas.setSelected(el);
        }
    }

    private void onReset() {
        int r = JOptionPane.showConfirmDialog(this,
                "Riportare lo scontrino al layout predefinito? Le personalizzazioni andranno perse.",
                "Ripristina default", JOptionPane.YES_NO_OPTION);
        if (r != JOptionPane.YES_OPTION) { return; }
        working = ReceiptTemplate.defaultTemplate();
        canvas.setTemplate(working);
        reloadElementBox();
        if (elementBox.getItemCount() > 0) { elementBox.setSelectedIndex(0); }
    }

    private void onSave() {
        try {
            store.save(working);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Errore salvataggio: " + ex.getMessage(),
                    "Errore", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (onSaved != null) { onSaved.run(); }
        JOptionPane.showMessageDialog(this, "Scontrino salvato. Le prossime stampe useranno questo layout.",
                "Salvato", JOptionPane.INFORMATION_MESSAGE);
    }

    // ----- helper enum/indice -----
    private static int alignIndex(Align a) {
        return a == Align.CENTER ? 1 : a == Align.RIGHT ? 2 : 0;
    }
    private static Align alignFromIndex(int i) {
        return i == 1 ? Align.CENTER : i == 2 ? Align.RIGHT : Align.LEFT;
    }
    private static Anchor anchorFromIndex(int i) {
        return i == 1 ? Anchor.CENTER : i == 2 ? Anchor.RIGHT : Anchor.LEFT;
    }

    /** Voce del combo: un elemento col suo nome leggibile. */
    private static final class ElementItem {
        final ReceiptElement el;
        ElementItem(ReceiptElement el) { this.el = el; }
        @Override public String toString() {
            switch (el.kind) {
                case HEADER1: return "Intestazione 1";
                case HEADER2: return "Intestazione 2";
                case ITEM:    return "Voce (Nx nome)";
                case PRICE:   return "Prezzo";
                case DATE:    return "Data";
                case TIME:    return "Ora";
                case FOOTER:  return "Riga in fondo";
                case LOGO:    return "Logo";
                default:      return el.kind.name();
            }
        }
    }

    /** Piccolo listener di documento che esegue un'azione a ogni modifica. */
    private static final class SimpleDocListener implements javax.swing.event.DocumentListener {
        private final Runnable r;
        SimpleDocListener(Runnable r) { this.r = r; }
        @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { r.run(); }
        @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { r.run(); }
        @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { r.run(); }
    }
}
