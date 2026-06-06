/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package cassaproloco;

/**
 *
 * @author franc
 */
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
public class PannelloResocontoVendite extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private JComboBox<String> comboDate;
    private File csvFolder;
    private JLabel labelTotale;
    private final SalesReportRepository repo = new SalesReportRepository();

    public PannelloResocontoVendite(File csvFolder) {
        this.csvFolder = csvFolder;
        setLayout(new BorderLayout());

        model = new DefaultTableModel(new String[]{"Nome", "Quantità", "Incasso (€)"}, 0);
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Top panel: combo box date
        JPanel topPanel = new JPanel(new FlowLayout());
        comboDate = new JComboBox<>();
        caricaDateDisponibili();

        comboDate.addActionListener(e -> {
            String dataSelezionata = (String) comboDate.getSelectedItem();
            if (dataSelezionata != null) caricaVendite(dataSelezionata);
        });

        topPanel.add(new JLabel("Data:"));
        topPanel.add(comboDate);
        add(topPanel, BorderLayout.NORTH);

        // Bottom panel: totale
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        labelTotale = new JLabel("Totale giornaliero: 0.00 €");
        bottomPanel.add(labelTotale);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    /** Popola la combo box con le date trovate nei nomi dei file CSV */
    private void caricaDateDisponibili() {
        List<String> dateList = repo.availableDates(csvFolder);
        comboDate.setModel(new DefaultComboBoxModel<>(dateList.toArray(new String[0])));
    }

    /** Carica e somma le vendite dal file CSV per la data selezionata */
    private void caricaVendite(String data) {
        model.setRowCount(0);
        File file = new File(csvFolder, "report_" + data + ".csv");

        if (!file.exists()) {
            JOptionPane.showMessageDialog(this, "File non trovato:\n" + file.getName());
            labelTotale.setText("Totale giornaliero: 0.00 €");
            return;
        }

        try {
            SalesReportRepository.Aggregate agg = repo.aggregate(file);
            for (Map.Entry<String, int[]> entry : agg.byName.entrySet()) {
                int qty = entry.getValue()[0];
                double incasso = entry.getValue()[1] / 100.0;
                model.addRow(new Object[]{entry.getKey(), qty, String.format("%.2f", incasso)});
            }
            labelTotale.setText(String.format("Totale giornaliero: %.2f €", agg.getTotalEuro()));
        } catch (IOException | NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Errore durante la lettura:\n" + e.getMessage());
            labelTotale.setText("Totale giornaliero: 0.00 €");
        }
    }
}
