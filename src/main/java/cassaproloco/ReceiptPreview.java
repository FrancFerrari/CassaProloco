package cassaproloco;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterJob;
import java.io.File;
import java.util.Date;

import javax.imageio.ImageIO;

/**
 * Strumento di anteprima/test della stampa: renderizza {@link ModelloStampa}
 * — con lo <b>stesso</b> metodo {@code print(Graphics, PageFormat, page)} che usa
 * la stampante — su file PNG, così da poter verificare l'aspetto dello scontrino
 * senza una stampante fisica.
 *
 * <p>Non altera in alcun modo la funzione di stampa: la usa soltanto.
 *
 * <pre>java -cp "target/classes;lib/AbsoluteLayout.jar" cassaproloco.ReceiptPreview</pre>
 */
public final class ReceiptPreview {

    private static final int DPI = 300;

    private ReceiptPreview() {
    }

    public static void main(String[] args) throws Exception {
        PageFormat pf = buildPageFormat();
        File outDir = new File(System.getProperty("java.io.tmpdir"));

        render(new File(outDir, "scontrino_singolo.png"),
                new ModelloStampa("2.50", "2", "Coca-cola alla spina", new Date()), pf);
        render(new File(outDir, "scontrino_menu_portata.png"),
                new ModelloStampa("", "1", "Panini Porchetta", new Date()), pf);
        render(new File(outDir, "scontrino_nome_lungo.png"),
                new ModelloStampa("7.00", "1", "Vino litro", new Date()), pf);

        System.out.println("Anteprime salvate in: " + outDir.getAbsolutePath());
    }

    /** Stesso PageFormat costruito da Cassa.printAndRecord(). */
    static PageFormat buildPageFormat() {
        PrinterJob job = PrinterJob.getPrinterJob();
        PageFormat pf = job.defaultPage();
        Paper paper = pf.getPaper();
        double w = Cassa.fromCMToPPI(6.2), h = Cassa.fromCMToPPI(4);
        paper.setSize(w, h);
        paper.setImageableArea(Cassa.fromCMToPPI(0.25), Cassa.fromCMToPPI(0), w, h - Cassa.fromCMToPPI(1));
        pf.setOrientation(PageFormat.PORTRAIT);
        pf.setPaper(paper);
        return pf;
    }

    private static void render(File out, ModelloStampa receipt, PageFormat pf) throws Exception {
        int wpx = (int) Math.ceil(pf.getWidth() / 72.0 * DPI);
        int hpx = (int) Math.ceil(pf.getHeight() / 72.0 * DPI);

        BufferedImage img = new BufferedImage(wpx, hpx, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, wpx, hpx);
        g.scale(DPI / 72.0, DPI / 72.0); // il Printable disegna in punti (72 dpi)
        receipt.print(g, pf, 0);
        g.dispose();

        ImageIO.write(img, "png", out);
        System.out.println("  -> " + out.getName());
    }
}
