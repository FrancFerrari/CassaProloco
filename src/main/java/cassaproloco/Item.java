package cassaproloco;

import java.io.Serializable;
import java.util.Objects;

/**
 * Voce di menu immutabile: un articolo singolo (es. "Coca Cola") oppure il
 * menu principale di un {@link GroupedItem}.
 *
 * <p>È immutabile e viene usata come chiave in {@link java.util.HashMap}
 * (vedi {@link Basket}); per questo {@code equals} e {@code hashCode} devono
 * restare coerenti e i campi non devono cambiare dopo la costruzione.
 * L'eventuale azzeramento prezzo (omaggio) è gestito da {@link Basket} tramite
 * override, senza mutare l'Item.
 */
public final class Item implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int id;
    private final float price;
    private final String text;
    private final String textToPrint;
    private final int qty;

    public Item(int id, float price, String text, String textToPrint, int qty) {
        this.id = id;
        this.price = price;
        this.text = text == null ? "" : text;
        this.textToPrint = textToPrint == null ? "" : textToPrint;
        this.qty = qty;
    }

    public Item() {
        this(0, 0f, "", "", 0);
    }

    public int getID() {
        return id;
    }

    public float getprice() {
        return price;
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
        return text + " - " + textToPrint + " - " + price + " €";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Item)) return false;
        Item i = (Item) o;
        return Float.compare(i.price, price) == 0
            && text.equals(i.text)
            && textToPrint.equals(i.textToPrint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, textToPrint, price);
    }
}
