package cassaproloco;

import java.io.File;

/**
 * Risolve la cartella dei file dati (configurazioni {@code .cfg}, menu JSON,
 * report CSV) in modo robusto rispetto alla working directory.
 *
 * <p>Prima l'app usava percorsi relativi come {@code new File("bere.cfg")}, che
 * dipendono dalla cartella da cui viene avviata: lanciandola da una directory
 * diversa (es. VSCode con workspace sulla cartella madre, oppure il jar da
 * {@code dist/}) i file non venivano trovati e ne venivano creati di vuoti.
 *
 * <p>Strategia di risoluzione:
 * <ol>
 *   <li>la working directory, se contiene già {@code bere.cfg} (caso sviluppo);</li>
 *   <li>altrimenti si risale dalla posizione del jar/classi in esecuzione
 *       (es. {@code dist/} o {@code target/classes}) cercando {@code bere.cfg};</li>
 *   <li>in mancanza, la cartella del jar (installazione nuova: dati accanto al jar).</li>
 * </ol>
 */
final class AppPaths {

    private static final String MARKER = "bere.cfg";
    private static final File BASE = resolveBase();

    private AppPaths() {
    }

    /** Cartella base dei dati. */
    static File base() {
        return BASE;
    }

    /** File dati con il nome indicato, risolto nella cartella base. */
    static File file(String name) {
        return new File(BASE, name);
    }

    /** Vero se la cartella contiene un {@code bere.cfg} NON vuoto (i file vuoti
     *  lasciati da vecchi avvii non devono ingannare la risoluzione). */
    private static boolean hasConfig(File dir) {
        if (dir == null) {
            return false;
        }
        File f = new File(dir, MARKER);
        return f.isFile() && f.length() > 0;
    }

    private static File resolveBase() {
        File cwd = new File("").getAbsoluteFile();
        if (hasConfig(cwd)) {
            return cwd;
        }
        try {
            File loc = new File(Cassa.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI());
            File firstDir = loc.isFile() ? loc.getParentFile() : loc;
            File walk = firstDir;
            for (int i = 0; i < 6 && walk != null; i++) {
                if (hasConfig(walk)) {
                    return walk;
                }
                walk = walk.getParentFile();
            }
            if (firstDir != null) {
                return firstDir; // installazione nuova: dati accanto al jar
            }
        } catch (Exception ignored) {
            // ricadiamo sulla working directory
        }
        return cwd;
    }
}
