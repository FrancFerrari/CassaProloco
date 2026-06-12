package cassaproloco;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Il template predefinito deve descrivere lo scontrino storico, e il salvataggio
 * deve fare round-trip (così le personalizzazioni si ritrovano al riavvio).
 */
class ReceiptTemplateTest {

    @TempDir
    Path tmp;

    @Test
    void defaultHaGliElementiStorici() {
        ReceiptTemplate t = ReceiptTemplate.defaultTemplate();

        ReceiptElement h1 = t.find(ReceiptElement.Kind.HEADER1);
        assertNotNull(h1);
        assertEquals("Antica Sagra di S.Luigi ", h1.text);
        assertTrue(h1.bold);
        assertEquals(8, h1.fontSize);

        // il prezzo è ancorato a destra (si sposta col rullino)
        assertEquals(ReceiptElement.Anchor.RIGHT, t.find(ReceiptElement.Kind.PRICE).anchor);
        // la voce è centrata su tutta la larghezza
        ReceiptElement item = t.find(ReceiptElement.Kind.ITEM);
        assertEquals(ReceiptElement.Align.CENTER, item.align);
        assertEquals(ReceiptElement.WIDTH_FULL, item.width);

        // riga in fondo e logo presenti ma nascosti: default invariato
        assertFalse(t.find(ReceiptElement.Kind.FOOTER).visible);
        assertFalse(t.find(ReceiptElement.Kind.LOGO).visible);
    }

    @Test
    void storeSalvaERicarica() throws Exception {
        File f = tmp.resolve("receiptTemplate.json").toFile();
        ReceiptTemplateStore store = new ReceiptTemplateStore(f);

        ReceiptTemplate t = ReceiptTemplate.defaultTemplate();
        t.find(ReceiptElement.Kind.HEADER1).text = "Festa della Birra";
        t.find(ReceiptElement.Kind.FOOTER).visible = true;
        store.save(t);

        assertTrue(f.exists());
        ReceiptTemplate loaded = store.load();
        assertEquals("Festa della Birra", loaded.find(ReceiptElement.Kind.HEADER1).text);
        assertTrue(loaded.find(ReceiptElement.Kind.FOOTER).visible);
        // gli enum si serializzano/rileggono correttamente
        assertEquals(ReceiptElement.Anchor.RIGHT, loaded.find(ReceiptElement.Kind.PRICE).anchor);
    }

    @Test
    void fileAssenteDaIlPredefinito() {
        File f = tmp.resolve("nope.json").toFile();
        ReceiptTemplate t = new ReceiptTemplateStore(f).load();
        assertEquals("Antica Sagra di S.Luigi ", t.find(ReceiptElement.Kind.HEADER1).text);
    }
}
