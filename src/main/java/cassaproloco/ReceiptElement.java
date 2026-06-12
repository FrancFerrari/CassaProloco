package cassaproloco;

import java.io.Serializable;

/**
 * Un elemento dello scontrino: una riga di testo fisso (intestazioni, riga in
 * fondo), un campo dinamico (quantità+nome, prezzo, data, ora) oppure un logo.
 *
 * <p>È un semplice contenitore di proprietà (testo, font, posizione, ecc.):
 * viene modificato dall'editor e salvato in JSON. Il rendering vero e proprio è
 * in {@link ReceiptModel}, che resta l'unica via di stampa.
 *
 * <p>Le coordinate sono nello spazio dello scontrino a <b>62 mm</b> (lo stesso di
 * sempre). Su rullini più stretti la posizione orizzontale viene adattata in base
 * all'{@link Anchor}, così il default resta identico al 62 mm e funziona anche a
 * 54 mm.
 */
public class ReceiptElement implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Cosa rappresenta l'elemento. */
    public enum Kind {
        /** Testo fisso (1ª riga intestazione). */ HEADER1,
        /** Testo fisso (2ª riga intestazione). */ HEADER2,
        /** Campo dinamico: "Nx nome". */          ITEM,
        /** Campo dinamico: prezzo. */             PRICE,
        /** Campo dinamico: data. */               DATE,
        /** Campo dinamico: ora. */                TIME,
        /** Testo fisso a piè di scontrino. */     FOOTER,
        /** Immagine/logo. */                      LOGO
    }

    /** Allineamento del testo dentro il suo riquadro. */
    public enum Align { LEFT, CENTER, RIGHT }

    /** Adattamento orizzontale su rullini più stretti del 62 mm. */
    public enum Anchor { LEFT, CENTER, RIGHT }

    /** Larghezza riquadro = larghezza del contenuto. */
    public static final int WIDTH_AUTO = -2;
    /** Larghezza riquadro = larghezza del pannello (intero scontrino). */
    public static final int WIDTH_FULL = -1;
    /** Altezza riquadro = altezza del contenuto. */
    public static final int HEIGHT_AUTO = 0;

    public Kind kind;
    public String text = "";          // testo fisso, o prefisso per DATE/TIME
    public String fontFamily = "Tahoma";
    public int fontSize = 8;
    public boolean bold = false;
    public int x = 0;
    public int y = 0;
    public int width = WIDTH_AUTO;
    public int height = HEIGHT_AUTO;
    public Align align = Align.LEFT;
    public Anchor anchor = Anchor.LEFT;
    public boolean visible = true;

    // --- solo per LOGO ---
    public String imagePath = "";
    public int imgW = 0;
    public int imgH = 0;

    public ReceiptElement() {
    }

    public ReceiptElement(Kind kind) {
        this.kind = kind;
    }

    /** Copia profonda (per l'editor: modifica una copia senza toccare l'originale). */
    public ReceiptElement copy() {
        ReceiptElement e = new ReceiptElement(kind);
        e.text = text;
        e.fontFamily = fontFamily;
        e.fontSize = fontSize;
        e.bold = bold;
        e.x = x;
        e.y = y;
        e.width = width;
        e.height = height;
        e.align = align;
        e.anchor = anchor;
        e.visible = visible;
        e.imagePath = imagePath;
        e.imgW = imgW;
        e.imgH = imgH;
        return e;
    }
}
