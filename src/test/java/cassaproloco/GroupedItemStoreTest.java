package cassaproloco;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GroupedItemStoreTest {

    @TempDir
    Path tmp;

    @Test
    void salvaERicaricaMantenendoIDati() throws IOException {
        File file = tmp.resolve("groupedItems.ser").toFile();
        GroupedItemStore store = new GroupedItemStore(file);

        GroupedItem menu = new GroupedItem.Builder()
                .withMenu(new Item(1, 12f, "Menu completo", "Menu completo", 1))
                .withBeverage(new Item(0, 2.5f, "Coca", "Coca", 1))
                .withFirst(new Item(0, 6f, "Pasta", "Pasta al ragu", 1))
                .build();

        store.save(Collections.singletonList(menu));
        List<GroupedItem> loaded = store.load();

        assertEquals(1, loaded.size());
        assertEquals(menu, loaded.get(0));
        assertEquals("Menu completo", loaded.get(0).getMenu().getText());
    }

    @Test
    void fileInesistenteRestituisceListaVuota() {
        File file = tmp.resolve("nope.ser").toFile();
        GroupedItemStore store = new GroupedItemStore(file);
        assertTrue(store.load().isEmpty());
    }
}
