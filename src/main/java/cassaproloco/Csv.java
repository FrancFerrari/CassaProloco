package cassaproloco;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility minimale per CSV conforme a RFC 4180 (separatore virgola).
 *
 * <p>Gestisce correttamente i campi che contengono virgole, virgolette o a capo
 * racchiudendoli tra virgolette e raddoppiando le virgolette interne. Risolve il
 * bug per cui prezzi/nomi con virgola rompevano le colonne.
 */
final class Csv {

    private Csv() {
    }

    /** Codifica un singolo campo, aggiungendo le virgolette solo se necessario. */
    static String escape(String field) {
        if (field == null) {
            field = "";
        }
        boolean needsQuote = field.indexOf(',') >= 0
                || field.indexOf('"') >= 0
                || field.indexOf('\n') >= 0
                || field.indexOf('\r') >= 0;
        if (!needsQuote) {
            return field;
        }
        return "\"" + field.replace("\"", "\"\"") + "\"";
    }

    /** Costruisce una riga CSV dai campi indicati. */
    static String toLine(String[] fields) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(escape(fields[i]));
        }
        return sb.toString();
    }

    /** Suddivide una riga CSV in campi, rispettando le virgolette. */
    static List<String> parseLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        cur.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    cur.append(c);
                }
            } else if (c == '"') {
                inQuotes = true;
            } else if (c == ',') {
                fields.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        fields.add(cur.toString());
        return fields;
    }
}
