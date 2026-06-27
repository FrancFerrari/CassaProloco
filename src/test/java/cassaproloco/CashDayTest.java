package cassaproloco;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;

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
        LocalDate expected = CashDay.businessDate(LocalDateTime.now());
        LocalDate d = CashDay.load(file).ensureOpen();
        assertEquals(expected, d);
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

        // la prossima vendita apre una nuova giornata (data commerciale di adesso)
        LocalDate expected = CashDay.businessDate(LocalDateTime.now());
        LocalDate next = CashDay.load(file).ensureOpen();
        assertEquals(expected, next);
    }

    @Test
    void taglioNotturnoPrimaDelleSeiContaIlGiornoPrima() {
        // sera del 13 -> 13
        assertEquals(LocalDate.of(2026, 6, 13), CashDay.businessDate(LocalDateTime.of(2026, 6, 13, 23, 0)));
        // 1 di notte del 14 -> resta sul 13 (serata che sfora la mezzanotte)
        assertEquals(LocalDate.of(2026, 6, 13), CashDay.businessDate(LocalDateTime.of(2026, 6, 14, 1, 0)));
        // 5:59 -> ancora il 13
        assertEquals(LocalDate.of(2026, 6, 13), CashDay.businessDate(LocalDateTime.of(2026, 6, 14, 5, 59)));
        // 6:00 in punto -> giorno nuovo (14)
        assertEquals(LocalDate.of(2026, 6, 14), CashDay.businessDate(LocalDateTime.of(2026, 6, 14, 6, 0)));
        // mattina -> 14
        assertEquals(LocalDate.of(2026, 6, 14), CashDay.businessDate(LocalDateTime.of(2026, 6, 14, 10, 0)));
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
