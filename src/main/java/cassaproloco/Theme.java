package cassaproloco;

import java.awt.Color;
import java.awt.Font;

/**
 * Palette colori, font e spaziature centralizzati dell'applicazione.
 *
 * <p>Prima questi valori erano sparsi e ripetuti (spesso via {@code RGBtoHSB})
 * in tutta la UI. Qui sono raccolti in un unico punto per coerenza e per poter
 * cambiare tema facilmente.
 */
public final class Theme {

    private Theme() {
    }

    // --- Superfici ---
    /** Sfondo principale dell'app (viola scuro). */
    public static final Color BACKGROUND = new Color(58, 48, 66);
    /** Sfondo del pannello carrello (azzurro chiaro). */
    public static final Color BASKET_BG = new Color(220, 234, 244);
    /** Sfondo riga carrello. */
    public static final Color BASKET_LINE_BG = new Color(178, 98, 110);

    // --- Pulsanti "primario" (categorie/menu) ---
    public static final Color PRIMARY = new Color(82, 141, 164);
    public static final Color SECONDARY = new Color(128, 209, 195);
    public static final Color ACCENT = new Color(78, 108, 135);

    // --- Pulsanti "azione" (toolbar, navigazione) - tonalità calde ---
    public static final Color WARM_BASE = new Color(228, 136, 106);
    public static final Color WARM_HOVER = new Color(255, 225, 156);
    public static final Color WARM_CLICK = new Color(219, 157, 71);

    // --- Testo ---
    public static final Color TEXT_LIGHT = new Color(221, 221, 221);
    public static final Color TEXT_ON_DARK = Color.WHITE;

    // --- Font ---
    public static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font TOTAL_FONT = new Font("Helvetica", Font.BOLD, 50);

    // --- Spaziature ---
    public static final int GAP = 8;
    public static final int BORDER = 20;
}
