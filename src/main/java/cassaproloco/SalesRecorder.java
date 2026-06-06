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
 * <p>Le righe sono codificate con {@link Csv} (RFC 4180): nomi con virgole o
 * virgolette vengono correttamente quotati.
 */
public class SalesRecorder {

    /** Appende le righe al file CSV (ogni riga è un array di colonne). */
    public void append(File file, List<String[]> rows) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(file, true))) {
            for (String[] row : rows) {
                pw.println(Csv.toLine(row));
            }
        }
    }
}
