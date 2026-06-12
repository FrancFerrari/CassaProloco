package cassaproloco;

import org.junit.jupiter.api.Test;

import java.awt.print.PageFormat;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifica la geometria della pagina per i vari formati rullino. In particolare
 * che il 62 mm resti identico al comportamento storico (no-arg = MM62) e che il
 * 54 mm cambi solo in larghezza, non in altezza.
 */
class ReceiptGeometryTest {

    private static final double EPS = 0.5;

    @Test
    void noArgEquivaleA62mm() {
        PageFormat def = ReceiptGeometry.pageFormat();
        PageFormat mm62 = ReceiptGeometry.pageFormat(RollSize.MM62);
        assertEquals(mm62.getWidth(), def.getWidth(), EPS);
        assertEquals(mm62.getHeight(), def.getHeight(), EPS);
        assertEquals(mm62.getImageableWidth(), def.getImageableWidth(), EPS);
        assertEquals(mm62.getImageableHeight(), def.getImageableHeight(), EPS);
    }

    @Test
    void larghezza62mmInvariata() {
        PageFormat pf = ReceiptGeometry.pageFormat(RollSize.MM62);
        assertEquals(ReceiptGeometry.fromCMToPPI(6.2), pf.getWidth(), EPS);
        assertEquals(ReceiptGeometry.fromCMToPPI(4.0), pf.getHeight(), EPS);
    }

    @Test
    void rullino54PiuStrettoStessaAltezza() {
        PageFormat mm62 = ReceiptGeometry.pageFormat(RollSize.MM62);
        PageFormat mm54 = ReceiptGeometry.pageFormat(RollSize.MM54);

        // larghezza = misura esatta del media Brother "54mm" (53,8 mm)
        assertEquals(ReceiptGeometry.fromCMToPPI(RollSize.MM54.widthCm), mm54.getWidth(), EPS);
        assertEquals(ReceiptGeometry.fromCMToPPI(5.38), mm54.getWidth(), EPS);
        assertTrue(mm54.getWidth() < mm62.getWidth(), "il 54 mm deve essere più stretto del 62 mm");
        // L'altezza (lunghezza dello scontrino) resta la stessa.
        assertEquals(mm62.getHeight(), mm54.getHeight(), EPS);
    }
}
