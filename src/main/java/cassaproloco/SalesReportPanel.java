package cassaproloco;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/**
 * Resoconto vendite: per la data scelta mostra la tabella aggregata per prodotto
 * (quantità e incasso) e il totale giornaliero. I dati arrivano da
 * {@link SalesReportRepository}. Eredita il look FlatLaf dell'applicazione.
 */
public class SalesReportPanel extends JPanel {

    private final DefaultTableModel model;
    private final JComboBox<String> comboDate = new JComboBox<>();
    private final JLabel labelTotale = new JLabel("Totale: 0.00 €");
    private final File csvFolder;
    private final SalesReportRepository repo = new SalesReportRepository();

    public SalesReportPanel(File csvFolder) {
        this.csvFolder = csvFolder;
        setLayout(new BorderLayout(0, 10));
        setBorder(new EmptyBorder(12, 12, 12, 12));

        // Intestazione: titolo + selettore data
        JLabel title = new JLabel("Resoconto vendite");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        datePanel.add(new JLabel("Data:"));
        datePanel.add(comboDate);
        JPanel top = new JPanel(new BorderLayout());
        top.add(title, BorderLayout.WEST);
        top.add(datePanel, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        model = new DefaultTableModel(new String[] {"Nome", "Quantità", "Incasso (€)"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);
        table.setRowHeight(26);
        add(new JScrollPane(table), BorderLayout.CENTER);

        labelTotale.setFont(labelTotale.getFont().deriveFont(Font.BOLD, 20f));
        labelTotale.setHorizontalAlignment(SwingConstants.RIGHT);
        add(labelTotale, BorderLayout.SOUTH);

        comboDate.addActionListener(e -> {
            String data = (String) comboDate.getSelectedItem();
            if (data != null) {
                caricaVendite(data);
            }
        });
        caricaDateDisponibili();
    }

    private void caricaDateDisponibili() {
        List<String> dates = repo.availableDates(csvFolder);
        comboDate.setModel(new DefaultComboBoxModel<>(dates.toArray(new String[0])));
        if (!dates.isEmpty()) {
            comboDate.setSelectedIndex(dates.size() - 1); // più recente, mostra subito i dati
        }
    }

    private void caricaVendite(String data) {
        model.setRowCount(0);
        File file = new File(csvFolder, "report_" + data + ".csv");
        if (!file.exists()) {
            labelTotale.setText("Totale: 0.00 €");
            return;
        }
        try {
            SalesReportRepository.Aggregate agg = repo.aggregate(file);
            for (Map.Entry<String, int[]> entry : agg.byName.entrySet()) {
                model.addRow(new Object[] {
                        entry.getKey(), entry.getValue()[0],
                        String.format("%.2f", entry.getValue()[1] / 100.0)
                });
            }
            labelTotale.setText(String.format("Totale: %.2f €", agg.getTotalEuro()));
        } catch (IOException | NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Errore durante la lettura:\n" + ex.getMessage());
            labelTotale.setText("Totale: 0.00 €");
        }
    }
}
