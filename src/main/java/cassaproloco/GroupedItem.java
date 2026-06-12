package cassaproloco;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Un "menu" = un gruppo di prodotti venduti insieme a un <b>prezzo unico</b>, ma
 * <b>stampati singolarmente</b>: alla stampa, ogni prodotto del gruppo produce
 * uno scontrino separato (tagliando per la cucina, senza prezzo), mentre il
 * prezzo del menu viene registrato una sola volta nel resoconto/CSV.
 *
 * <p>{@code menu} è la "testata" del gruppo (nome + prezzo combinato);
 * {@code components} sono i prodotti che lo compongono, in ordine, pescati dal
 * listino. A differenza del vecchio modello a portate fisse (primo/secondo/
 * bevanda/dolce/caffè), i prodotti sono un <b>elenco libero</b> di lunghezza
 * qualsiasi: si possono mettere due primi, tre bibite, ecc.
 *
 * <p>Implementa {@link Serializable} solo per compatibilità con la vecchia
 * migrazione {@code .ser}; la persistenza corrente è in JSON
 * ({@link GroupedItemStore}), che sa leggere anche il vecchio formato a portate.
 */
public class GroupedItem implements Serializable {
    private static final long serialVersionUID = 2L;

    private final Item menu;
    private final List<Item> components;

    public GroupedItem(Item menu, List<Item> components) {
        this.menu = Objects.requireNonNull(menu, "menu");
        this.components = new ArrayList<>(components == null ? Collections.emptyList() : components);
    }

    /** Testata del gruppo: nome + prezzo combinato. */
    public Item getMenu() {
        return menu;
    }

    /** Nome del menu (mostrato sul pulsante e nel carrello). */
    public String getName() {
        return menu.getText();
    }

    /** Prezzo combinato del menu, in centesimi. */
    public int getPriceCents() {
        return menu.getPriceCents();
    }

    /** Prodotti che compongono il menu, stampati uno per uno (lista immutabile). */
    public List<Item> getComponents() {
        return Collections.unmodifiableList(components);
    }

    @Override
    public String toString() {
        return getName() + " (" + components.size() + " prodotti)";
    }

    @Override
    public int hashCode() {
        return Objects.hash(menu, components);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof GroupedItem)) return false;
        GroupedItem other = (GroupedItem) obj;
        return Objects.equals(menu, other.menu)
            && Objects.equals(components, other.components);
    }
}
