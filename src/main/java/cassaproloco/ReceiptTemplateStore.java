package cassaproloco;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Persistenza del template dello scontrino in JSON ({@code receiptTemplate.json}).
 *
 * <p>Se il file non esiste o è illeggibile si parte dal {@link
 * ReceiptTemplate#defaultTemplate() template predefinito} (cioè lo scontrino
 * storico): così, finché l'utente non personalizza nulla, la stampa resta identica.
 */
public class ReceiptTemplateStore {

    private static final Logger LOG = Logger.getLogger(ReceiptTemplateStore.class.getName());

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final File file;

    public ReceiptTemplateStore(File file) {
        this.file = file;
    }

    /** Carica il template salvato; quello predefinito se assente o illeggibile. */
    public ReceiptTemplate load() {
        if (file.exists()) {
            try (Reader r = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
                ReceiptTemplate t = gson.fromJson(r, ReceiptTemplate.class);
                if (t != null && t.elements != null && !t.elements.isEmpty()) {
                    return t;
                }
            } catch (Exception ex) {
                LOG.log(Level.WARNING, "Template scontrino non caricato, uso il predefinito", ex);
            }
        }
        return ReceiptTemplate.defaultTemplate();
    }

    /** Salva (sovrascrive) il template in JSON. */
    public void save(ReceiptTemplate template) throws IOException {
        try (Writer w = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            gson.toJson(template, w);
        }
    }
}
