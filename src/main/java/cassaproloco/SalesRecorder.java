package cassaproloco;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Scrive le righe di vendita in append su un file CSV giornaliero
 * ({@code report_AAAA-MM-GG.csv}).
 *
 * <p>Fase 1: comportamento di scrittura identico a prima (separatore virgola).
 * L'irrobustimento (locale fisso + escaping) arriva in fase 2.
 */
public class SalesRecorder {

    /** Appende le righe al file CSV (ogni riga è un array di colonne). */
    public void append(File file, List<String[]> rows) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(file, true))) {
            for (String[] row : rows) {
                pw.println(String.join(",", row));
            }
        }
    }
}
