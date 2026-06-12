package cassaproloco;

import java.awt.Color;
import java.awt.Font;

/**
 * Palette colori, font e spaziature centralizzati dell'applicazione.
 *
 * <p>Tema <b>chiaro (light) moderno</b>: superfici chiare, tile bianchi a "card",
 * accenti blu/ambra/verde. I componenti custom ({@link ModernButtonUI}, ecc.)
 * usano questi colori.
 */
public final class Theme {

    private Theme() {
    }

    // --- Superfici ---
    /** Sfondo principale dell'app (grigio chiarissimo). */
    public static final Color BACKGROUND = new Color(237, 239, 242);
    /** Superficie "card" (bianca) per i tile/pulsanti chiari. */
    public static final Color CARD = Color.WHITE;
    public static final Color CARD_HOVER = new Color(233, 236, 241);
    public static final Color CARD_CLICK = new Color(220, 224, 230);
    /** Sfondo del pannello carrello (bianco). */
    public static final Color BASKET_BG = Color.WHITE;
    /** Sfondo riga carrello (azzurro tenue). */
    public static final Color BASKET_LINE_BG = new Color(233, 241, 248);

    // --- Accento primario (blu) ---
    public static final Color PRIMARY = new Color(62, 124, 177);
    public static final Color SECONDARY = new Color(90, 151, 201); // hover
    public static final Color ACCENT = new Color(44, 95, 143);      // pressed

    // --- Accento "caldo" (ambra) per OMAGGIO ---
    public static final Color WARM_BASE = new Color(224, 164, 88);
    public static final Color WARM_HOVER = new Color(236, 188, 126);
    public static final Color WARM_CLICK = new Color(198, 138, 62);

    // --- Verde (NUOVO MENU) ---
    public static final Color GREEN_BASE = new Color(95, 164, 95);
    public static final Color GREEN_HOVER = new Color(127, 192, 127);
    public static final Color GREEN_CLICK = new Color(74, 138, 74);

    // --- Rosso (elimina) ---
    public static final Color DANGER_BASE = new Color(217, 83, 79);
    public static final Color DANGER_HOVER = new Color(228, 120, 115);
    public static final Color DANGER_CLICK = new Color(184, 61, 57);

    // --- Testo ---
    public static final Color TEXT_DARK = new Color(42, 46, 53);
    /** Testo "secondario"/attenuato su sfondo chiaro. */
    public static final Color TEXT_LIGHT = new Color(107, 114, 128);
    /** Testo bianco sopra i pulsanti accentati. */
    public static final Color TEXT_ON_DARK = Color.WHITE;

    // --- Font ---
    public static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font TOTAL_FONT = new Font("Segoe UI", Font.BOLD, 48);

    // --- Spaziature ---
    public static final int GAP = 8;
    public static final int BORDER = 20;
}
