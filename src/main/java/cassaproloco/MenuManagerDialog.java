package cassaproloco;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Window;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JSplitPane;

/**
 * Finestra unica che integra <b>listino</b> e <b>creazione menu</b>:
 * a sinistra il catalogo modificabile ({@link CatalogEditorPanel}), a destra il
 * compositore ({@link MenuBuilderPanel}). Dal catalogo si selezionano i prodotti
 * e con "Aggiungi al menu" li si versa nel compositore a destra.
 *
 * <p>Sostituisce le due voci separate "LISTINO" (editor) e "NUOVO MENU"
 * (compositore) con un'unica schermata.
 */
public class MenuManagerDialog extends JDialog {

    public MenuManagerDialog(Window owner,
                             File baseDir,
                             CatalogEditorPanel.SaveListener onSaved,
                             MenuBuilderPanel.Listener onMenuCreated) {
        super(owner, "Gestione listino e menu", ModalityType.APPLICATION_MODAL);

        CatalogEditorPanel catalog = new CatalogEditorPanel(baseDir, onSaved);
        MenuBuilderPanel composer = new MenuBuilderPanel();
        composer.setListener(onMenuCreated);
        // Il pulsante "Aggiungi al menu" del catalogo versa i prodotti nel compositore.
        catalog.setAddToMenuHandler(items -> items.forEach(composer::addProduct));

        catalog.setBackground(Theme.BACKGROUND);
        composer.setBackground(Theme.BACKGROUND);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, catalog, composer);
        split.setResizeWeight(0.58);
        split.setBorder(null);
        split.setDividerSize(8);

        JComponent content = (JComponent) getContentPane();
        content.setLayout(new BorderLayout());
        content.setBackground(Theme.BACKGROUND);
        content.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        content.add(split, BorderLayout.CENTER);

        // i due lati con un sottile divisore visivo
        catalog.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(220, 224, 230)),
                BorderFactory.createEmptyBorder(0, 0, 0, 10)));
        composer.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

        setMinimumSize(new Dimension(820, 520));
        setSize(1000, 580);
        setLocationRelativeTo(owner);
    }
}
