package cassaproloco;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Persistenza dei menu combinati ({@link GroupedItem}).
 *
 * <p>Fase 1: usa la serializzazione Java (file {@code .ser}), incapsulando l'IO
 * che prima era sparso in {@code Cassa}. In fase 2 verrà sostituita da JSON.
 */
public class GroupedItemStore {

    private final File file;

    public GroupedItemStore(File file) {
        this.file = file;
    }

    /** Carica la lista salvata; lista vuota se il file non esiste o è illeggibile. */
    @SuppressWarnings("unchecked")
    public List<GroupedItem> load() {
        if (!file.exists()) {
            return new ArrayList<>();
        }
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            Object obj = in.readObject();
            if (obj instanceof List) {
                return new ArrayList<>((List<GroupedItem>) obj);
            }
        } catch (IOException | ClassNotFoundException ex) {
            System.out.println("GroupedItem non caricati: " + ex.getMessage());
        }
        return new ArrayList<>();
    }

    /** Salva (sovrascrive) la lista dei menu combinati. */
    public void save(List<GroupedItem> items) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(new ArrayList<>(items));
        }
    }
}
