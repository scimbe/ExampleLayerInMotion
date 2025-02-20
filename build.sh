#!/bin/bash

# Source Java environment setup script
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
source "$SCRIPT_DIR/setup-java-env.sh"

# Farben für Konsolenausgaben
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Konfigurationsvariablen
SONAR_HOST_URL=${SONAR_HOST_URL:-"http://localhost:9000"}
SONAR_PROJECT_KEY=${SONAR_PROJECT_KEY:-"com.example:motion-system"}
SONAR_SCANNER_VERSION="4.7.0.2747"
SONAR_SCANNER_DIR="$HOME/.sonar/sonar-scanner"

# Logging-Funktionen
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Prüfe ob notwendige Tools installiert sind
check_requirements() {
    log_info "Prüfe Systemvoraussetzungen..."

    # Prüfe Maven
    if ! command -v mvn &> /dev/null; then
        log_error "Maven ist nicht installiert"
        exit 1
    fi
    log_info "Maven gefunden: $(mvn --version | head -n 1)"

    # Prüfe Java
    if ! command -v java &> /dev/null; then
        log_error "Java ist nicht installiert"
        exit 1
    fi
    log_info "Java gefunden: $(java -version 2>&1 | head -n 1)"

    # Prüfe SonarScanner
    check_sonar_scanner

    # Prüfe Docker (optional)
    if command -v docker &> /dev/null; then
        DOCKER_AVAILABLE=1
        log_info "Docker gefunden: $(docker --version)"
    else
        DOCKER_AVAILABLE=0
        log_warn "Docker ist nicht installiert. Docker-bezogene Schritte werden übersprungen."
    fi
}

# SonarScanner Installation prüfen und ggf. durchführen
check_sonar_scanner() {
    if ! command -v sonar-scanner &> /dev/null; then
        if [ ! -d "$SONAR_SCANNER_DIR" ]; then
            log_warn "SonarScanner nicht gefunden. Starte Installation..."
            install_sonar_scanner
        fi
        export PATH="$SONAR_SCANNER_DIR/bin:$PATH"
    fi
    log_info "SonarScanner gefunden: $(sonar-scanner --version 2>&1 | head -n 1)"
}

# SonarScanner Installation
install_sonar_scanner() {
  local os_type
  case "$(uname -s)" in
    Linux*)   os_type="linux";;
    Darwin*)  os_type="macosx";;
    *)        log_error "Betriebssystem nicht unterstützt"; exit 1;;
  esac

  local download_url="https://binaries.sonarsource.com/Distribution/sonar-scanner-cli/sonar-scanner-cli-${SONAR_SCANNER_VERSION}-${os_type}.zip"
  local temp_dir=$(mktemp -d -t sonar-scanner-XXXXXX)

  log_info "Lade SonarScanner herunter..."
  curl -L "$download_url" -o "$temp_dir/sonar-scanner.zip"

  log_info "Installiere SonarScanner..."
  unzip -q "$temp_dir/sonar-scanner.zip" -d "$temp_dir"
  mkdir -p "$HOME/.sonar"
  mv "$temp_dir/sonar-scanner-${SONAR_SCANNER_VERSION}-${os_type}" "$SONAR_SCANNER_DIR"
  rm -rf "$temp_dir"

  log_info "SonarScanner Installation abgeschlossen"
}

# Prüfe SonarQube-Server
check_sonar_server() {
  log_info "Prüfe SonarQube-Server..."
  if curl -s "$SONAR_HOST_URL/api/system/status" | grep -E '"status":"UP"' > /dev/null; then
    log_info "SonarQube-Server ist erreichbar"
    return 0
  else
    log_error "SonarQube-Server nicht erreichbar: $SONAR_HOST_URL"
    log_info "Stelle sicher, dass der Server läuft und erreichbar ist"
    return 1
  fi
}

# Maven Build
build_project() {
    log_info "Starte Maven Build..."

    if ! mvn clean compile; then
        log_error "Maven Build fehlgeschlagen"
        exit 1
    fi
    log_info "Maven Build erfolgreich"

    log_info "Führe Tests aus..."
    if ! mvn test; then
        log_error "Tests fehlgeschlagen"
        exit 1
    fi
    log_info "Tests erfolgreich"

    log_info "Erstelle JaCoCo Report..."
    if ! mvn jacoco:report; then
        log_error "JaCoCo Report Erstellung fehlgeschlagen"
        exit 1
    fi
    log_info "JaCoCo Report erstellt"

    log_info "Erstelle JAR-Archiv..."
    if ! mvn package assembly:single -DskipTests; then
        log_error "Paketerstellung fehlgeschlagen"
        exit 1
    fi
    log_info "JAR-Archiv erfolgreich erstellt"
}

# SonarQube Analyse
run_sonar_analysis() {
    if ! check_sonar_server; then
        log_warn "Überspringe SonarQube-Analyse"
        return
    fi

    log_info "Starte SonarQube-Analyse..."

    if [ -z "$SONAR_TOKEN" ]; then
        log_error "SONAR_TOKEN nicht gesetzt"
        log_info "Bitte setzen Sie die Umgebungsvariable SONAR_TOKEN"
        return 1
    fi

    sonar-scanner \
        -Dsonar.host.url="$SONAR_HOST_URL" \
        -Dsonar.projectKey="$SONAR_PROJECT_KEY" \
        -Dsonar.java.binaries=target/classes \
        -Dsonar.sources=src/main/java \
        -Dsonar.tests=src/test/java \
        -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml \
        -Dsonar.junit.reportPaths=target/surefire-reports \
        -Dsonar.java.coveragePlugin=jacoco \
        -Dsonar.login="$SONAR_TOKEN"

    if [ $? -eq 0 ]; then
        log_info "SonarQube-Analyse erfolgreich abgeschlossen"
        log_info "Dashboard: $SONAR_HOST_URL/dashboard?id=$SONAR_PROJECT_KEY"
    else
        log_error "SonarQube-Analyse fehlgeschlagen"
        return 1
    fi
}

# Docker Image bauen
build_docker_image() {
    if [ $DOCKER_AVAILABLE -eq 0 ]; then
        log_warn "Docker nicht verfügbar. Überspringe Docker Build."
        return
    fi

    local image_name="motion-system"
    local image_tag="latest"

    log_info "Baue Docker Image ${image_name}:${image_tag}..."

    if ! docker build -t "${image_name}:${image_tag}" .; then
        log_error "Docker Build fehlgeschlagen"
        exit 1
    fi
    log_info "Docker Image erfolgreich erstellt"

    log_info "Teste Docker Image..."
    if ! docker run --rm --entrypoint java "${image_name}:${image_tag}" -version; then
        log_error "Docker Image Java-Versionstest fehlgeschlagen"
        exit 1
    fi
    log_info "Docker Image Java-Versionstest erfolgreich"
}

# Aufräumen
cleanup() {
    log_info "Räume temporäre Dateien auf..."

    local dirs_to_clean=(
        "target"
        "$HOME/.m2/repository/com/example/motion-system"
        "$HOME/.sonar/cache"
    )

    for dir in "${dirs_to_clean[@]}"; do
        if [ -d "$dir" ]; then
            rm -rf "$dir"
            log_info "Verzeichnis gelöscht: $dir"
        fi
    done
}

# Hauptfunktion
main() {
    local do_clean=0
    local do_build=0
    local do_sonar=0
    local do_docker=0

    if [ $# -eq 0 ]; then
        do_clean=1
        do_build=1
        do_sonar=1
        do_docker=1
    fi

    while [ "$1" != "" ]; do
        case $1 in
            -h | --help )    show_help
                            exit
                            ;;
            -c | --clean )   do_clean=1
                            ;;
            -b | --build )   do_build=1
                            ;;
            -s | --sonar )   do_sonar=1
                            ;;
            -d | --docker )  do_docker=1
                            ;;
            -a | --all )     do_clean=1
                            do_build=1
                            do_sonar=1
                            do_docker=1
                            ;;
            * )             log_error "Unbekannte Option: $1"
                            show_help
                            exit 1
        esac
        shift
    done

    local start_time=$(date +%s)

    log_info "Starte Build-Prozess..."

    check_requirements

    [ $do_clean -eq 1 ] && cleanup
    [ $do_build -eq 1 ] && build_project
    [ $do_sonar -eq 1 ] && run_sonar_analysis
    [ $do_docker -eq 1 ] && build_docker_image

    local end_time=$(date +%s)
    local duration=$((end_time - start_time))

    log_info "Build erfolgreich abgeschlossen in ${duration} Sekunden"
}

# Skript ausführen
main "$@"
