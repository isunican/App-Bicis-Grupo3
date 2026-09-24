@echo off
REM Genera los diagramas PNG a partir de los archivos .puml de este directorio.
REM
REM Requisitos:
REM   - Java (JRE/JDK) instalado y accesible en el PATH (para ejecutar PlantUML).
REM
REM Comportamiento:
REM   - Si plantuml.jar no existe en este directorio, lo descarga automaticamente.
REM   - Genera un archivo .png por cada archivo .puml de este directorio.
REM
REM Uso:
REM   generate_diagrams.bat
setlocal enabledelayedexpansion

set "SCRIPT_DIR=%~dp0"
cd /d "%SCRIPT_DIR%"

set "PLANTUML_JAR=%SCRIPT_DIR%plantuml.jar"
set "PLANTUML_VERSION=v1.2026.6"
set "PLANTUML_URL=https://github.com/plantuml/plantuml/releases/download/%PLANTUML_VERSION%/plantuml-%PLANTUML_VERSION:~1%.jar"

REM 1. Comprobar que Java esta instalado
where java >nul 2>&1
if errorlevel 1 (
  echo ERROR: Java no esta instalado o no esta en el PATH.
  echo Instala un JRE/JDK (por ejemplo, desde https://adoptium.net/) y vuelve a intentarlo.
  exit /b 1
)

REM 2. Descargar plantuml.jar si no existe
if not exist "%PLANTUML_JAR%" (
  echo Descargando PlantUML %PLANTUML_VERSION%...
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -Uri '%PLANTUML_URL%' -OutFile '%PLANTUML_JAR%'"
  if errorlevel 1 (
    echo ERROR: No se pudo descargar PlantUML.
    exit /b 1
  )
  echo PlantUML descargado.
)

REM 3. Generar los PNG
echo Generando diagramas...
for %%f in (*.puml) do (
  java -Djava.awt.headless=true -jar "%PLANTUML_JAR%" -png "%%f"
)
echo Diagramas generados correctamente.

endlocal
