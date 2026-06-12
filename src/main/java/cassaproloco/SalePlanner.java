package cassaproloco;

import java.util.ArrayList;
import java.util.List;

/**
 * Trasforma le righe del carrello nel "piano di stampa": quali scontrini stampare
 * e quali righe scrivere nel CSV, applicando le regole Unito/Separato, l'espansione
 * dei menu nei singoli prodotti e i prezzi effettivi (omaggi inclusi).
 *
 * <p>Un "menu" ({@link GroupedItem}) viene espanso nei suoi prodotti: ognuno
 * produce uno scontrino separato (senza prezzo), mentre nel CSV finisce una sola
 * riga col nome e il prezzo del menu.
 *
 * <p>È una funzione <b>pura</b> (nessuna UI, nessuna stampante): per questo è
 * facilmente testabile. La stampa vera e propria resta a {@code Cassa}
 * ({@code printOnce}/{@code printItem} + {@link ReceiptModel}, invariati).
 */
public final class SalePlanner {

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
                    // Un menu: ogni prodotto del gruppo → uno scontrino separato (senza prezzo).
                    for (Item comp : l.gi.getComponents()) {
                        receipts.add(Receipt.course(comp.getText(), qtyPerCopy, "menu"));
                    }
                    // Nel CSV una sola riga: nome e prezzo del menu.
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
    private static String money(int cents) {
        return Money.formatRoot(cents);
    }
}
