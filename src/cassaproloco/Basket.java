package cassaproloco;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Modello dati che tiene traccia di Item singoli e GroupedItem.
 */
public class Basket implements Iterable<Map.Entry<Item, Integer>> {
    private JPanelBasket parent;
    private final Map<Item, Integer> items        = new HashMap<>();
    private final Map<GroupedItem, Integer> groups = new HashMap<>();
    private final Map<Item, Float> originalPrices  = new HashMap<>();

    public void setParent(JPanelBasket parent) {
        this.parent = parent;
    }

    public void clear() {
        items.clear();
        groups.clear();
        originalPrices.clear();
        notifyUI();
    }

    // --- Item methods ---

    public int addItem(Item i) {
        originalPrices.putIfAbsent(i, i.getprice());
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

    private void restorePrice(Item item) {
    if (originalPrices.containsKey(item)) {
        item.setPrice(originalPrices.get(item));
        originalPrices.remove(item);
    }
}

    public void removeItem(Item i) {
        restorePrice(i);     // <--- RIPRISTINA prezzo
        items.remove(i);
        notifyUI();
    }

    public void removeGroupedItem(GroupedItem gi) {
        restorePrice(gi.getMenu());   // <--- RIPRISTINA prezzo menu
        groups.remove(gi);
        notifyUI();
    }
    
    /*
    public void removeItem(Item i) {
        items.remove(i);
        originalPrices.remove(i);
        notifyUI();
    }
    */
    public int getItemQty(Item i) {
        return items.getOrDefault(i, 0);
    }

    public float getItemTotalPrice(Item i) {
        return i.getprice() * getItemQty(i);
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

    /*
    public void removeGroupedItem(GroupedItem gi) {
        groups.remove(gi);
        notifyUI();
    }
    */
    public int getGroupedItemQty(GroupedItem gi) {
        return groups.getOrDefault(gi, 0);
    }

    public float getGroupedItemTotalPrice(GroupedItem gi) {
        return gi.getMenu().getprice() * getGroupedItemQty(gi);
    }

    // --- Common ---

    /** Restituisce il totale sommando tutti gli Item e GroupedItem (menu). */
    public float getTotalPrice() {
        float sum = 0f;
        for (Map.Entry<Item, Integer> e : items.entrySet()) {
            sum += e.getKey().getprice() * e.getValue();
        }
        for (Map.Entry<GroupedItem, Integer> e : groups.entrySet()) {
            sum += e.getKey().getMenu().getprice() * e.getValue();
        }
        return sum;
    }

    public void setPricesToZero() {
        // 1) Azzera i singoli articoli
        for (Item item : items.keySet()) {
            originalPrices.putIfAbsent(item, item.getprice());
            item.setPrice(0f);
        }
        // 2) Azzera i menu dei GroupedItem
        for (GroupedItem gi : groups.keySet()) {
            Item menu = gi.getMenu();
            originalPrices.putIfAbsent(menu, menu.getprice());
            menu.setPrice(0f);
        }
        // 3) Notifica subito la UI
        notifyUI();
    }

    /** Ripristina prezzi originali e notifica la UI. */
    public void restorePrices() {
        for (Map.Entry<Item, Float> e : originalPrices.entrySet()) {
            e.getKey().setPrice(e.getValue());
        }
        originalPrices.clear();
        notifyUI();
    }

    
    public String getName(Item i) {
        return i.getText();
    }
    
    /** Notifica il JPanelBasket di aggiornare le righe e il totale */
    private void notifyUI() {
        if (parent != null) {
            parent.updateAll();
        }
    }

    @Override
    public Iterator<Map.Entry<Item, Integer>> iterator() {
        return items.entrySet().iterator();
    }

    /** Numero totale di righe (item + group) */
    public int size() {
        return items.size() + groups.size();
    }
}
