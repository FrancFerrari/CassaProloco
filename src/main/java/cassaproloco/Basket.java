package cassaproloco;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Modello dati del carrello: tiene le quantità di {@link Item} singoli e di
 * {@link GroupedItem} (menu), oltre agli eventuali override di prezzo usati
 * dalla funzione "Omaggio".
 *
 * <p>L'omaggio NON muta gli Item (che sono immutabili e usati come chiavi di
 * mappa): registra invece un prezzo effettivo alternativo in
 * {@link #priceOverrides} e lo applica nei calcoli.
 */
public class Basket implements Iterable<Map.Entry<Item, Integer>> {
    private JPanelBasket parent;
    private final Map<Item, Integer> items        = new HashMap<>();
    private final Map<GroupedItem, Integer> groups = new HashMap<>();
    private final Map<Item, Integer> priceOverrides = new HashMap<>(); // centesimi

    public void setParent(JPanelBasket parent) {
        this.parent = parent;
    }

    public void clear() {
        items.clear();
        groups.clear();
        priceOverrides.clear();
        notifyUI();
    }

    /** Prezzo effettivo dell'item in centesimi, tenendo conto di eventuali omaggi. */
    public int getEffectivePrice(Item i) {
        Integer override = priceOverrides.get(i);
        return override != null ? override : i.getPriceCents();
    }

    // --- Item methods ---

    public int addItem(Item i) {
        items.merge(i, 1, Integer::sum);
        notifyUI();
        return items.get(i);
    }

    public int subtractItem(Item i) {
        Integer qty = items.getOrDefault(i, 0);
        if (qty > 0) {
            if (qty == 1) items.remove(i);
            else          items.put(i, qty - 1);
        }
        notifyUI();
        return items.getOrDefault(i, 0);
    }

    public void removeItem(Item i) {
        items.remove(i);
        priceOverrides.remove(i);
        notifyUI();
    }

    public void removeGroupedItem(GroupedItem gi) {
        priceOverrides.remove(gi.getMenu());
        groups.remove(gi);
        notifyUI();
    }

    public int getItemQty(Item i) {
        return items.getOrDefault(i, 0);
    }

    /** Totale (in centesimi) per l'item. */
    public int getItemTotalPrice(Item i) {
        return getEffectivePrice(i) * getItemQty(i);
    }

    // --- GroupedItem methods ---

    public int addGroupedItem(GroupedItem gi) {
        groups.merge(gi, 1, Integer::sum);
        notifyUI();
        return groups.get(gi);
    }

    public int subtractGroupedItem(GroupedItem gi) {
        Integer qty = groups.getOrDefault(gi, 0);
        if (qty > 0) {
            if (qty == 1) groups.remove(gi);
            else          groups.put(gi, qty - 1);
        }
        notifyUI();
        return groups.getOrDefault(gi, 0);
    }

    public int getGroupedItemQty(GroupedItem gi) {
        return groups.getOrDefault(gi, 0);
    }

    /** Totale (in centesimi) per il menu combinato. */
    public int getGroupedItemTotalPrice(GroupedItem gi) {
        return getEffectivePrice(gi.getMenu()) * getGroupedItemQty(gi);
    }

    // --- Common ---

    /** Totale del carrello in centesimi, prezzi effettivi (omaggi inclusi). */
    public int getTotalPrice() {
        int sum = 0;
        for (Map.Entry<Item, Integer> e : items.entrySet()) {
            sum += getEffectivePrice(e.getKey()) * e.getValue();
        }
        for (Map.Entry<GroupedItem, Integer> e : groups.entrySet()) {
            sum += getEffectivePrice(e.getKey().getMenu()) * e.getValue();
        }
        return sum;
    }

    /** Omaggio: azzera il prezzo effettivo di tutto ciò che è nel carrello. */
    public void setPricesToZero() {
        for (Item item : items.keySet()) {
            priceOverrides.put(item, 0);
        }
        for (GroupedItem gi : groups.keySet()) {
            priceOverrides.put(gi.getMenu(), 0);
        }
        notifyUI();
    }

    /** Ripristina i prezzi originali rimuovendo tutti gli override. */
    public void restorePrices() {
        priceOverrides.clear();
        notifyUI();
    }

    public String getName(Item i) {
        return i.getText();
    }

    /** Notifica il JPanelBasket di aggiornare le righe e il totale. */
    private void notifyUI() {
        if (parent != null) {
            parent.updateAll();
        }
    }

    @Override
    public Iterator<Map.Entry<Item, Integer>> iterator() {
        return items.entrySet().iterator();
    }

    /** Numero totale di righe (item + group). */
    public int size() {
        return items.size() + groups.size();
    }

    /** Numero totale di pezzi nel carrello (somma di tutte le quantità). */
    public int totalQuantity() {
        int n = 0;
        for (int q : items.values()) {
            n += q;
        }
        for (int q : groups.values()) {
            n += q;
        }
        return n;
    }
}

