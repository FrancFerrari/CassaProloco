package cassaproloco;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class SalePlannerTest {

    private Item item(String text, int priceCents) {
        return new Item(priceCents, text, text);
    }

    private SalePlanner.Plan planOf(SalePlanner.Line line, Basket b, boolean csvExists) {
        return SalePlanner.plan(Collections.singletonList(line), b, "2025-08-22", csvExists);
    }

    @Test
    void articoloSingoloUnitoProduceUnSoloScontrino() {
        Basket b = new Basket();
        Item coca = item("Coca", 250);
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
        Item coca = item("Coca", 250);
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
    void menuEspandeIProdottiMaUnaSolaRigaCsv() {
        Basket b = new Basket();
        GroupedItem gi = new GroupedItem(item("Menu A", 1200),
                Arrays.asList(item("Coca", 250), item("Pasta", 600)));
        b.addGroupedItem(gi);

        SalePlanner.Plan plan = planOf(new SalePlanner.Line(true, null, gi, 1, true), b, true);

        // 2 prodotti = 2 scontrini separati (senza prezzo)
        assertEquals(2, plan.receipts.size());
        assertFalse(plan.receipts.get(0).isSingle());
        assertEquals("Coca", plan.receipts.get(0).name);
        assertEquals("Pasta", plan.receipts.get(1).name);
        // CSV: solo il menu principale, col suo prezzo
        assertEquals(1, plan.csvRows.size());
        assertArrayEquals(new String[]{"2025-08-22", "Menu A", "1", "12.00"}, plan.csvRows.get(0));
    }

    @Test
    void menuSeparatoProduceNCopieDiTuttiIProdotti() {
        Basket b = new Basket();
        GroupedItem gi = new GroupedItem(item("Menu A", 1200),
                Arrays.asList(item("Coca", 250), item("Pasta", 600)));
        b.addGroupedItem(gi);

        // qty 2, Separato -> 2 copie x 2 prodotti = 4 scontrini, 2 righe CSV
        SalePlanner.Plan plan = planOf(new SalePlanner.Line(true, null, gi, 2, false), b, true);

        assertEquals(4, plan.receipts.size());
        assertEquals(2, plan.csvRows.size());
        for (String[] row : plan.csvRows) {
            assertArrayEquals(new String[]{"2025-08-22", "Menu A", "1", "12.00"}, row);
        }
    }

    @Test
    void omaggioAzzeraIlPrezzoNelCsv() {
        Basket b = new Basket();
        Item coca = item("Coca", 250);
        b.addItem(coca);
        b.setPricesToZero();

        SalePlanner.Plan plan = planOf(new SalePlanner.Line(false, coca, null, 1, true), b, true);

        assertEquals("0.00", plan.csvRows.get(0)[3]);
    }

    @Test
    void intestazioneAggiuntaSeIlCsvNonEsiste() {
        Basket b = new Basket();
        Item coca = item("Coca", 250);
        b.addItem(coca);

        SalePlanner.Plan plan = planOf(new SalePlanner.Line(false, coca, null, 1, true), b, false);

        assertEquals(2, plan.csvRows.size());
        assertEquals("Data", plan.csvRows.get(0)[0]);
        assertEquals("Coca", plan.csvRows.get(1)[1]);
    }
}
