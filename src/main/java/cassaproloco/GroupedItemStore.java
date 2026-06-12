package cassaproloco;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Persistenza dei menu ({@link GroupedItem}) in formato JSON.
 *
 * <p>Il modello attuale è "testata + lista di prodotti". Per non perdere i menu
 * salvati col vecchio modello a portate fisse, il deserializzatore è
 * <b>tollerante</b>: riconosce sia il nuovo formato ({@code menu}+{@code components})
 * sia il vecchio ({@code items} mappa di portate + {@code groupType}) e converte
 * il vecchio nella lista di prodotti.
 *
 * <p>Al primo avvio, se il file JSON non esiste ma è presente un vecchio file
 * serializzato ({@code .ser}), tenta una migrazione una-tantum (best-effort):
 * in caso di incompatibilità si riparte da una lista vuota (i menu sono
 * ricreabili dall'interfaccia).
 */
public class GroupedItemStore {

    private static final Logger LOG = Logger.getLogger(GroupedItemStore.class.getName());
    private static final Type LIST_TYPE = new TypeToken<List<GroupedItem>>() {}.getType();

    /** Ordine delle vecchie portate, usato per convertire i menu del vecchio formato. */
    private static final String[] LEGACY_COURSES = {"BEVERAGE", "FIRST", "SECOND", "DESSERT", "COFFEE"};

    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(GroupedItem.class, new GroupedItemDeserializer())
            .create();
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
                LOG.log(Level.WARNING, "GroupedItem JSON non caricati", ex);
            }
            return new ArrayList<>();
        }

        // Migrazione una-tantum dal vecchio formato serializzato
        List<GroupedItem> migrated = tryLoadLegacy();
        if (!migrated.isEmpty()) {
            try {
                save(migrated);
                LOG.info("Migrazione menu da .ser a JSON completata.");
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
            LOG.log(Level.WARNING, "Migrazione .ser fallita (si riparte da vuoto)", ex);
        }
        return new ArrayList<>();
    }

    /** Salva (sovrascrive) la lista dei menu in JSON. */
    public void save(List<GroupedItem> items) throws IOException {
        try (Writer w = new OutputStreamWriter(new FileOutputStream(jsonFile), StandardCharsets.UTF_8)) {
            gson.toJson(items, LIST_TYPE, w);
        }
    }

    /**
     * Legge sia il nuovo formato ({@code menu}+{@code components}) sia il vecchio
     * formato a portate ({@code items}+{@code groupType}), convertendolo nella
     * lista di prodotti.
     */
    private static final class GroupedItemDeserializer implements JsonDeserializer<GroupedItem> {
        @Override
        public GroupedItem deserialize(JsonElement el, Type type, JsonDeserializationContext ctx) {
            JsonObject o = el.getAsJsonObject();

            // Vecchio formato a portate: { "items": { "MENU": {...}, "FIRST": {...} }, "groupType": ... }
            if (o.has("items") && o.get("items").isJsonObject() && !o.has("components")) {
                JsonObject items = o.getAsJsonObject("items");
                Item menu = ctx.deserialize(items.get("MENU"), Item.class);
                if (menu == null) {
                    throw new JsonParseException("Menu mancante nel vecchio formato");
                }
                List<Item> comps = new ArrayList<>();
                for (String course : LEGACY_COURSES) {
                    if (items.has(course) && items.get(course).isJsonObject()) {
                        comps.add(ctx.<Item>deserialize(items.get(course), Item.class));
                    }
                }
                return new GroupedItem(menu, comps);
            }

            // Nuovo formato: { "menu": {...}, "components": [ {...}, ... ] }
            Item menu = ctx.deserialize(o.get("menu"), Item.class);
            if (menu == null) {
                throw new JsonParseException("Menu mancante");
            }
            List<Item> comps = new ArrayList<>();
            if (o.has("components") && o.get("components").isJsonArray()) {
                JsonArray arr = o.getAsJsonArray("components");
                for (JsonElement ce : arr) {
                    comps.add(ctx.<Item>deserialize(ce, Item.class));
                }
            }
            return new GroupedItem(menu, comps);
        }
    }
}
