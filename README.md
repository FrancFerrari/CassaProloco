# CassaProloco

Applicazione di cassa (point-of-sale) per le sagre della Pro Loco, scritta in **Java Swing**.
Permette di gestire menu (primi, secondi, bevande), carrello, stampa scontrini e report vendite in CSV.

## Requisiti

- **JDK 11** (Eclipse Temurin consigliato)
- **Maven 3.9+**

Il progetto è autosufficiente: l'unica dipendenza esterna (`AbsoluteLayout`) è inclusa nel
repository locale `repo/`, quindi non serve installare nulla a mano.

## Compilare ed eseguire

```bash
# Compila, esegue i test e genera il JAR in dist/
mvn clean package

# Avvia l'applicazione
java -jar dist/CassaProloco.jar
```

> **Importante:** avviare l'app dalla cartella radice del progetto: i file di configurazione
> dei menu (`bere.cfg`, `primi.cfg`, `secondi.cfg`) vengono letti dalla directory corrente.

## Da VSCode

1. Installare l'**Extension Pack for Java**
2. Premere **F5** per avviare in debug (configurazione in `.vscode/launch.json`)
3. **Ctrl+Shift+B** per compilare il JAR (task Maven)

## Test

```bash
mvn test
```

I test JUnit 5 si trovano in `src/test/java/cassaproloco/`.

## Struttura del progetto

```
src/main/java/cassaproloco/   Codice sorgente dell'applicazione
src/test/java/cassaproloco/   Test JUnit
repo/                         Repository Maven locale (AbsoluteLayout.jar)
*.cfg                         Configurazione menu (letti a runtime)
dist/                         Output di build (JAR + librerie)
```

## Note

- Classe principale: `cassaproloco.Cassa`
- Migrato da NetBeans/Ant a Maven, mantenendo i file `.form` del GUI Builder.
