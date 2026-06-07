package cassaproloco;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Trasforma le righe del carrello nel "piano di stampa": quali scontrini stampare
 * e quali righe scrivere nel CSV, applicando le regole Unito/Separato, l'espansione
 * dei menu nelle singole portate e i prezzi effettivi (omaggi inclusi).
 *
 * <p>È una funzione <b>pura</b> (nessuna UI, nessuna stampante): per questo è
 * facilmente testabile. La stampa vera e propria resta a {@code Cassa}
 * ({@code printOnce}/{@code printItem} + {@link ModelloStampa}, invariati).
 */
public final class SalePlanner {

    private static final List<GroupedItem.Course> COURSES = Arrays.asList(
            GroupedItem.Course.BEVERAGE, GroupedItem.Course.FIRST, GroupedItem.Course.SECOND,
            GroupedItem.Course.DESSERT, GroupedItem.Course.COFFEE);

    private SalePlanner() {
    }

    /** Una riga del carrello da stampare. */
    public static final class Line {
        public final boolean group;
        public final Item item;       // valorizzato se non è un menu
        public final GroupedItem gi;  // valorizzato se è un menu
        public final int qty;
        public final boolean unit;    // true = Unito, false = Separato

        public Line(boolean group, Item item, GroupedItem gi, int qty, boolean unit) {
            this.group = group;
            this.item = item;
            this.gi = gi;
            this.qty = qty;
            this.unit = unit;
        }
    }

    /** Un singolo scontrino da stampare. */
    public static final class Receipt {
        public final Item item;          // non-null: articolo singolo (printOnce)
        public final String name;        // non-null: portata di un menu (printItem)
        public final int qty;
        public final String courseLabel;

        private Receipt(Item item, String name, int qty, String courseLabel) {
            this.item = item;
            this.name = name;
            this.qty = qty;
            this.courseLabel = courseLabel;
        }

        public static Receipt single(Item item, int qty) {
            return new Receipt(item, null, qty, null);
        }

        public static Receipt course(String name, int qty, String courseLabel) {
            return new Receipt(null, name, qty, courseLabel);
        }

        public boolean isSingle() {
            return item != null;
        }
    }

    /** Risultato: scontrini da stampare + righe da scrivere nel CSV. */
    public static final class Plan {
        public final List<Receipt> receipts;
        public final List<String[]> csvRows;

        Plan(List<Receipt> receipts, List<String[]> csvRows) {
            this.receipts = receipts;
            this.csvRows = csvRows;
        }
    }

    /**
     * Calcola il piano di stampa.
     *
     * @param lines     righe del carrello
     * @param basket    per i prezzi effettivi (tiene conto degli omaggi)
     * @param dateStr   data odierna (colonna del CSV)
     * @param csvExists se il file CSV esiste già (per decidere se aggiungere l'intestazione)
     */
    public static Plan plan(List<Line> lines, Basket basket, String dateStr, boolean csvExists) {
        List<Receipt> receipts = new ArrayList<>();
        List<String[]> csv = new ArrayList<>();
        if (!csvExists) {
            csv.add(new String[] {"Data", "Nome", "Quantità", "PrezzoUnitario"});
        }

        for (Line l : lines) {
            if (l.qty <= 0) {
                continue;
            }
            int copies = l.unit ? 1 : l.qty;
            final int qtyPerCopy = l.unit ? l.qty : 1;

            for (int copy = 0; copy < copies; copy++) {
                if (!l.group) {
                    receipts.add(Receipt.single(l.item, qtyPerCopy));
                    csv.add(new String[] {
                            dateStr, l.item.getText(), String.valueOf(qtyPerCopy),
                            money(basket.getEffectivePrice(l.item))
                    });
                } else {
                    for (GroupedItem.Course course : COURSES) {
                        l.gi.getItem(course).ifPresent(it ->
                            receipts.add(Receipt.course(l.gi.getText(course), qtyPerCopy,
                                    course.name().toLowerCase())));
                    }
                    csv.add(new String[] {
                            dateStr, l.gi.getMenu().getText(), String.valueOf(qtyPerCopy),
                            money(basket.getEffectivePrice(l.gi.getMenu()))
                    });
                }
            }
        }
        return new Plan(receipts, csv);
    }

    /** Prezzo per il CSV: punto decimale, indipendente dal locale. */
    private static String money(float value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }
}
