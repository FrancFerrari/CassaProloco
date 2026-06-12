package cassaproloco;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import cassaproloco.ReceiptElement.Align;
import cassaproloco.ReceiptElement.Anchor;
import cassaproloco.ReceiptElement.Kind;

/**
 * Modello (template) dello scontrino: l'elenco ordinato degli {@link ReceiptElement}
 * che lo compongono. L'editor lo modifica, {@link ReceiptModel} lo stampa.
 *
 * <p>{@link #defaultTemplate()} riproduce <b>esattamente</b> lo scontrino storico:
 * finché il template non viene personalizzato, la stampa resta identica a prima
 * (le coordinate qui sono quelle del codice originale, nello spazio a 62 mm).
 */
public class ReceiptTemplate implements Serializable {
    private static final long serialVersionUID = 1L;

    public List<ReceiptElement> elements = new ArrayList<>();

    /** Template predefinito = layout storico dello scontrino (stampa identica). */
    public static ReceiptTemplate defaultTemplate() {
        ReceiptTemplate t = new ReceiptTemplate();

        // 1ª intestazione: "Antica Sagra di S.Luigi " (Tahoma grassetto 8), x=39 y=7
        t.elements.add(text(Kind.HEADER1, "Antica Sagra di S.Luigi ", 8, true,
                39, 7, Align.LEFT, Anchor.CENTER));

        // 2ª intestazione: "ProLoco Cogollo" (Tahoma 8), x=55 y=17
        t.elements.add(text(Kind.HEADER2, "ProLoco Cogollo", 8, false,
                55, 17, Align.LEFT, Anchor.CENTER));

        // Voce "Nx nome" (Tahoma grassetto 12), centrata su tutta la larghezza, x=-5 y=30 h=18
        ReceiptElement item = text(Kind.ITEM, "", 12, true, -5, 30, Align.CENTER, Anchor.LEFT);
        item.width = ReceiptElement.WIDTH_FULL;
        item.height = 18;
        t.elements.add(item);

        // Prezzo (Tahoma 12), ancorato a destra, x=130 y=55
        t.elements.add(text(Kind.PRICE, "", 12, false, 130, 55, Align.LEFT, Anchor.RIGHT));

        // Data (Tahoma 6), x=15 y=55, prefisso "Data:    "
        t.elements.add(text(Kind.DATE, "Data:    ", 6, false, 15, 55, Align.LEFT, Anchor.LEFT));

        // Ora (Tahoma 6), x=15 y=66, prefisso "Ora:     "
        t.elements.add(text(Kind.TIME, "Ora:     ", 6, false, 15, 66, Align.LEFT, Anchor.LEFT));

        // Riga in fondo e logo: presenti ma NASCOSTI di default (così il default è invariato)
        ReceiptElement footer = text(Kind.FOOTER, "Grazie!", 8, false, 0, 95, Align.CENTER, Anchor.LEFT);
        footer.width = ReceiptElement.WIDTH_FULL;
        footer.visible = false;
        t.elements.add(footer);

        ReceiptElement logo = new ReceiptElement(Kind.LOGO);
        logo.x = 5;
        logo.y = 5;
        logo.imgW = 40;
        logo.imgH = 40;
        logo.visible = false;
        t.elements.add(logo);

        return t;
    }

    private static ReceiptElement text(Kind kind, String txt, int size, boolean bold,
                                       int x, int y, Align align, Anchor anchor) {
        ReceiptElement e = new ReceiptElement(kind);
        e.text = txt;
        e.fontFamily = "Tahoma";
        e.fontSize = size;
        e.bold = bold;
        e.x = x;
        e.y = y;
        e.align = align;
        e.anchor = anchor;
        return e;
    }

    /** Copia profonda del template (per l'editor). */
    public ReceiptTemplate copy() {
        ReceiptTemplate t = new ReceiptTemplate();
        for (ReceiptElement e : elements) {
            t.elements.add(e.copy());
        }
        return t;
    }

    /** Primo elemento del tipo indicato, o {@code null} se assente. */
    public ReceiptElement find(Kind kind) {
        for (ReceiptElement e : elements) {
            if (e.kind == kind) {
                return e;
            }
        }
        return null;
    }
}
