package cassaproloco;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Legge i file CSV giornalieri ({@code report_AAAA-MM-GG.csv}) e aggrega le
 * vendite per nome prodotto. Logica estratta da {@code SalesReportPanel}
 * per renderla testabile senza UI.
 */
public class SalesReportRepository {

    private static final Pattern FILE_PATTERN =
            Pattern.compile("report_(\\d{4}-\\d{2}-\\d{2})\\.csv");

    /** Risultato dell'aggregazione di un giorno. */
    public static final class Aggregate {
        /** nome prodotto -&gt; [quantità totale, incasso in centesimi]. */
        public final Map<String, int[]> byName;
        public final int totalCents;

        Aggregate(Map<String, int[]> byName, int totalCents) {
            this.byName = byName;
            this.totalCents = totalCents;
        }

        public double getTotalEuro() {
            return totalCents / 100.0;
        }
    }

    /** Date disponibili (ordinate) ricavate dai nomi dei file nella cartella. */
    public List<String> availableDates(File folder) {
        File[] files = folder.listFiles((dir, name) -> FILE_PATTERN.matcher(name).matches());
        List<String> dates = new ArrayList<>();
        if (files == null) {
            return dates;
        }
        for (File f : files) {
            Matcher m = FILE_PATTERN.matcher(f.getName());
            if (m.matches()) {
                dates.add(m.group(1));
            }
        }
        Collections.sort(dates);
        return dates;
    }

    /** Aggrega le vendite del file CSV indicato. */
    public Aggregate aggregate(File csv) throws IOException {
        Map<String, int[]> byName = new LinkedHashMap<>();
        int totalCents = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(csv))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                List<String> fields = Csv.parseLine(line);
                if (fields.size() < 4) {
                    continue;
                }
                String name = fields.get(1).trim();
                int qty = Integer.parseInt(fields.get(2).trim());
                double price = Double.parseDouble(fields.get(3).trim());

                int incomeCents = (int) (price * qty * 100);
                totalCents += incomeCents;

                int[] acc = byName.computeIfAbsent(name, k -> new int[]{0, 0});
                acc[0] += qty;
                acc[1] += incomeCents;
            }
        }
        return new Aggregate(byName, totalCents);
    }
}
