package cassaproloco;

/**
 * Formato del rullino di stampa: cambia solo la <b>larghezza</b> dello scontrino;
 * la lunghezza (altezza, lato avanzamento carta) resta fissa a 4&nbsp;cm.
 *
 * <p>{@link #MM62} è il formato storico: con esso la geometria e il layout dello
 * scontrino restano identici a prima. {@link #MM54} è il rullino più stretto.
 */
public enum RollSize {

    MM62("62 mm", 6.2),
    // Larghezza 5.38 cm (non 5.40): è la misura ESATTA del media "54mm" del
    // driver Brother QL (53,8 mm). Mandare 54,0 mm fa scattare l'errore
    // "il rotolo non corrisponde a quello selezionato"; con 53,8 mm combacia.
    MM54("54 mm", 5.38);

    /** Altezza/lunghezza dello scontrino in cm, uguale per tutti i formati. */
    public static final double HEIGHT_CM = 4.0;

    /** Etichetta mostrata nell'interfaccia. */
    public final String label;
    /** Larghezza del rullino (e dello scontrino) in cm. */
    public final double widthCm;

    RollSize(String label, double widthCm) {
        this.label = label;
        this.widthCm = widthCm;
    }

    /** Risolve un formato dal nome ({@code MM62}/{@code MM54}); 62 mm se assente o ignoto. */
    public static RollSize fromName(String name) {
        if (name != null) {
            for (RollSize r : values()) {
                if (r.name().equalsIgnoreCase(name.trim())) {
                    return r;
                }
            }
        }
        return MM62;
    }

    @Override
    public String toString() {
        return label;
    }
}
