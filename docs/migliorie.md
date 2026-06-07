# Piano migliorie — CassaProloco

Elenco di possibili migliorie al codice, ordinate per rapporto **valore/rischio**.
Ogni voce ha una stima e, dove serve, una nota di cautela.

> ⚠️ **Regola stampa**: le voci marcate _[STAMPA]_ toccano la funzione di stampa.
> Prima di intervenire creare/usare il backup recuperabile (tag git
> `print-baseline-v1`, vedi [`docs/stampa.md`](stampa.md)). Lo scontrino deve
> restare identico.

---

## 🟢 Quick win (alto valore, basso rischio)

- [ ] **1. Rimuovere la dipendenza `AbsoluteLayout`**
  Dopo la riscrittura UI non è più referenziata nel codice (unica menzione: un
  commento). Azioni: toglierla dal `pom.xml`, eliminare `repo/` e la copia in
  `dist/lib`, semplificare il manifest/`copy-dependencies`.
  _Effort: basso · Rischio: basso._

- [ ] **2. Ripulire gli ultimi residui NetBeans**
  `JPanelBasket` e `JPanelBasketLine` hanno ancora `initComponents()` generato
  (GroupLayout/GridBag), marker `//GEN-*` e handler vuoti
  (`lblQtyActionPerformed`, `selectBtnInputMethodTextChanged`). `JPanelBasket`
  crea un GroupLayout subito sovrascritto da `VerticalFlowLayout`.
  _Effort: medio · Rischio: basso/medio (verificare a schermo)._

- [ ] **3. Bug layout riga carrello**
  I controlli `+ / − / qty / prezzo / X` escono oltre il bordo destro: la riga è
  più larga del viewport. Sistemare il layout di `JPanelBasketLine`.
  _Effort: medio · Rischio: basso._

## 🟡 Correttezza

- [ ] **4. Soldi come `float` → centesimi interi (o `BigDecimal`)**
  `Item.price`, `Basket`, totali, stampa e CSV usano `float`: impreciso per una
  cassa (arrotondamenti). Migrare a `int`/`long` centesimi. _[STAMPA]_ (cambia il
  formato del prezzo a video/scontrino: verificare con cura).
  _Effort: alto · Rischio: medio · Impatto correttezza: alto._

- [ ] **5. `MenuBuilderPanel`: prezzo con la virgola + validazione nome**
  `Float.parseFloat` rifiuta `7,50`. Accettare anche la virgola; richiedere un
  nome non vuoto per il menu.
  _Effort: basso · Rischio: basso._

- [ ] **6. Logging coerente**
  Mix di `System.out.println`, `printStackTrace()` e `java.util.logging`.
  Standardizzare su un unico Logger.
  _Effort: basso · Rischio: basso._

- [ ] **7. `printItem`: dialog di errore sull'EDT** _[STAMPA]_
  In caso di `PrinterException` il `JOptionPane` viene mostrato dal thread del
  `SwingWorker`. Spostarlo sull'EDT (pattern `showError`).
  _Effort: basso · Rischio: basso (ma tocca la stampa)._

## 🟠 Architettura / pulizia

- [ ] **8. Sotto-package + rinomine coerenti**
  Da package piatto `cassaproloco` a `model/ service/ ui/ print/`.
  Rinomine: `Cassa`→`MainFrame`, `JPanelBasket`→`BasketPanel`,
  `JPanelBasketLine`→`BasketLinePanel`,
  `PannelloResocontoVendite`→`SalesReportPanel`, `ModelloStampa`→`Receipt`.
  _Effort: medio · Rischio: basso (churn import) · Valore: leggibilità._

- [ ] **9. Disaccoppiare la stampa dalla finestra** _[STAMPA]_
  `fromCMToPPI`/`toPPI` stanno in `Cassa` e `ModelloStampa` fa
  `import static cassaproloco.Cassa.fromCMToPPI`. Spostare la geometria di stampa
  in una classe dedicata (`ReceiptPrinter`/`PrintGeometry`).
  _Effort: medio · Rischio: medio (tocca la stampa)._

- [ ] **10. `ModelloStampa` crea un `JFrame` per scontrino** _[STAMPA]_
  Il JFrame non viene mai mostrato: spreco. Renderizzare senza JFrame.
  _Effort: medio · Rischio: medio (tocca la stampa)._

- [ ] **11. Categoria "Menu" dedicata**
  I menu combinati finiscono nella griglia PRIMI (`importMenu` → `primi`),
  confonde. Spostarli in una categoria/tab propria.
  _Effort: medio · Rischio: basso._

## 🔵 Testabilità / qualità

- [ ] **12. Estrarre la logica di stampa in una funzione pura testabile**
  Oggi la costruzione "righe da stampare + righe CSV" è dentro il `SwingWorker`.
  Estrarla (carrello → lista scontrini + righe CSV) per testare Unito/Separato,
  omaggio e menu senza UI né stampante.
  _Effort: medio · Rischio: basso · Valore: alto._

- [ ] **13. CI con GitHub Actions**
  Eseguire `mvn test` a ogni push (badge sul repo).
  _Effort: basso · Rischio: nullo._

## 🟣 UX / funzionalità

- [ ] **14. Editor dei menu dall'app**
  Oggi i `.cfg` si modificano a mano; aggiungere un piccolo editor in-app.
  _Effort: alto · Rischio: basso._

- [ ] **15. Conferma prima di STAMPA / svuota carrello + contatore articoli + "rimuovi ultimo"**
  _Effort: basso/medio · Rischio: basso._

- [ ] **16. Resoconto vendite a tema**
  Apre un `JFrame` non in stile FlatLaf: integrarlo nel look del resto.
  _Effort: basso · Rischio: basso._

---

## Ordine consigliato

1. Quick win **#1–#3** (un commit): rimozione `AbsoluteLayout` + pulizia residui
   NetBeans + fix layout riga carrello.
2. **#5, #6, #12, #13**: correttezze veloci + testabilità + CI.
3. **#4** (soldi in centesimi): il più impattante sulla correttezza, da fare con
   attenzione e verifica della stampa.
4. **#8** (rinomine/sotto-package) e le voci _[STAMPA]_ **#7, #9, #10** con il
   backup `print-baseline-v1`.
5. UX **#11, #14–#16** secondo necessità.
