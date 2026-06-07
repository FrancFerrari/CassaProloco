package cassaproloco;

import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterJob;

/**
 * Geometria dello scontrino (carta 6.2×4 cm) e conversioni cm→punti.
 *
 * <p>Estratta da {@code Cassa} per disaccoppiare la stampa dalla finestra:
 * {@link ModelloStampa} e {@code Cassa} usano questa classe. I valori sono
 * invariati rispetto a prima, quindi lo scontrino resta identico.
 */
final class ReceiptGeometry {

    private ReceiptGeometry() {
    }

    static double fromCMToPPI(double cm) {
        return toPPI(cm * 0.393700787);
    }

    static double toPPI(double inch) {
        return inch * 72d;
    }

    /** PageFormat dello scontrino: 6.2×4 cm, PORTRAIT. */
    static PageFormat pageFormat() {
        PrinterJob job = PrinterJob.getPrinterJob();
        PageFormat pf = job.defaultPage();
        Paper paper = pf.getPaper();
        double w = fromCMToPPI(6.2), h = fromCMToPPI(4);
        paper.setSize(w, h);
        paper.setImageableArea(fromCMToPPI(0.25), fromCMToPPI(0), w, h - fromCMToPPI(1));
        pf.setOrientation(PageFormat.PORTRAIT);
        pf.setPaper(paper);
        return pf;
    }
}
