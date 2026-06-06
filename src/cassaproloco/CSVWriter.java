package cassaproloco;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.JOptionPane;

public class CSVWriter {
    private final String csvFile;
    private final String TotalFileString;
    private String[] headers;
    private File file;

    public CSVWriter(String csvFile, String TotalFileString) throws IOException {
        this.csvFile = csvFile;
        this.TotalFileString = TotalFileString;

        createCsvFileIfNotExists(csvFile);
        createTxTFileIfNotExists(TotalFileString);
    }

    private void createCsvFileIfNotExists(String csvFile) {
        try {
            
            file = new File(csvFile);
            if (!file.exists()) {
                // Crea il file CSV
                file.createNewFile();
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Errore Creazione File CVS " + e.getMessage(), "ERRORE", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void createTxTFileIfNotExists(String TotalFileString) {
        try {
            
            file = new File(TotalFileString);
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Errore Creazione File TXT " + e.getMessage(), "ERRORE", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void writeHeaders(FileWriter writer) throws IOException {
        for (int i = 0; i < headers.length; i++) {
            writer.append(headers[i]);
            if (i < headers.length - 1) {
                writer.append(",");
            }
        }
        writer.append("\n");
    }

    
    public void writeDataToCsvFile(String[] data) throws IOException {
        System.out.println(csvFile);
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String riga;
            StringBuilder fileModificato = new StringBuilder();
            boolean trovato = false;
            while ((riga = br.readLine()) != null) {
                String[] campi = riga.split(",");
                if (campi.length > 0 && campi[0].equals(data[0])) {
                    // Modifica il valore della colonna desiderata
                    campi[1] = Integer.toString(Integer.parseInt(campi[1])+Integer.parseInt(data[1]));
                    campi[2] = Float.toString(Float.parseFloat(campi[2])+Float.parseFloat(data[2]));
                    trovato = true;
                    System.out.println("modificato");
                    
                }
                fileModificato.append(String.join(",", campi)).append("\n");
                //br.close();
            }
            
            if (trovato) {
            // Scrive le modifiche sul file CSV
            try(FileWriter writer = new FileWriter(csvFile)){
                writer.write(fileModificato.toString());
                writer.close();
                
             } catch (IOException e) {
                 JOptionPane.showMessageDialog(null, "Errore in riscrittura File CVS " + e.getMessage(), "ERRORE", JOptionPane.ERROR_MESSAGE);
            }
            }else{
                try(FileWriter writer = new FileWriter(csvFile,true)){
                System.out.println("entrato");
                for (int i = 0; i < data.length; i++) {
                    writer.append(data[i]);
                    System.out.println("stampato");
                    if (i < data.length - 1) {
                        writer.append(",");
                    }
                }
                writer.append("\n");
                }catch (IOException e) {
                  JOptionPane.showMessageDialog(null, "Errore in scrittura File CVS " + e.getMessage(), "ERRORE", JOptionPane.ERROR_MESSAGE);
            }
            }
            
 }
}
    public void writeDataToTxTFile(String[] data) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(TotalFileString))) {
            String riga;
            StringBuilder fileModificato = new StringBuilder();
            boolean trovato = false;
            while ((riga = br.readLine()) != null) {
                riga = Float.toString(Float.parseFloat(riga) + Float.parseFloat(data[2]));
                fileModificato.append(riga);
                trovato = true;
                //br.close();
            }
            
            if (trovato) {
            // Scrive le modifiche sul file CSV
            try(FileWriter writer = new FileWriter(TotalFileString)){
                writer.write(fileModificato.toString());
                writer.close();
                
             } catch (IOException e) {
                  JOptionPane.showMessageDialog(null, "Errore in riscrittura File TXT " + e.getMessage(), "ERRORE", JOptionPane.ERROR_MESSAGE);
            }
            }else{
                try(FileWriter writer = new FileWriter(TotalFileString)){
                System.out.println("entrato");
                writer.append(data[2]);
                }catch (IOException e) {
                  JOptionPane.showMessageDialog(null, "Errore in scrittura File TXT " + e.getMessage(), "ERRORE", JOptionPane.ERROR_MESSAGE);
            }
            }
            
 }
}
}
