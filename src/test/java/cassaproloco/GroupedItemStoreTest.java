package cassaproloco;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GroupedItemStoreTest {

    @TempDir
    Path tmp;

    private GroupedItem sampleMenu() {
        return new GroupedItem(
                new Item(1200, "Menu completo", "Menu completo"),
                Arrays.asList(
                        new Item(250, "Coca", "Coca"),
                        new Item(600, "Pasta", "Pasta al ragu")));
    }

    @Test
    void salvaERicaricaInJson() throws IOException {
        File json = tmp.resolve("groupedItems.json").toFile();
        GroupedItemStore store = new GroupedItemStore(json);

        GroupedItem menu = sampleMenu();
        store.save(Collections.singletonList(menu));

        assertTrue(json.exists(), "il file JSON deve essere creato");
        List<GroupedItem> loaded = store.load();
        assertEquals(1, loaded.size());
        assertEquals(menu, loaded.get(0));
        assertEquals("Menu completo", loaded.get(0).getMenu().getText());
        assertEquals(2, loaded.get(0).getComponents().size());
        assertEquals("Pasta al ragu", loaded.get(0).getComponents().get(1).getTextToPrint());
    }

    @Test
    void fileInesistenteRestituisceListaVuota() {
        File json = tmp.resolve("nope.json").toFile();
        assertTrue(new GroupedItemStore(json).load().isEmpty());
    }

    @Test
    void migraDalVecchioSerELoConverteInJson() throws IOException {
        // Scrive un legacy .ser usando le classi correnti (Serializable)
        File legacy = tmp.resolve("groupedItems.ser").toFile();
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(legacy))) {
            out.writeObject(Collections.singletonList(sampleMenu()));
        }

        File json = tmp.resolve("groupedItems.json").toFile();
        GroupedItemStore store = new GroupedItemStore(json, legacy);

        List<GroupedItem> loaded = store.load();
        assertEquals(1, loaded.size());
        assertEquals(sampleMenu(), loaded.get(0));
        assertTrue(json.exists(), "la migrazione deve aver creato il file JSON");
    }

    @Test
    void leggeIlVecchioFormatoJsonAPortate() throws IOException {
        // Vecchio JSON: mappa di portate (MENU + FIRST + BEVERAGE) e groupType.
        String legacyJson =
                "[\n" +
                "  {\n" +
                "    \"items\": {\n" +
                "      \"MENU\":     {\"id\":1,\"priceCents\":1200,\"text\":\"Menu A\",\"textToPrint\":\"Menu A\",\"qty\":1},\n" +
                "      \"FIRST\":    {\"id\":0,\"priceCents\":600,\"text\":\"Pasta\",\"textToPrint\":\"Pasta al ragu\",\"qty\":1},\n" +
                "      \"BEVERAGE\": {\"id\":0,\"priceCents\":250,\"text\":\"Coca\",\"textToPrint\":\"Coca\",\"qty\":1}\n" +
                "    },\n" +
                "    \"groupType\": \"FULL\"\n" +
                "  }\n" +
                "]\n";
        File json = tmp.resolve("groupedItems.json").toFile();
        try (Writer w = new OutputStreamWriter(new FileOutputStream(json), StandardCharsets.UTF_8)) {
            w.write(legacyJson);
        }

        List<GroupedItem> loaded = new GroupedItemStore(json).load();

        assertEquals(1, loaded.size());
        GroupedItem gi = loaded.get(0);
        assertEquals("Menu A", gi.getName());
        assertEquals(1200, gi.getPriceCents());
        // I prodotti vengono ricavati dalle portate presenti (ordine BEVERAGE, FIRST, ...).
        assertEquals(2, gi.getComponents().size());
        assertEquals("Coca", gi.getComponents().get(0).getText());
        assertEquals("Pasta", gi.getComponents().get(1).getText());
    }
}
