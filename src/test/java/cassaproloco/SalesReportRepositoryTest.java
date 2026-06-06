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

class SalesReportRepositoryTest {

    @TempDir
    Path tmp;

    private File writeCsv(String name, String content) throws IOException {
        File f = tmp.resolve(name).toFile();
        Files.write(f.toPath(), content.getBytes(StandardCharsets.UTF_8));
        return f;
    }

    @Test
    void aggregaQuantitaEIncassoPerNome() throws IOException {
        File csv = writeCsv("report_2025-08-22.csv", String.join("\n",
                "Data,Nome,Quantita,PrezzoUnitario",
                "2025-08-22,Coca,2,2.50",
                "2025-08-22,Coca,1,2.50",
                "2025-08-22,Birra,3,3.00"));

        SalesReportRepository repo = new SalesReportRepository();
        SalesReportRepository.Aggregate agg = repo.aggregate(csv);

        assertEquals(3, agg.byName.get("Coca")[0]);          // quantità totale Coca
        assertEquals(750, agg.byName.get("Coca")[1]);        // incasso Coca in centesimi
        assertEquals(900, agg.byName.get("Birra")[1]);
        assertEquals(1650, agg.totalCents);
        assertEquals(16.5, agg.getTotalEuro(), 0.0001);
    }

    @Test
    void elencaSoloLeDateDeiFileReport() throws IOException {
        writeCsv("report_2025-08-22.csv", "Data,Nome,Quantita,PrezzoUnitario\n");
        writeCsv("report_2025-08-20.csv", "Data,Nome,Quantita,PrezzoUnitario\n");
        writeCsv("altro.csv", "rumore\n");

        List<String> dates = new SalesReportRepository().availableDates(tmp.toFile());

        assertEquals(Arrays.asList("2025-08-20", "2025-08-22"), dates); // ordinate
    }

    @Test
    void scritturaELetturaConNomeContenenteVirgola() throws IOException {
        // Fix end-to-end: un nome con virgola non deve rompere le colonne
        File csv = tmp.resolve("report_2025-08-22.csv").toFile();
        SalesRecorder recorder = new SalesRecorder();
        recorder.append(csv, Arrays.asList(
                new String[]{"Data", "Nome", "Quantita", "PrezzoUnitario"},
                new String[]{"2025-08-22", "Panino, salsiccia", "2", "4.00"}));

        SalesReportRepository.Aggregate agg = new SalesReportRepository().aggregate(csv);

        assertEquals(2, agg.byName.get("Panino, salsiccia")[0]);
        assertEquals(800, agg.totalCents);
    }
}
