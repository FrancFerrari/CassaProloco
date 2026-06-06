package cassaproloco;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GroupedItemStoreTest {

    @TempDir
    Path tmp;

    private GroupedItem sampleMenu() {
        return new GroupedItem.Builder()
                .withMenu(new Item(1, 12f, "Menu completo", "Menu completo", 1))
                .withBeverage(new Item(0, 2.5f, "Coca", "Coca", 1))
                .withFirst(new Item(0, 6f, "Pasta", "Pasta al ragu", 1))
                .build();
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
        assertEquals("Pasta al ragu", loaded.get(0).getItem(GroupedItem.Course.FIRST).get().getTextToPrint());
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
}
