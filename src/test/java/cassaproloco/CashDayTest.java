package cassaproloco;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * La giornata di cassa deve sopravvivere ai riavvii (stato su file) e cambiare
 * solo con la chiusura, non con il passare della mezzanotte.
 */
class CashDayTest {

    @TempDir
    Path tmp;

    private File f() {
        return tmp.resolve("cassa-giornata.txt").toFile();
    }

    @Test
    void appenaCreataNessunaGiornataAperta() {
        assertFalse(CashDay.load(f()).isOpen());
    }

    @Test
    void ensureOpenApreOggiEPersiste() {
        File file = f();
        LocalDate d = CashDay.load(file).ensureOpen();
        assertEquals(LocalDate.now(), d);
        assertTrue(file.isFile(), "lo stato deve essere salvato su file");

        // Riavvio: la giornata aperta viene ripresa
        CashDay reloaded = CashDay.load(file);
        assertTrue(reloaded.isOpen());
        assertEquals(d, reloaded.currentDate());
    }

    @Test
    void laDataNonCambiaTraPiuVendite() {
        File file = f();
        CashDay c = CashDay.load(file);
        c.setDate(LocalDate.of(2026, 6, 13)); // serata del 13
        // "passa la mezzanotte" e si riavvia: la giornata resta il 13
        CashDay afterRestart = CashDay.load(file);
        assertEquals(LocalDate.of(2026, 6, 13), afterRestart.currentDate());
    }

    @Test
    void chiusuraChiudeELaProssimaVenditaApreNuova() {
        File file = f();
        CashDay c = CashDay.load(file);
        c.setDate(LocalDate.of(2026, 6, 13));
        c.close();
        assertFalse(c.isOpen());

        // riavvio dopo chiusura: resta chiusa
        assertFalse(CashDay.load(file).isOpen());

        // la prossima vendita apre una nuova giornata (oggi)
        LocalDate next = CashDay.load(file).ensureOpen();
        assertEquals(LocalDate.now(), next);
    }

    @Test
    void overrideManualeDellaData() {
        File file = f();
        CashDay c = CashDay.load(file);
        c.ensureOpen();
        c.setDate(LocalDate.of(2026, 6, 13)); // correzione manuale
        assertEquals(LocalDate.of(2026, 6, 13), CashDay.load(file).currentDate());
    }
}
