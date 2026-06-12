package cassaproloco;

import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterJob;

/**
 * Geometria dello scontrino (carta larga quanto il rullino × 4 cm) e conversioni
 * cm→punti.
 *
 * <p>Estratta da {@code Cassa} per disaccoppiare la stampa dalla finestra:
 * {@link ReceiptModel} e {@code Cassa} usano questa classe. Cambia solo la
 * larghezza in base al {@link RollSize}; con {@link RollSize#MM62} i valori sono
 * identici a prima, quindi lo scontrino del rullino storico resta invariato.
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

    /** PageFormat dello scontrino sul rullino storico (62 mm): identico a prima. */
    static PageFormat pageFormat() {
        return pageFormat(RollSize.MM62);
    }

    /** PageFormat dello scontrino per il formato rullino indicato (larghezza×4 cm, PORTRAIT). */
    static PageFormat pageFormat(RollSize roll) {
        PrinterJob job = PrinterJob.getPrinterJob();
        PageFormat pf = job.defaultPage();
        Paper paper = pf.getPaper();
        double w = fromCMToPPI(roll.widthCm), h = fromCMToPPI(RollSize.HEIGHT_CM);
        paper.setSize(w, h);
        paper.setImageableArea(fromCMToPPI(0.25), fromCMToPPI(0), w, h - fromCMToPPI(1));
        pf.setOrientation(PageFormat.PORTRAIT);
        pf.setPaper(paper);
        return pf;
    }
}
