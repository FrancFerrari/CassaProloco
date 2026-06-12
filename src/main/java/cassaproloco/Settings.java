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
 * Preferenze dell'applicazione persistite in un piccolo file
 * {@code settings.properties} accanto agli altri dati.
 *
 * <p>Per ora contiene solo il formato del rullino di stampa scelto, così che
 * l'operatore lo imposti una volta e resti memorizzato anche dopo il riavvio.
 * In assenza del file (o di valori validi) si parte dai valori predefiniti.
 */
final class Settings {

    private static final Logger LOG = Logger.getLogger(Settings.class.getName());
    private static final String KEY_ROLL = "roll.size";

    private final File file;
    private RollSize rollSize = RollSize.MM62;

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

    private void save() {
        Properties p = new Properties();
        p.setProperty(KEY_ROLL, rollSize.name());
        try (OutputStream out = new FileOutputStream(file)) {
            p.store(out, "CassaProloco - preferenze");
        } catch (IOException ex) {
            LOG.log(Level.WARNING, "Impostazioni non salvate", ex);
        }
    }
}
