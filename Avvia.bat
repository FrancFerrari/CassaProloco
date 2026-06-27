@echo off
rem ============================================================
rem  Avvio CassaProloco (doppio clic).
rem  Mettere questo file nella stessa cartella di CassaProloco.jar,
rem  della cartella lib\ e dei file .cfg (listino).
rem ============================================================
cd /d "%~dp0"

where javaw >nul 2>nul
if errorlevel 1 (
  echo.
  echo   Java non e' installato su questo computer.
  echo   Installa Java ^(JRE 8 o piu' recente^) da https://adoptium.net
  echo   e poi riavvia con un doppio clic su questo file.
  echo.
  pause
  exit /b 1
)

start "" javaw -jar CassaProloco.jar
