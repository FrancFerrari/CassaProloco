# CassaProloco

Applicazione di cassa (point-of-sale) per le sagre della Pro Loco, scritta in **Java Swing**
con look moderno (**FlatLaf**). Gestisce menu (primi, secondi, bevande), carrello, menu
combinati, stampa scontrini su stampante etichette e report vendite giornalieri in CSV.

## Requisiti

- **JDK 11** (Eclipse Temurin consigliato)
- **Maven 3.9+**

Le dipendenze (AbsoluteLayout, Gson, FlatLaf, JUnit) sono risolte da Maven; `AbsoluteLayout`
è incluso nel repository locale `repo/`, quindi il progetto è autosufficiente.

## Compilare ed eseguire

```bash
# Compila, esegue i test e genera il JAR in dist/
mvn clean package

# Avvia l'applicazione
java -jar dist/CassaProloco.jar
```

> I file di configurazione dei menu (`bere.cfg`, `primi.cfg`, `secondi.cfg`), i report CSV e
> `groupedItems.json` vengono cercati in modo robusto rispetto alla posizione del jar/classi
> (vedi `AppPaths`): l'app funziona anche se avviata da una cartella diversa.

## Da VSCode

1. Installare l'**Extension Pack for Java**
2. Premere **F5** per avviare in debug (configurazione in `.vscode/launch.json`)
3. **Ctrl+Shift+B** per compilare il JAR (task Maven)

## Test

```bash
mvn test
```

I test JUnit 5 sono in `src/test/java/cassaproloco/`.

**CI:** è pronto un workflow GitHub Actions in [`docs/ci-workflow.yml`](docs/ci-workflow.yml)
(build + test a ogni push/PR). Per attivarlo, copiarlo in `.github/workflows/ci.yml`
(dal web GitHub, oppure da CLI dopo `gh auth refresh -s workflow`).

## Architettura

```
cassaproloco/
  Cassa                  Finestra principale (compone UI e cabla i servizi)
  MenuBuilderPanel       Form per creare i menu combinati
  Theme                  Palette colori / font centralizzati
  AppPaths               Risoluzione robusta della cartella dei dati
  model:
    Item                 Voce di menu immutabile (equals/hashCode coerenti)
    Basket               Carrello (override prezzo per l'Omaggio, senza mutare Item)
    GroupedItem          Menu combinato (Builder + EnumMap)
  servizi:
    MenuConfigLoader     Parsing dei .cfg (UTF-8)
    GroupedItemStore     Persistenza menu in JSON (Gson) + migrazione da .ser
    SalesRecorder        Append vendite su CSV (Csv = RFC 4180, Locale.ROOT)
    SalesReportRepository Lettura/aggregazione CSV per il Resoconto
  stampa:
    ModelloStampa, Paint Rendering dello scontrino (Printable)
    ReceiptPreview       Anteprima dello scontrino su PNG (test senza stampante)
  componenti UI:
    JPanelBasket, JPanelBasketLine, ModernButtonUI, RoundedTextField, ...
```

Struttura file: `src/main/java`, `src/test/java`, `repo/` (Maven locale),
`*.cfg` (menu), `dist/` (build), `docs/` (note, incluso `docs/stampa.md`).

## Stampa

Lo scontrino è 6.2×4 cm; la stampa va sulla stampante predefinita di sistema. Il flusso e il
formato sono documentati in [`docs/stampa.md`](docs/stampa.md). Lo stato funzionante è
marcato dal tag git `print-baseline-v1`.

Per vedere l'aspetto dello scontrino senza stampante:

```bash
java -cp "target/classes;dist/lib/*" cassaproloco.ReceiptPreview
```

## Note

- Classe principale (entry point): `cassaproloco.Cassa`
- Migrato da NetBeans/Ant a Maven; UI riscritta a mano (niente più file `.form`).
