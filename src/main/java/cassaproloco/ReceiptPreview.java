package cassaproloco;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.io.File;
import java.util.Date;

import javax.imageio.ImageIO;

/**
 * Strumento di anteprima/test della stampa: renderizza {@link ReceiptModel}
 * — con lo <b>stesso</b> metodo {@code print(Graphics, PageFormat, page)} che usa
 * la stampante — su file PNG, così da poter verificare l'aspetto dello scontrino
 * senza una stampante fisica.
 *
 * <p>Non altera in alcun modo la funzione di stampa: la usa soltanto.
 *
 * <pre>java -cp "target/classes;dist/lib/*" cassaproloco.ReceiptPreview</pre>
 */
public final class ReceiptPreview {

    private static final int DPI = 300;

    private ReceiptPreview() {
    }

    public static void main(String[] args) throws Exception {
        File outDir = new File(System.getProperty("java.io.tmpdir"));

        // Rullino storico 62 mm (deve restare identico).
        PageFormat pf62 = ReceiptGeometry.pageFormat(RollSize.MM62);
        render(new File(outDir, "scontrino_singolo.png"),
                new ReceiptModel("2.50", "2", "Coca-cola alla spina", new Date(), RollSize.MM62), pf62);
        render(new File(outDir, "scontrino_menu_portata.png"),
                new ReceiptModel("", "1", "Panini Porchetta", new Date(), RollSize.MM62), pf62);
        render(new File(outDir, "scontrino_nome_lungo.png"),
                new ReceiptModel("7.00", "1", "Vino litro", new Date(), RollSize.MM62), pf62);

        // Rullino 54 mm (stesse voci, larghezza ridotta).
        PageFormat pf54 = ReceiptGeometry.pageFormat(RollSize.MM54);
        render(new File(outDir, "scontrino_singolo_54.png"),
                new ReceiptModel("2.50", "2", "Coca-cola alla spina", new Date(), RollSize.MM54), pf54);
        render(new File(outDir, "scontrino_menu_portata_54.png"),
                new ReceiptModel("", "1", "Panini Porchetta", new Date(), RollSize.MM54), pf54);
        render(new File(outDir, "scontrino_nome_lungo_54.png"),
                new ReceiptModel("7.00", "1", "Vino litro", new Date(), RollSize.MM54), pf54);

        System.out.println("Anteprime salvate in: " + outDir.getAbsolutePath());
    }

    private static void render(File out, ReceiptModel receipt, PageFormat pf) throws Exception {
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
