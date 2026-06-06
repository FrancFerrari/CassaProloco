# Funzione di stampa — stato di riferimento (NON rompere)

Questo documento descrive **come funziona la stampa** nello stato attuale, così da
poterla ripristinare se una futura modifica la dovesse alterare.

> Regola: lo **scontrino stampato deve restare identico** (va su stampante etichette
> reale). Le modifiche ammesse (es. stampa fuori dall'EDT) devono *avvolgere* la
> stampa, non cambiare il rendering.

Snapshot di codice recuperabile: **git tag `print-baseline-v1`** (commit della Fase 4b).
Per recuperare i file di stampa a quello stato:
```
git checkout print-baseline-v1 -- src/main/java/cassaproloco/ModelloStampa.java \
    src/main/java/cassaproloco/Paint.java src/main/java/cassaproloco/Cassa.java
```

## Componenti coinvolti

- **`ModelloStampa`** (`implements java.awt.print.Printable`): costruisce lo scontrino.
  Disegna su un pannello `Paint` (layout `null`, posizioni assolute via `setBounds`):
  - prezzo `"<price>€"` (se price non vuoto) a (130,55)
  - riga `"<qty>x <Nome>"` centrata a y=30
  - `"ProLoco Cogollo"` a (55,17)
  - `"Antica Sagra di S.Luigi "` a (39,7)
  - `"Data: gg/MM/aaaa"` a (15,55)
  - `"Ora: HH:mm:ss"` sotto la data
  - `print(Graphics, PageFormat, page)` → `panel.print(g2d); printAll(g2d);`
- **`Paint`**: il `JPanel` su cui vengono disegnate le etichette.
- **Conversioni**: `Cassa.fromCMToPPI(cm)` e `Cassa.toPPI(inch)` (1 inch = 72 pt).

## Formato pagina (PageFormat)

Costruito in `Cassa.printAndRecord()`:
- carta **6.2 cm × 4 cm** (`paper.setSize(fromCMToPPI(6.2), fromCMToPPI(4))`)
- area stampabile: origine x = `fromCMToPPI(0.25)`, y = 0; larghezza = piena;
  altezza = `4cm − 1cm`
- orientamento **PORTRAIT**

## Flusso di stampa (`Cassa.printAndRecord`)

Per ogni riga del carrello (`JPanelBasketLine`):
- quantità `qty` dal `Basket`;
- modalità dal toggle della riga (`isUnitPrinting()`):
  - **Unito** → 1 scontrino con quantità `qty`;
  - **Separato** → `qty` scontrini da 1.
- **Item singolo** → `printOnce(item, ...)` (→ `printItem(...)`), prezzo = prezzo
  effettivo del basket (rispetta l'Omaggio).
- **GroupedItem (menu)** → stampa una voce per ogni portata presente
  (bevanda/primo/secondo/dolce/caffè) con `printItem(...)`.
- Ogni scontrino: `ModelloStampa` → `Book` → `job.setPageable(book)` → `job.print()`
  (stampante **predefinita** di sistema, senza dialog).
- In parallelo viene registrata la vendita su `report_AAAA-MM-GG.csv`
  (`SalesRecorder`, prezzi con `Locale.ROOT`).
- A fine stampa: `basket.restorePrices(); basket.clear(); basketPanel.clear();`

## Come testare senza stampante

`ReceiptPreview` (`cassaproloco.ReceiptPreview`) renderizza `ModelloStampa` — con lo
**stesso** metodo `print(Graphics, …)` usato dalla stampante — su file PNG, per
verificare l'aspetto dello scontrino:
```
java -cp "target/classes;lib/AbsoluteLayout.jar" cassaproloco.ReceiptPreview
```
Le immagini vengono salvate nella cartella temporanea (percorso stampato a video).
