# Installazione CassaProloco (produzione)

Guida per mettere l'app sui PC della sagra. Due strade:

- **A) Cartella con jar + Java installato** — semplice, ma serve Java su ogni PC.
- **B) Exe con Java incluso** — un po' più di lavoro in preparazione, ma sui PC della
  sagra **non serve installare Java**.

---

## Prima di tutto: costruire l'app

Sul PC di sviluppo, nella cartella del progetto:

```
mvn clean package
```

Genera la cartella **`dist\`** già pronta:

```
dist\
  CassaProloco.jar
  lib\            (gson, flatlaf)
  bere.cfg primi.cfg secondi.cfg
  Avvia.bat
```

I 49 test devono essere verdi (vengono eseguiti durante `package`).

---

## A) Cartella con jar (serve Java)

1. Su **ogni** PC installa **Java JRE 8 o più recente** → https://adoptium.net
2. Copia la cartella **`dist\`** sul PC (rinominala es. `C:\CassaProloco\`).
3. Avvia con **doppio clic su `Avvia.bat`**.
   - Se manca Java, il file lo segnala.

I dati (vendite, menu, impostazioni, log) vengono creati **dentro quella cartella**.

---

## B) Exe con Java incluso (consigliato sui PC sagra)

Sul PC di sviluppo serve **una volta** un **JDK 17+** (contiene `jpackage`):
https://adoptium.net (Temurin 17 o 21, tipo **JDK**).

```
mvn clean package
build-exe.bat
```

Risultato: **`build-exe\CassaProloco\`** con dentro `CassaProloco.exe`, il runtime Java
e i listini. Copia **tutta quella cartella** sul PC della sagra (es. `C:\CassaProloco\`,
in una posizione **scrivibile** — non `Programmi`) e avvia **`CassaProloco.exe`**.
Non serve installare Java.

---

## Stampante (su ogni PC)

1. Installa il driver **Brother QL‑570**.
2. Imposta il formato rotolo nel driver (Preferenze di stampa):
   - **62 mm**: formato "62mm" (esce a ~29 mm) — come già funziona.
   - **54 mm**: formato **"54mm" con Lunghezza = 40 mm**.
3. La stampante può essere quella **predefinita** del PC.

## Impostazioni nell'app (su ogni PC)

Apri **IMPOSTAZIONI**:
- **ID cassa**: `1` sul primo PC, `2` sul secondo (vuoto se cassa singola).
- **Rullino**: 62 o 54 mm (la stampante di QUEL PC).
- **Cartella condivisa**: vedi sotto (vuoto se cassa singola).
- **Modifica scontrino…** / **Menu e listino…**: personalizzazioni.

## Database comune (2 casse in rete locale, anche senza internet)

1. Collega i due PC in **rete locale** (cavo di rete tra i due, oppure un router/switch).
2. Su un PC crea una cartella e **condividila** (es. `C:\Cassa-condivisa` → `\\PC1\Cassa-condivisa`),
   con permessi di **lettura/scrittura**.
3. Su **entrambe** le casse: IMPOSTAZIONI → **Cartella condivisa** = quel percorso.
4. Allinea i listini/menu: su Cassa 1 premi **"Invia listino/menu → condivisa"**,
   su Cassa 2 premi **"Prendi listino/menu ← condivisa"**.

Come funziona: ogni cassa salva le vendite **in locale** (non si perdono mai) e le
**copia nella condivisa**; il **Resoconto somma entrambe** le casse per giornata.

---

## Prova prima della sagra (da non saltare)

- Stampa reale su **62 mm** e su **54 mm**.
- Una vendita su **ogni** cassa → apri **Resoconto** e verifica il totale **combinato**.
- Prova **Chiusura cassa** (mostra il totale e chiude la giornata).
- **Riavvia** l'app a metà: deve **riprendere** la giornata aperta.

## Se qualcosa va storto

Nella cartella dei dati c'è il file **`cassa.0.log`** (e successivi): contiene gli errori
e aiuta a capire cosa è successo.
