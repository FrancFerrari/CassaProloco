@echo off
rem ============================================================
rem  Crea CassaProloco.exe con Java INCLUSO (cosi' sui PC della
rem  sagra NON serve installare Java).
rem
rem  Richiede un JDK 17+ (che contiene jpackage) sul PC di sviluppo:
rem    https://adoptium.net  (Temurin 17 o 21, "JDK")
rem
rem  Uso:  prima  ->  mvn clean package
rem        poi    ->  doppio clic su questo file
rem  Risultato:    build-exe\CassaProloco\  (cartella da copiare sul PC)
rem ============================================================
cd /d "%~dp0"

where jpackage >nul 2>nul
if errorlevel 1 (
  echo.
  echo   jpackage non trovato. Serve un JDK 17+ con jpackage nel PATH.
  echo   Installa Temurin 17/21 ^(JDK^) da https://adoptium.net
  echo.
  pause
  exit /b 1
)

if not exist "dist\CassaProloco.jar" (
  echo.
  echo   Manca dist\CassaProloco.jar.  Esegui prima:   mvn clean package
  echo.
  pause
  exit /b 1
)

if exist "build-exe" rmdir /s /q "build-exe"

echo Creazione dell'app con Java incluso in corso...
jpackage ^
  --type app-image ^
  --name CassaProloco ^
  --input dist ^
  --main-jar CassaProloco.jar ^
  --main-class cassaproloco.Cassa ^
  --dest build-exe
if errorlevel 1 (
  echo.
  echo   Errore durante jpackage.
  pause
  exit /b 1
)

rem Mette i listini accanto all'exe (cartella di lavoro dell'app)
copy /y *.cfg "build-exe\CassaProloco\" >nul

echo.
echo   FATTO.  Cartella pronta:  build-exe\CassaProloco\
echo   Copia tutta quella cartella sul PC della sagra e avvia CassaProloco.exe
echo   (non serve installare Java).
echo.
pause
