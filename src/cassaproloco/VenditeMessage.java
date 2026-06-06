/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cassaproloco;

import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author franc
 */
public class VenditeMessage implements Serializable {
    public String data;           // es: "2025-07-02"
    public ArrayList<String> righeCSV; // tutte le righe dal file

    public VenditeMessage(String data, ArrayList<String> righeCSV) {
        this.data = data;
        this.righeCSV = righeCSV;
    }
}
