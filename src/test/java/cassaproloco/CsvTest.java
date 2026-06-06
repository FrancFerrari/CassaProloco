package cassaproloco;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvTest {

    @Test
    void campiSempliciNonVengonoQuotati() {
        assertEquals("Coca,2,2.50", Csv.toLine(new String[]{"Coca", "2", "2.50"}));
    }

    @Test
    void nomiConVirgolaVengonoQuotati() {
        String line = Csv.toLine(new String[]{"Panino, salsiccia", "1", "4.00"});
        assertEquals("\"Panino, salsiccia\",1,4.00", line);
    }

    @Test
    void roundTripConVirgoleEVirgolette() {
        String[] row = {"Vino \"rosso\", 1L", "3", "7.00"};
        List<String> parsed = Csv.parseLine(Csv.toLine(row));
        assertEquals("Vino \"rosso\", 1L", parsed.get(0));
        assertEquals("3", parsed.get(1));
        assertEquals("7.00", parsed.get(2));
    }

    @Test
    void parseDiRigaSemplice() {
        List<String> f = Csv.parseLine("2025-08-22,Birra,3,3.00");
        assertEquals(4, f.size());
        assertEquals("Birra", f.get(1));
        assertEquals("3.00", f.get(3));
    }
}
