package cassaproloco;

import java.util.Locale;

/**
 * Utility per i prezzi. I prezzi sono gestiti internamente in <b>centesimi
 * interi</b> (non in {@code float}), per evitare errori di arrotondamento tipici
 * della virgola mobile in una cassa.
 */
final class Money {

    private Money() {
    }

    /** "2.50" con il locale di default (UI e scontrino: in IT mostra la virgola). */
    static String format(int cents) {
        return String.format("%.2f", cents / 100.0);
    }

    /** "2.50" con il punto decimale, indipendente dal locale (per il CSV). */
    static String formatRoot(int cents) {
        return String.format(Locale.ROOT, "%.2f", cents / 100.0);
    }

    /** Converte una stringa prezzo ("2,50" o "2.50") in centesimi. */
    static int parse(String s) {
        return Math.round(Float.parseFloat(s.trim().replace(',', '.')) * 100f);
    }
}
