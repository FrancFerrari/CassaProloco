package cassaproloco;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/*
 * Rappresenta un raggruppamento di portate (menu, bevanda, primi, secondi) 
 * con un tipo specifico di menu.
 */
public class GroupedItem implements Serializable {
    private static final long serialVersionUID = 1L;

    /* Tipi di portata nel gruppo */
    public enum Course {
        MENU, BEVERAGE, FIRST, SECOND, DESSERT, COFFEE;
    }

    /* Possibili tipi di menu (reemplazare o estendere secondo necessità) */
    public enum GroupType {
        FULL(0),
        PARTIAL(1),
        CUSTOM(2);

        private final int id;
        GroupType(int id) { this.id = id; }
        public int getId() { return id; }
        public static Optional<GroupType> fromId(int id) {
            for (GroupType gt : values()) if (gt.id == id) return Optional.of(gt);
            return Optional.empty();
        }
    }

    private final Map<Course, Item> items;
    private final GroupType groupType;

    private GroupedItem(Map<Course, Item> items, GroupType groupType) {
        this.items = new EnumMap<>(Objects.requireNonNull(items));
        this.groupType = Objects.requireNonNull(groupType);
    }

    /* Builder per creare in modo fluido un GroupedItem */
    public static class Builder {
        private final Map<Course, Item> items = new EnumMap<>(Course.class);
        private GroupType groupType = GroupType.FULL;

        public Builder withMenu(Item menu) {
            items.put(Course.MENU, Objects.requireNonNull(menu));
            return this;
        }
        public Builder withBeverage(Item bev) {
            items.put(Course.BEVERAGE, bev);
            return this;
        }
        public Builder withFirst(Item first) {
            items.put(Course.FIRST, first);
            return this;
        }
        public Builder withSecond(Item second) {
            items.put(Course.SECOND, second);
            return this;
        }
        public Builder withDessert(Item dessert) {
            items.put(Course.DESSERT, Objects.requireNonNull(dessert));
            return this;
        }
        public Builder withCoffee(Item coffee) {
            items.put(Course.COFFEE, Objects.requireNonNull(coffee));
            return this;
        }
        
        public Builder ofType(GroupType type) {
            this.groupType = Objects.requireNonNull(type);
            return this;
        }
        public GroupedItem build() {
            if (!items.containsKey(Course.MENU)) {
                throw new IllegalStateException("Menu item is mandatory");
            }
            return new GroupedItem(items, groupType);
        }
    }

    /* Recupera l'item per il corso specificato */
    public Optional<Item> getItem(Course course) {
        return Optional.ofNullable(items.get(course));
    }

    /* Quantità dell'item per il corso */
    public int getQty(Course course) {
        return getItem(course).map(Item::getQty).orElse(0);
    }

    /* Prezzo (in centesimi) dell'item per il corso */
    public int getPriceCents(Course course) {
        return getItem(course).map(Item::getPriceCents).orElse(0);
    }

    /* Testo descrittivo dell'item per il corso */
    public String getText(Course course) {
        return getItem(course).map(Item::getText).orElse("");
    }

    /* Tipo di menu */
    public GroupType getType() {
        return groupType;
    }

    /* Ritorna l'item associato al menu principale */
    public Item getMenu() {
        return items.get(Course.MENU);
    }

    /* Ritorna tutti gli item */
    public Map<Course, Item> getAllItems() {
        return new EnumMap<>(items);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        items.forEach((course, item) -> sb.append(course)
                                          .append(": ")
                                          .append(item)
                                          .append("\n"));
        sb.append("Type: ").append(groupType).append(" (id=").append(groupType.getId()).append(")");
        return sb.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(items, groupType);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof GroupedItem)) return false;
        GroupedItem other = (GroupedItem) obj;
        return Objects.equals(items, other.items)
            && groupType == other.groupType;
    }
}
