package cassaproloco;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Preferenze <b>locali</b> di questo PC, persistite in {@code settings.properties}
 * nella cartella dati locale (quindi diverse per ogni cassa).
 *
 * <p>Contiene il formato del rullino di stampa, l'<b>ID di questa cassa</b> (per
 * distinguere i file delle vendite quando ci sono più casse) e il percorso della
 * <b>cartella condivisa</b> dove copiare le vendite e da cui leggere il resoconto
 * combinato. In assenza del file (o di valori validi) si parte dai predefiniti
 * (cassa singola: nessun ID, nessuna cartella condivisa).
 */
final class Settings {

    private static final Logger LOG = Logger.getLogger(Settings.class.getName());
    private static final String KEY_ROLL = "roll.size";
    private static final String KEY_CASSA_ID = "cassa.id";
    private static final String KEY_SHARED_DIR = "shared.dir";

    private final File file;
    private RollSize rollSize = RollSize.MM62;
    private String cassaId = "";
    private String sharedDir = "";

    private Settings(File file) {
        this.file = file;
    }

    /** Carica le preferenze dal file (valori predefiniti se assente o illeggibile). */
    static Settings load(File file) {
        Settings s = new Settings(file);
        if (file.exists()) {
            Properties p = new Properties();
            try (InputStream in = new FileInputStream(file)) {
                p.load(in);
                s.rollSize = RollSize.fromName(p.getProperty(KEY_ROLL));
                s.cassaId = clean(p.getProperty(KEY_CASSA_ID));
                s.sharedDir = p.getProperty(KEY_SHARED_DIR, "").trim();
            } catch (IOException ex) {
                LOG.log(Level.WARNING, "Impostazioni non caricate, uso i valori predefiniti", ex);
            }
        }
        return s;
    }

    RollSize getRollSize() {
        return rollSize;
    }

    /** Imposta il formato rullino e lo salva subito su file. */
    void setRollSize(RollSize rollSize) {
        if (rollSize != null && rollSize != this.rollSize) {
            this.rollSize = rollSize;
            save();
        }
    }

    /** ID di questa cassa (es. "1"); stringa vuota = cassa singola. */
    String getCassaId() {
        return cassaId;
    }

    /** Cartella condivisa dove copiare le vendite e leggere il resoconto combinato; null se non impostata. */
    File getSharedDir() {
        return sharedDir.isEmpty() ? null : new File(sharedDir);
    }

    String getSharedDirPath() {
        return sharedDir;
    }

    /** Imposta in un colpo solo ID cassa, rullino e cartella condivisa, e salva. */
    void update(String cassaId, RollSize rollSize, String sharedDir) {
        this.cassaId = clean(cassaId);
        this.rollSize = rollSize != null ? rollSize : this.rollSize;
        this.sharedDir = sharedDir == null ? "" : sharedDir.trim();
        save();
    }

    /** ID ripulito: solo caratteri sicuri per un nome file (lettere, cifre, - e _). */
    private static String clean(String id) {
        if (id == null) {
            return "";
        }
        return id.trim().replaceAll("[^A-Za-z0-9_-]", "");
    }

    private void save() {
        Properties p = new Properties();
        p.setProperty(KEY_ROLL, rollSize.name());
        p.setProperty(KEY_CASSA_ID, cassaId);
        p.setProperty(KEY_SHARED_DIR, sharedDir);
        try (OutputStream out = new FileOutputStream(file)) {
            p.store(out, "CassaProloco - preferenze locali");
        } catch (IOException ex) {
            LOG.log(Level.WARNING, "Impostazioni non salvate", ex);
        }
    }
}
