package cassaproloco;

import java.io.Serializable;
import java.util.Objects;

/**
 * Voce di menu immutabile: un articolo singolo (es. "Coca Cola") oppure il
 * menu principale di un {@link GroupedItem}.
 *
 * <p>Il prezzo è in <b>centesimi interi</b> (vedi {@link Money}) per evitare
 * errori di arrotondamento.
 *
 * <p>È immutabile e viene usata come chiave in {@link java.util.HashMap}
 * (vedi {@link Basket}); per questo {@code equals} e {@code hashCode} devono
 * restare coerenti e i campi non devono cambiare dopo la costruzione.
 * L'eventuale azzeramento prezzo (omaggio) è gestito da {@link Basket} tramite
 * override, senza mutare l'Item.
 */
public final class Item implements Serializable {
    private static final long serialVersionUID = 2L;

    private final int id;
    private final int priceCents;
    private final String text;
    private final String textToPrint;
    private final int qty;

    public Item(int id, int priceCents, String text, String textToPrint, int qty) {
        this.id = id;
        this.priceCents = priceCents;
        this.text = text == null ? "" : text;
        this.textToPrint = textToPrint == null ? "" : textToPrint;
        this.qty = qty;
    }

    public Item() {
        this(0, 0, "", "", 0);
    }

    public int getID() {
        return id;
    }

    /** Prezzo in centesimi. */
    public int getPriceCents() {
        return priceCents;
    }

    public String getText() {
        return text;
    }

    public String getTextToPrint() {
        return textToPrint;
    }

    public int getQty() {
        return qty;
    }

    @Override
    public String toString() {
        return text + " - " + textToPrint + " - " + Money.format(priceCents) + " €";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Item)) return false;
        Item i = (Item) o;
        return priceCents == i.priceCents
            && text.equals(i.text)
            && textToPrint.equals(i.textToPrint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, textToPrint, priceCents);
    }
}
