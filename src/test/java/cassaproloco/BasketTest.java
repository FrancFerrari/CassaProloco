package cassaproloco;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test del modello dati Basket (carrello).
 * Non richiede la UI: Basket senza parent non notifica nulla.
 */
class BasketTest {

    private Item item(String text, float price) {
        // Item(int ID, float price, String text, String textToPrint, int qty)
        return new Item(0, price, text, text, 0);
    }

    @Test
    void carrelloVuotoHaTotaleZero() {
        Basket b = new Basket();
        assertEquals(0f, b.getTotalPrice());
        assertEquals(0, b.size());
    }

    @Test
    void aggiungereUnItemIncrementaLaQuantita() {
        Basket b = new Basket();
        Item coca = item("Coca Cola", 2.5f);

        assertEquals(1, b.addItem(coca));
        assertEquals(2, b.addItem(coca));
        assertEquals(2, b.getItemQty(coca));
    }

    @Test
    void totaleSommaPrezziPerQuantita() {
        Basket b = new Basket();
        Item coca = item("Coca Cola", 2.5f);
        Item birra = item("Birra", 3.5f);

        b.addItem(coca);
        b.addItem(coca);   // 2 x 2.5 = 5.0
        b.addItem(birra);  // 1 x 3.5 = 3.5

        assertEquals(8.5f, b.getTotalPrice(), 0.0001f);
    }

    @Test
    void sottrarreRiduceLaQuantitaEPulisceAZero() {
        Basket b = new Basket();
        Item coca = item("Coca Cola", 2.5f);

        b.addItem(coca);
        b.addItem(coca);
        assertEquals(1, b.subtractItem(coca));
        assertEquals(0, b.subtractItem(coca));
        assertEquals(0, b.getItemQty(coca));
        assertEquals(0f, b.getTotalPrice());
    }

    @Test
    void rimuovereItemLoEliminaDalCarrello() {
        Basket b = new Basket();
        Item coca = item("Coca Cola", 2.5f);

        b.addItem(coca);
        b.removeItem(coca);
        assertEquals(0, b.getItemQty(coca));
        assertEquals(0, b.size());
    }
}
