package cassaproloco;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Round-trip delle preferenze: il formato rullino scelto viene salvato e
 * ritrovato al riavvio; in assenza del file si parte dal default (62 mm).
 */
class SettingsTest {

    @TempDir
    Path tmp;

    @Test
    void defaultE62mmSeIlFileNonEsiste() {
        File f = tmp.resolve("settings.properties").toFile();
        assertEquals(RollSize.MM62, Settings.load(f).getRollSize());
    }

    @Test
    void salvaERitrovaIlFormatoScelto() {
        File f = tmp.resolve("settings.properties").toFile();

        Settings s = Settings.load(f);
        s.setRollSize(RollSize.MM54);
        assertTrue(f.exists(), "il file delle impostazioni deve essere creato");

        // Nuova istanza: deve rileggere il 54 mm dal file
        assertEquals(RollSize.MM54, Settings.load(f).getRollSize());
    }

    @Test
    void cambioFormatoVienePersistito() {
        File f = tmp.resolve("settings.properties").toFile();
        Settings.load(f).setRollSize(RollSize.MM54);
        Settings.load(f).setRollSize(RollSize.MM62);
        assertEquals(RollSize.MM62, Settings.load(f).getRollSize());
    }
}
