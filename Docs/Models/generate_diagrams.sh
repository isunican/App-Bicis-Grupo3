#!/usr/bin/env bash
# Genera los diagramas PNG a partir de los archivos .puml de este directorio.
#
# Requisitos:
#   - Java (JRE/JDK) instalado y accesible en el PATH (para ejecutar PlantUML).
#
# Comportamiento:
#   - Si plantuml.jar no existe en este directorio, lo descarga automaticamente.
#   - Genera un archivo .png por cada archivo .puml de este directorio.
#
# Uso:
#   ./generate_diagrams.sh
set -euo pipefail

# Directorio donde se encuentra este script (Docs/Models)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

PLANTUML_JAR="$SCRIPT_DIR/plantuml.jar"
PLANTUML_VERSION="v1.2026.6"
PLANTUML_URL="https://github.com/plantuml/plantuml/releases/download/${PLANTUML_VERSION}/plantuml-${PLANTUML_VERSION#v}.jar"

# 1. Comprobar que Java esta instalado
if ! command -v java >/dev/null 2>&1; then
  echo "ERROR: Java no esta instalado o no esta en el PATH." >&2
  echo "Instala un JRE/JDK (por ejemplo, desde https://adoptium.net/) y vuelve a intentarlo." >&2
  exit 1
fi

# 2. Descargar plantuml.jar si no existe
if [ ! -f "$PLANTUML_JAR" ]; then
  echo "Descargando PlantUML ${PLANTUML_VERSION}..."
  if command -v curl >/dev/null 2>&1; then
    curl -L -o "$PLANTUML_JAR" "$PLANTUML_URL"
  elif command -v wget >/dev/null 2>&1; then
    wget -O "$PLANTUML_JAR" "$PLANTUML_URL"
  else
    echo "ERROR: No se encontro curl ni wget para descargar PlantUML." >&2
    exit 1
  fi
  echo "PlantUML descargado."
fi

# 3. Generar los PNG
echo "Generando diagramas..."
java -Djava.awt.headless=true -jar "$PLANTUML_JAR" -png ./*.puml
echo "Diagramas generados correctamente."
