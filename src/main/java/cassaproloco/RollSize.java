package cassaproloco;

/**
 * Formato del rullino di stampa: cambia solo la <b>larghezza</b> dello scontrino;
 * la lunghezza (altezza, lato avanzamento carta) resta fissa a 4&nbsp;cm.
 *
 * <p>{@link #MM62} è il formato storico: con esso la geometria e il layout dello
 * scontrino restano identici a prima. {@link #MM54} è il rullino più stretto.
 */
public enum RollSize {

    // larghezza, lunghezza fisica dell'etichetta (cm)
    MM62("62 mm", 6.2, 2.9),
    // Larghezza 5.38 cm (non 5.40): è la misura ESATTA del media "54mm" del
    // driver Brother QL (53,8 mm). Mandare 54,0 mm fa scattare l'errore
    // "il rotolo non corrisponde a quello selezionato"; con 53,8 mm combacia.
    MM54("54 mm", 5.38, 4.0);

    /**
     * Altezza della <b>pagina</b> inviata alla stampante, in cm. Resta 4 cm per
     * tutti: il driver Brother taglia poi alla lunghezza del formato scelto
     * (62 mm → "62mm x 29mm" ≈ 2,9 cm; 54 mm → "54mm" con Lunghezza 4 cm).
     */
    public static final double HEIGHT_CM = 4.0;

    /** Etichetta mostrata nell'interfaccia. */
    public final String label;
    /** Larghezza del rullino (e dello scontrino) in cm. */
    public final double widthCm;
    /**
     * Lunghezza <b>fisica</b> dell'etichetta in cm (dove il rullino taglia
     * davvero): 2,9 per il 62 mm, 4,0 per il 54 mm. Usata dall'editor per mostrare
     * la zona effettivamente stampata di ciascun rullino.
     */
    public final double labelHeightCm;

    RollSize(String label, double widthCm, double labelHeightCm) {
        this.label = label;
        this.widthCm = widthCm;
        this.labelHeightCm = labelHeightCm;
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
