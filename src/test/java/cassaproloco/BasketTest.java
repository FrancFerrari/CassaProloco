package cassaproloco;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test del modello dati Basket (carrello). Prezzi in centesimi interi.
 * Non richiede la UI: Basket senza parent non notifica nulla.
 */
class BasketTest {

    private Item item(String text, int priceCents) {
        return new Item(0, priceCents, text, text, 0);
    }

    @Test
    void carrelloVuotoHaTotaleZero() {
        Basket b = new Basket();
        assertEquals(0, b.getTotalPrice());
        assertEquals(0, b.size());
    }

    @Test
    void aggiungereUnItemIncrementaLaQuantita() {
        Basket b = new Basket();
        Item coca = item("Coca Cola", 250);

        assertEquals(1, b.addItem(coca));
        assertEquals(2, b.addItem(coca));
        assertEquals(2, b.getItemQty(coca));
    }

    @Test
    void totaleSommaPrezziPerQuantita() {
        Basket b = new Basket();
        Item coca = item("Coca Cola", 250);
        Item birra = item("Birra", 350);

        b.addItem(coca);
        b.addItem(coca);   // 2 x 2.50 = 5.00
        b.addItem(birra);  // 1 x 3.50 = 3.50

        assertEquals(850, b.getTotalPrice()); // 8.50 € in centesimi
    }

    @Test
    void sottrarreRiduceLaQuantitaEPulisceAZero() {
        Basket b = new Basket();
        Item coca = item("Coca Cola", 250);

        b.addItem(coca);
        b.addItem(coca);
        assertEquals(1, b.subtractItem(coca));
        assertEquals(0, b.subtractItem(coca));
        assertEquals(0, b.getItemQty(coca));
        assertEquals(0, b.getTotalPrice());
    }

    @Test
    void rimuovereItemLoEliminaDalCarrello() {
        Basket b = new Basket();
        Item coca = item("Coca Cola", 250);

        b.addItem(coca);
        b.removeItem(coca);
        assertEquals(0, b.getItemQty(coca));
        assertEquals(0, b.size());
    }

    @Test
    void omaggioAzzeraIPrezziSenzaMutareGliItem() {
        Basket b = new Basket();
        Item coca = item("Coca Cola", 250);
        b.addItem(coca);
        b.addItem(coca);

        b.setPricesToZero();
        assertEquals(0, b.getTotalPrice());
        assertEquals(0, b.getItemTotalPrice(coca));
        // L'Item resta immutato: il suo prezzo "di listino" è ancora 250
        assertEquals(250, coca.getPriceCents());
        assertEquals(0, b.getEffectivePrice(coca));
    }

    @Test
    void ripristinoPrezziDopoOmaggio() {
        Basket b = new Basket();
        Item coca = item("Coca Cola", 250);
        b.addItem(coca);

        b.setPricesToZero();
        assertEquals(0, b.getTotalPrice());

        b.restorePrices();
        assertEquals(250, b.getTotalPrice());
        assertEquals(250, b.getEffectivePrice(coca));
    }

    @Test
    void itemUgualiSiFondonoComeChiaveDiMappa() {
        // equals/hashCode coerenti: due Item con stessi testo+prezzo sono la stessa voce
        Basket b = new Basket();
        b.addItem(item("Coca Cola", 250));
        b.addItem(item("Coca Cola", 250));
        assertEquals(1, b.size());
        assertEquals(2, b.getItemQty(item("Coca Cola", 250)));
    }
}
