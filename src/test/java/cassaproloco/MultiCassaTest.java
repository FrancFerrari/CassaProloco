package cassaproloco;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Database comune fra più casse: nomi file per-cassa, copia nella cartella
 * condivisa, e resoconto che somma tutte le casse di una giornata.
 */
class MultiCassaTest {

    @TempDir
    Path tmp;

    @Test
    void nomeFilePerCassa() {
        assertEquals("report_2026-06-13.csv", SalesRecorder.reportFileName("2026-06-13", ""));
        assertEquals("report_2026-06-13.csv", SalesRecorder.reportFileName("2026-06-13", null));
        assertEquals("report_2026-06-13__cassa-1.csv", SalesRecorder.reportFileName("2026-06-13", "1"));
        assertEquals("report_2026-06-13__cassa-2.csv", SalesRecorder.reportFileName("2026-06-13", "2"));
    }

    @Test
    void scriveLocaleECopiaNellaCondivisa() throws IOException {
        File local = tmp.resolve("locale").toFile();
        File shared = tmp.resolve("condivisa").toFile();
        local.mkdirs();
        File csv = new File(local, SalesRecorder.reportFileName("2026-06-13", "1"));

        new SalesRecorder().append(csv, Arrays.asList(
                new String[]{"Data", "Nome", "Quantita", "PrezzoUnitario"},
                new String[]{"2026-06-13", "Birra", "2", "3.00"}), shared);

        assertTrue(csv.isFile(), "il file locale deve esistere");
        File copia = new File(shared, csv.getName());
        assertTrue(copia.isFile(), "il file deve essere copiato nella condivisa");
    }

    @Test
    void resocontoSommaLeDueCasse() throws IOException {
        File folder = tmp.toFile();
        write(new File(folder, "report_2026-06-13__cassa-1.csv"),
                "Data,Nome,Quantita,PrezzoUnitario",
                "2026-06-13,Birra,2,3.00");      // cassa 1: 6.00
        write(new File(folder, "report_2026-06-13__cassa-2.csv"),
                "Data,Nome,Quantita,PrezzoUnitario",
                "2026-06-13,Birra,1,3.00",       // cassa 2: 3.00
                "2026-06-13,Coca,4,2.50");       // cassa 2: 10.00

        SalesReportRepository repo = new SalesReportRepository();

        // la data compare una sola volta anche se ci sono due file
        List<String> dates = repo.availableDates(folder);
        assertEquals(Arrays.asList("2026-06-13"), dates);

        SalesReportRepository.Aggregate agg = repo.aggregateDate(folder, "2026-06-13");
        assertEquals(3, agg.byName.get("Birra")[0]);     // 2 + 1
        assertEquals(900, agg.byName.get("Birra")[1]);   // 6.00 + 3.00
        assertEquals(1000, agg.byName.get("Coca")[1]);   // 10.00
        assertEquals(1900, agg.totalCents);              // 19.00 totali fra le due casse
    }

    @Test
    void resocontoCompatibileColNomeVecchioSenzaCassa() throws IOException {
        File folder = tmp.toFile();
        write(new File(folder, "report_2026-06-13.csv"),   // formato singolo storico
                "Data,Nome,Quantita,PrezzoUnitario",
                "2026-06-13,Birra,5,3.00");

        SalesReportRepository.Aggregate agg =
                new SalesReportRepository().aggregateDate(folder, "2026-06-13");
        assertEquals(1500, agg.totalCents);
    }

    private void write(File f, String... lines) throws IOException {
        Files.write(f.toPath(), String.join("\n", lines).getBytes(StandardCharsets.UTF_8));
    }
}
