package cassaproloco;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Carica le voci di menu da un file di configurazione {@code .cfg}.
 *
 * <p>Formato per riga: {@code nome;testoDaStampare;prezzo}. La prima riga è
 * un'intestazione e viene saltata; le righe malformate (numero di campi diverso
 * da 3 o prezzo non numerico) vengono ignorate.
 */
public final class MenuConfigLoader {

    private MenuConfigLoader() {
    }

    public static List<Item> load(File file) throws IOException {
        List<Item> items = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine(); // salta l'intestazione
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length != 3) {
                    continue;
                }
                try {
                    float price = Float.parseFloat(parts[2].trim());
                    items.add(new Item(0, price, parts[0], parts[1], 1));
                } catch (NumberFormatException ignored) {
                    // riga con prezzo non valido: la salto
                }
            }
        }
        return items;
    }
}
