package cassaproloco;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Scrive le righe di vendita in append su un file CSV giornaliero, <b>in locale</b>
 * (così la vendita non si perde mai), e poi ne <b>copia</b> una versione nella
 * cartella condivisa, se impostata (per il resoconto combinato fra più casse).
 *
 * <p>Con più casse il nome del file include l'ID cassa
 * ({@code report_AAAA-MM-GG__cassa-1.csv}) così due casse non scrivono mai lo
 * stesso file. La copia nella condivisa è <b>best-effort</b>: se la rete non è
 * raggiungibile, i dati restano salvati in locale e verranno ricopiati alla
 * vendita successiva.
 */
public class SalesRecorder {

    private static final Logger LOG = Logger.getLogger(SalesRecorder.class.getName());

    /** Nome del file CSV per la data e l'ID cassa indicati (nessun suffisso se ID vuoto). */
    public static String reportFileName(String dateStr, String cassaId) {
        if (cassaId == null || cassaId.trim().isEmpty()) {
            return "report_" + dateStr + ".csv";
        }
        return "report_" + dateStr + "__cassa-" + cassaId.trim() + ".csv";
    }

    /** Appende le righe al solo file locale (nessuna cartella condivisa). */
    public void append(File file, List<String[]> rows) throws IOException {
        append(file, rows, null);
    }

    /** Appende le righe al file locale e copia nella condivisa (se {@code sharedDir} non è null). */
    public void append(File file, List<String[]> rows, File sharedDir) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(file, true))) {
            for (String[] row : rows) {
                pw.println(Csv.toLine(row));
            }
        }
        copyToShared(file, sharedDir);
    }

    /** Copia best-effort del file vendite nella cartella condivisa. */
    private void copyToShared(File localFile, File sharedDir) {
        if (sharedDir == null) {
            return;
        }
        try {
            if (!sharedDir.isDirectory() && !sharedDir.mkdirs()) {
                LOG.log(Level.WARNING, "Cartella condivisa non raggiungibile: {0}", sharedDir);
                return;
            }
            Files.copy(localFile.toPath(), new File(sharedDir, localFile.getName()).toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception ex) {
            // la vendita è già salvata in locale: la copia si ritenterà alla prossima
            LOG.log(Level.WARNING, "Copia vendite nella cartella condivisa non riuscita", ex);
        }
    }
}
