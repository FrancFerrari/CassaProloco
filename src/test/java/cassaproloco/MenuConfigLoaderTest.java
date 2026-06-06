package cassaproloco;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MenuConfigLoaderTest {

    @TempDir
    Path tmp;

    private File writeCfg(String content) throws IOException {
        File f = tmp.resolve("test.cfg").toFile();
        Files.write(f.toPath(), content.getBytes(StandardCharsets.UTF_8));
        return f;
    }

    @Test
    void caricaVociValideSaltandoIntestazione() throws IOException {
        File cfg = writeCfg(String.join("\n",
                "intestazione;da;saltare",
                "Coca Cola;Coca-cola alla spina;2.5",
                "Birra;Birra alla spina;3.5"));

        List<Item> items = MenuConfigLoader.load(cfg);

        assertEquals(2, items.size());
        assertEquals("Coca Cola", items.get(0).getText());
        assertEquals("Coca-cola alla spina", items.get(0).getTextToPrint());
        assertEquals(2.5f, items.get(0).getprice(), 0.0001f);
        assertEquals(3.5f, items.get(1).getprice(), 0.0001f);
    }

    @Test
    void ignoraRigheMalformateOConPrezzoNonNumerico() throws IOException {
        File cfg = writeCfg(String.join("\n",
                "header;x;y",
                "Solo due campi;senza prezzo",
                "Prezzo invalido;testo;abc",
                "Valido;testo;1.0"));

        List<Item> items = MenuConfigLoader.load(cfg);

        assertEquals(1, items.size());
        assertEquals("Valido", items.get(0).getText());
    }
}
