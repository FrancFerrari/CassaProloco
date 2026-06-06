package cassaproloco;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Persistenza dei menu combinati ({@link GroupedItem}) in formato JSON.
 *
 * <p>Al primo avvio, se il file JSON non esiste ma è presente un vecchio file
 * serializzato ({@code .ser}), tenta una migrazione una-tantum (best-effort):
 * in caso di incompatibilità si riparte da una lista vuota (i menu sono
 * ricreabili dall'interfaccia).
 */
public class GroupedItemStore {

    private static final Type LIST_TYPE = new TypeToken<List<GroupedItem>>() {}.getType();

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final File jsonFile;
    private final File legacySerFile;

    public GroupedItemStore(File jsonFile) {
        this(jsonFile, null);
    }

    public GroupedItemStore(File jsonFile, File legacySerFile) {
        this.jsonFile = jsonFile;
        this.legacySerFile = legacySerFile;
    }

    /** Carica la lista salvata; lista vuota se assente o illeggibile. */
    public List<GroupedItem> load() {
        if (jsonFile.exists()) {
            try (Reader r = new InputStreamReader(new FileInputStream(jsonFile), StandardCharsets.UTF_8)) {
                List<GroupedItem> list = gson.fromJson(r, LIST_TYPE);
                if (list != null) {
                    return new ArrayList<>(list);
                }
            } catch (Exception ex) {
                System.out.println("GroupedItem JSON non caricati: " + ex.getMessage());
            }
            return new ArrayList<>();
        }

        // Migrazione una-tantum dal vecchio formato serializzato
        List<GroupedItem> migrated = tryLoadLegacy();
        if (!migrated.isEmpty()) {
            try {
                save(migrated);
                System.out.println("Migrazione menu da .ser a JSON completata.");
            } catch (IOException ignored) {
                // se non riusciamo a scrivere il JSON, restituiamo comunque i dati migrati
            }
        }
        return migrated;
    }

    @SuppressWarnings("unchecked")
    private List<GroupedItem> tryLoadLegacy() {
        if (legacySerFile == null || !legacySerFile.exists()) {
            return new ArrayList<>();
        }
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(legacySerFile))) {
            Object obj = in.readObject();
            if (obj instanceof List) {
                return new ArrayList<>((List<GroupedItem>) obj);
            }
        } catch (Exception ex) {
            System.out.println("Migrazione .ser fallita (si riparte da vuoto): " + ex.getMessage());
        }
        return new ArrayList<>();
    }

    /** Salva (sovrascrive) la lista dei menu combinati in JSON. */
    public void save(List<GroupedItem> items) throws IOException {
        try (Writer w = new OutputStreamWriter(new FileOutputStream(jsonFile), StandardCharsets.UTF_8)) {
            gson.toJson(items, LIST_TYPE, w);
        }
    }
}
