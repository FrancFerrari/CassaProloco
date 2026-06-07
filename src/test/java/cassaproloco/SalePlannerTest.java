package cassaproloco;

import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class SalePlannerTest {

    private Item item(String text, float price) {
        return new Item(0, price, text, text, 0);
    }

    private SalePlanner.Plan planOf(SalePlanner.Line line, Basket b, boolean csvExists) {
        return SalePlanner.plan(Collections.singletonList(line), b, "2025-08-22", csvExists);
    }

    @Test
    void articoloSingoloUnitoProduceUnSoloScontrino() {
        Basket b = new Basket();
        Item coca = item("Coca", 2.5f);
        b.addItem(coca);
        b.addItem(coca); // qty 2

        SalePlanner.Plan plan = planOf(new SalePlanner.Line(false, coca, null, 2, true), b, true);

        assertEquals(1, plan.receipts.size());
        assertTrue(plan.receipts.get(0).isSingle());
        assertEquals(2, plan.receipts.get(0).qty);
        // csvExists=true -> niente intestazione, solo la riga vendita
        assertEquals(1, plan.csvRows.size());
        assertArrayEquals(new String[]{"2025-08-22", "Coca", "2", "2.50"}, plan.csvRows.get(0));
    }

    @Test
    void articoloSingoloSeparatoProduceNScontriniDaUno() {
        Basket b = new Basket();
        Item coca = item("Coca", 2.5f);
        b.addItem(coca);
        b.addItem(coca);
        b.addItem(coca); // qty 3

        SalePlanner.Plan plan = planOf(new SalePlanner.Line(false, coca, null, 3, false), b, true);

        assertEquals(3, plan.receipts.size());
        assertEquals(3, plan.csvRows.size());
        for (String[] row : plan.csvRows) {
            assertArrayEquals(new String[]{"2025-08-22", "Coca", "1", "2.50"}, row);
        }
    }

    @Test
    void menuEspandeLePortateMaUnaSolaRigaCsv() {
        Basket b = new Basket();
        GroupedItem gi = new GroupedItem.Builder()
                .withMenu(item("Menu A", 12f))
                .withBeverage(item("Coca", 2.5f))
                .withFirst(item("Pasta", 6f))
                .build();
        b.addGroupedItem(gi);

        SalePlanner.Plan plan = planOf(new SalePlanner.Line(true, null, gi, 1, true), b, true);

        // bevanda + primo = 2 scontrini di portata
        assertEquals(2, plan.receipts.size());
        assertFalse(plan.receipts.get(0).isSingle());
        // CSV: solo il menu principale
        assertEquals(1, plan.csvRows.size());
        assertArrayEquals(new String[]{"2025-08-22", "Menu A", "1", "12.00"}, plan.csvRows.get(0));
    }

    @Test
    void omaggioAzzeraIlPrezzoNelCsv() {
        Basket b = new Basket();
        Item coca = item("Coca", 2.5f);
        b.addItem(coca);
        b.setPricesToZero();

        SalePlanner.Plan plan = planOf(new SalePlanner.Line(false, coca, null, 1, true), b, true);

        assertEquals("0.00", plan.csvRows.get(0)[3]);
    }

    @Test
    void intestazioneAggiuntaSeIlCsvNonEsiste() {
        Basket b = new Basket();
        Item coca = item("Coca", 2.5f);
        b.addItem(coca);

        SalePlanner.Plan plan = planOf(new SalePlanner.Line(false, coca, null, 1, true), b, false);

        assertEquals(2, plan.csvRows.size());
        assertEquals("Data", plan.csvRows.get(0)[0]);
        assertEquals("Coca", plan.csvRows.get(1)[1]);
    }
}
