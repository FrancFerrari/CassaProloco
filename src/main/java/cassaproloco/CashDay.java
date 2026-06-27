package cassaproloco;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Giornata di cassa = la sessione di vendita di una serata. <b>Non</b> cambia a
 * mezzanotte: cambia solo quando l'operatore preme "Chiusura cassa". Così le
 * vendite di una serata che prosegue dopo mezzanotte restano tutte sulla stessa
 * giornata (e nello stesso file CSV).
 *
 * <p>Lo stato (data della giornata aperta, oppure nessuna) è persistito su un
 * piccolo file di testo. Se il programma viene riavviato durante la serata, la
 * giornata aperta viene ripresa. La scrittura è <b>atomica</b> (file temporaneo +
 * rename) per non lasciare uno stato a metà in caso di crash.
 */
class CashDay {

    private static final Logger LOG = Logger.getLogger(CashDay.class.getName());

    /**
     * Ora di "taglio" della giornata: le vendite prima di quest'ora contano come
     * il giorno precedente (una serata che sfora la mezzanotte resta sul giorno
     * della serata). Le serate finiscono ben prima, quindi è una soglia sicura.
     */
    static final LocalTime MORNING_CUTOFF = LocalTime.of(6, 0);

    private final File file;
    private LocalDate date; // null = nessuna giornata aperta

    private CashDay(File file, LocalDate date) {
        this.file = file;
        this.date = date;
    }

    /** Carica lo stato persistito; giornata chiusa se assente o illeggibile. */
    static CashDay load(File file) {
        LocalDate d = null;
        if (file.isFile()) {
            try {
                String s = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8).trim();
                if (!s.isEmpty()) {
                    d = LocalDate.parse(s);
                }
            } catch (Exception ex) {
                LOG.log(Level.WARNING, "Stato giornata illeggibile: la considero chiusa", ex);
            }
        }
        return new CashDay(file, d);
    }

    boolean isOpen() {
        return date != null;
    }

    /** Data della giornata aperta, o {@code null} se chiusa. */
    LocalDate currentDate() {
        return date;
    }

    /**
     * Garantisce una giornata aperta: se è chiusa, ne apre una con la data
     * odierna e la salva. Ritorna la data della giornata corrente.
     */
    LocalDate ensureOpen() {
        if (date == null) {
            setDate(businessDate(LocalDateTime.now()));
        }
        return date;
    }

    /**
     * Data "commerciale" per l'istante indicato: prima delle {@link #MORNING_CUTOFF}
     * conta come il giorno precedente. Così, anche dopo una chiusura, una vendita
     * fatta all'1 di notte resta sul giorno della serata e non salta al giorno dopo.
     */
    static LocalDate businessDate(LocalDateTime when) {
        if (when.toLocalTime().isBefore(MORNING_CUTOFF)) {
            return when.toLocalDate().minusDays(1);
        }
        return when.toLocalDate();
    }

    /** Chiude la giornata corrente: la prossima vendita ne aprirà una nuova. */
    void close() {
        date = null;
        try {
            Files.deleteIfExists(file.toPath());
        } catch (IOException ex) {
            LOG.log(Level.WARNING, "Impossibile cancellare lo stato giornata", ex);
        }
    }

    /**
     * Forza la data della giornata aperta. Sicurezza per i casi limite (es. se la
     * prima vendita avviene per errore dopo mezzanotte e va riportata al giorno
     * prima).
     */
    void setDate(LocalDate d) {
        this.date = d;
        if (d == null) {
            close();
            return;
        }
        try {
            File tmp = new File(file.getParentFile(), file.getName() + ".tmp");
            Files.write(tmp.toPath(), d.toString().getBytes(StandardCharsets.UTF_8));
            try {
                Files.move(tmp.toPath(), file.toPath(),
                        StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (Exception atomicNotSupported) {
                Files.move(tmp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ex) {
            LOG.log(Level.WARNING, "Impossibile salvare lo stato giornata", ex);
        }
    }
}
