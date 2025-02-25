#!/bin/bash

# Charakterbewegungssystem - Build-Skript
# Version: 1.1
# Dieses Skript automatisiert den Build-Prozess für das Charakterbewegungssystem mit
# disjunkten und klar definierten Build-Pfaden

# Source Java environment setup script
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
source "$SCRIPT_DIR/setup-java-env.sh"

# Farben für Konsolenausgaben
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Konfigurationsvariablen
SONAR_HOST_URL=${SONAR_HOST_URL:-"http://localhost:9000"}
SONAR_PROJECT_KEY=${SONAR_PROJECT_KEY:-"com.example:motion-system"}
SONAR_SCANNER_VERSION="4.7.0.2747"
SONAR_SCANNER_DIR="$HOME/.sonar/sonar-scanner"
PROJECT_NAME="Character Motion System"
JAR_NAME="motion-system-1.0-SNAPSHOT.jar"

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

log_section() {
    echo -e "\n${BLUE}=== $1 ===${NC}"
}

# Hilfe anzeigen
show_help() {
    echo -e "${BLUE}${PROJECT_NAME} - Build-Skript${NC}"
    echo "Dieses Skript automatisiert den Build und die Analyse des Charakterbewegungssystems."
    echo ""
    echo "Nutzung: $0 [Optionen]"
    echo ""
    echo "Optionen:"
    echo "  -h, --help           Diese Hilfe anzeigen"
    echo ""
    echo "  Build-Optionen:"
    echo "  -c, --clean          Cleanup durchführen (löscht target/ und andere temporäre Dateien)"
    echo "  -b, --build          Projekt kompilieren und Tests ausführen"
    echo "  -p, --package        Erstellt das ausführbare JAR-Paket (impliziert --build wenn nicht bereits gebaut)"
    echo "  -r, --run            Führt die Anwendung aus (impliziert --package wenn nicht bereits gebaut)"
    echo ""
    echo "  Analyse-Optionen:"
    echo "  -t, --test           Führt nur die Tests aus"
    echo "  -j, --jacoco         Generiert JaCoCo-Coverage-Report"
    echo "  -s, --sonar          SonarQube-Analyse durchführen (erfordert laufenden SonarQube-Server)"
    echo ""
    echo "  Container-Optionen:"
    echo "  -d, --docker         Docker-Image bauen"
    echo "  --docker-run         Docker-Container mit der Anwendung starten"
    echo ""
    echo "  Kombinierte Optionen:"
    echo "  -a, --all            Alle Build- und Docker-Schritte ausführen (clean, build, package, docker)"
    echo "  --all-sonar          Alle Schritte inklusive SonarQube ausführen"
    echo "  --dev                Entwicklermodus: clean, build, package, run"
    echo ""
    echo "Beispiele:"
    echo "  $0 --build              # Nur bauen und testen"
    echo "  $0 --dev                # Entwicklermodus: bauen und ausführen"
    echo "  $0 --all                # Vollständiger Build-Prozess"
    echo "  $0 --clean --sonar      # Bereinigen und SonarQube-Analyse durchführen"
    echo ""
}

# Prüfe ob notwendige Tools installiert sind
check_requirements() {
    log_section "Systemanforderungen prüfen"

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
    java_version=$(java -version 2>&1 | head -n 1)
    log_info "Java gefunden: $java_version"
    
    # Warne bei Java-Version > 21, da JaCoCo Probleme haben könnte
    if [[ "$java_version" == *"openjdk"*"23"* ]]; then
        log_warn "Java 23 erkannt - JaCoCo könnte Probleme haben. Nutzen Sie '--jacoco' mit Vorsicht."
    fi

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
    log_section "SonarScanner prüfen"
    
    if ! command -v sonar-scanner &> /dev/null; then
        if [ ! -d "$SONAR_SCANNER_DIR" ]; then
            log_warn "SonarScanner nicht gefunden. Starte Installation..."
            install_sonar_scanner
        fi
        export PATH="$SONAR_SCANNER_DIR/bin:$PATH"
    fi
    
    if command -v sonar-scanner &> /dev/null; then
        log_info "SonarScanner verfügbar: $(sonar-scanner --version 2>&1 | head -n 1)"
    else
        log_error "SonarScanner konnte nicht installiert werden"
        return 1
    fi
    
    return 0
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

# Kompilieren (ohne Tests)
compile_project() {
    log_section "Kompiliere Projekt"
    
    # Maven Compile ausführen
    if ! mvn clean compile; then
        log_error "Maven Kompilierung fehlgeschlagen"
        exit 1
    fi
    
    log_info "Kompilierung erfolgreich abgeschlossen"
    return 0
}

# Tests ausführen
run_tests() {
    log_section "Führe Tests aus"
    
    # Prüfe, ob JAR bereits gebaut wurde
    if [ ! -d "target/classes" ]; then
        log_warn "Kompilieren ist erforderlich vor dem Testen"
        compile_project
    fi
    
    # Maven-Tests ausführen
    if ! mvn test; then
        log_error "Tests fehlgeschlagen"
        return 1
    fi
    
    log_info "Tests erfolgreich abgeschlossen"
    return 0
}

# JaCoCo Report erstellen
run_jacoco() {
    log_section "Generiere JaCoCo Coverage Report"
    
    # Prüfe, ob Tests ausgeführt wurden
    if [ ! -f "target/jacoco.exec" ]; then
        log_warn "Keine JaCoCo-Execution-Daten gefunden. Führe Tests aus..."
        if ! run_tests; then
            return 1
        fi
    fi
    
    # JaCoCo Report erstellen
    if ! mvn jacoco:report; then
        log_error "JaCoCo Report Erstellung fehlgeschlagen"
        return 1
    fi
    
    log_info "JaCoCo Report erfolgreich erstellt: target/site/jacoco/index.html"
    return 0
}

# Maven Build mit Tests
build_project() {
    log_section "Baue Projekt mit Tests"
    
    # Kompilieren
    if ! compile_project; then
        return 1
    fi
    
    # Tests ausführen
    if ! run_tests; then
        return 1
    fi
    
    log_info "Build erfolgreich abgeschlossen"
    return 0
}

# Packet (JAR) erstellen
package_project() {
    log_section "Erstelle JAR-Datei"
    
    # Prüfe, ob das Projekt bereits kompiliert wurde
    if [ ! -d "target/classes" ]; then
        log_warn "Projekt muss vor dem Packaging gebaut werden"
        if ! build_project; then
            return 1
        fi
    fi
    
    # Maven package ausführen (ohne Tests erneut auszuführen)
    if ! mvn package -DskipTests; then
        log_error "Maven Packaging fehlgeschlagen"
        return 1
    fi
    
    local jar_path="target/$JAR_NAME"
    if [ -f "$jar_path" ]; then
        log_info "JAR-Datei erfolgreich erstellt: $jar_path"
        log_info "Sie können die Anwendung mit 'java -jar $jar_path' ausführen"
        return 0
    else
        log_error "JAR-Datei wurde nicht gefunden"
        return 1
    fi
}

# Anwendung ausführen
run_application() {
    log_section "Führe Anwendung aus"
    
    local jar_path="target/$JAR_NAME"
    
    # Prüfe, ob JAR existiert
    if [ ! -f "$jar_path" ]; then
        log_warn "JAR-Datei nicht gefunden, erstelle Paket..."
        if ! package_project; then
            return 1
        fi
    fi
    
    log_info "Starte Anwendung: $jar_path"
    log_info "Die Anwendung ist unter http://localhost:8080 erreichbar"
    log_info "Drücken Sie Strg+C zum Beenden"
    
    # Führe JAR aus
    java -jar "$jar_path"
    
    return $?
}

# SonarQube Analyse
run_sonar_analysis() {
    log_section "Führe SonarQube-Analyse durch"
    
    if ! check_sonar_scanner; then
        log_error "SonarScanner ist nicht verfügbar"
        return 1
    fi
    
    if ! check_sonar_server; then
        log_warn "Überspringe SonarQube-Analyse, da Server nicht erreichbar ist"
        return 1
    fi
    
    if [ -z "$SONAR_TOKEN" ]; then
        log_error "SONAR_TOKEN nicht gesetzt"
        log_info "Bitte setzen Sie die Umgebungsvariable SONAR_TOKEN mit einem gültigen Token"
        return 1
    fi
    
    # Stelle sicher, dass Tests ausgeführt wurden (für Coverage)
    if [ ! -f "target/site/jacoco/jacoco.xml" ]; then
        log_warn "JaCoCo-Bericht nicht gefunden, führe Tests aus..."
        if ! run_jacoco; then
            log_warn "JaCoCo-Bericht konnte nicht erstellt werden, führe SonarQube ohne Coverage aus"
        fi
    fi
    
    log_info "Starte SonarQube-Analyse..."
    if ! mvn sonar:sonar -Dsonar.skip=false; then
        log_error "SonarQube-Analyse fehlgeschlagen"
        return 1
    fi
    
    log_info "SonarQube-Analyse erfolgreich abgeschlossen"
    log_info "Dashboard: $SONAR_HOST_URL/dashboard?id=$SONAR_PROJECT_KEY"
    return 0
}

# Docker Image bauen
build_docker_image() {
    log_section "Baue Docker Image"
    
    if [ $DOCKER_AVAILABLE -eq 0 ]; then
        log_error "Docker ist nicht verfügbar. Installation erforderlich."
        return 1
    fi
    
    local image_name="motion-system"
    local image_tag="latest"
    
    # Stelle sicher, dass das JAR erstellt wurde
    local jar_path="target/$JAR_NAME"
    if [ ! -f "$jar_path" ]; then
        log_warn "JAR-Datei nicht gefunden, erstelle Paket..."
        if ! package_project; then
            return 1
        fi
    fi
    
    log_info "Baue Docker Image ${image_name}:${image_tag}..."
    if ! docker build -t "${image_name}:${image_tag}" .; then
        log_error "Docker Build fehlgeschlagen"
        return 1
    fi
    
    log_info "Docker Image erfolgreich erstellt: ${image_name}:${image_tag}"
    
    # Teste das Docker Image
    log_info "Teste Docker Image..."
    if ! docker run --rm --entrypoint java "${image_name}:${image_tag}" -version; then
        log_error "Docker Image Test fehlgeschlagen"
        return 1
    fi
    
    log_info "Docker Image Test erfolgreich"
    log_info "Sie können das Image mit 'docker run -p 8080:8080 ${image_name}:${image_tag}' starten"
    
    return 0
}

# Docker Container starten
run_docker_container() {
    log_section "Starte Docker Container"
    
    if [ $DOCKER_AVAILABLE -eq 0 ]; then
        log_error "Docker ist nicht verfügbar. Installation erforderlich."
        return 1
    fi
    
    local image_name="motion-system"
    local image_tag="latest"
    local container_name="motion-system-app"
    
    # Prüfe, ob das Image existiert
    if ! docker image inspect "${image_name}:${image_tag}" &>/dev/null; then
        log_warn "Docker Image nicht gefunden, erstelle es..."
        if ! build_docker_image; then
            return 1
        fi
    fi
    
    # Stoppe und entferne vorhandenen Container, falls vorhanden
    if docker ps -a | grep -q "$container_name"; then
        log_info "Stoppe und entferne existierenden Container..."
        docker stop "$container_name" &>/dev/null
        docker rm "$container_name" &>/dev/null
    fi
    
    log_info "Starte Docker Container: $container_name"
    if ! docker run --name "$container_name" -p 8080:8080 -d "${image_name}:${image_tag}"; then
        log_error "Konnte Docker Container nicht starten"
        return 1
    fi
    
    log_info "Docker Container erfolgreich gestartet"
    log_info "Die Anwendung ist unter http://localhost:8080 erreichbar"
    log_info "Container-Logs anzeigen: docker logs $container_name"
    log_info "Container stoppen: docker stop $container_name"
    
    return 0
}

# Aufräumen
cleanup() {
    log_section "Räume Projektdateien auf"
    
    local dirs_to_clean=(
        "target"
        "$HOME/.m2/repository/com/example/motion-system"
    )
    
    for dir in "${dirs_to_clean[@]}"; do
        if [ -d "$dir" ]; then
            log_info "Lösche Verzeichnis: $dir"
            rm -rf "$dir"
        fi
    done
    
    # Optional: SonarQube Cache löschen
    if [ "$1" == "full" ]; then
        local sonar_cache="$HOME/.sonar/cache"
        if [ -d "$sonar_cache" ]; then
            log_info "Lösche SonarQube Cache: $sonar_cache"
            rm -rf "$sonar_cache"
        fi
    fi
    
    log_info "Aufräumarbeiten abgeschlossen"
    return 0
}

# Entwicklermodus
dev_mode() {
    log_section "Entwicklermodus"
    
    # Clean und Build
    cleanup
    if ! build_project; then
        return 1
    fi
    
    # Package und Run
    if ! package_project; then
        return 1
    fi
    
    # Anwendung starten
    run_application
    
    return $?
}

# Hauptfunktion
main() {
    local do_clean=0
    local do_compile=0
    local do_build=0
    local do_test=0
    local do_jacoco=0
    local do_package=0
    local do_run=0
    local do_sonar=0
    local do_docker=0
    local do_docker_run=0
    local do_dev=0
    
    # Ohne Argumente: Hilfe anzeigen
    if [ $# -eq 0 ]; then
        show_help
        exit 0
    fi
    
    # Verarbeite Kommandozeilenargumente
    while [ "$1" != "" ]; do
        case $1 in
            -h | --help )        show_help
                                exit 0
                                ;;
            -c | --clean )       do_clean=1
                                ;;
            --compile )          do_compile=1
                                ;;
            -b | --build )       do_build=1
                                ;;
            -t | --test )        do_test=1
                                ;;
            -j | --jacoco )      do_jacoco=1
                                ;;
            -p | --package )     do_package=1
                                ;;
            -r | --run )         do_run=1
                                ;;
            -s | --sonar )       do_sonar=1
                                ;;
            -d | --docker )      do_docker=1
                                ;;
            --docker-run )       do_docker=1
                                do_docker_run=1
                                ;;
            -a | --all )         do_clean=1
                                do_build=1
                                do_package=1
                                do_docker=1
                                ;;
            --all-sonar )        do_clean=1
                                do_build=1
                                do_jacoco=1
                                do_package=1
                                do_sonar=1
                                do_docker=1
                                ;;
            --dev )              do_dev=1
                                ;;
            * )                 log_error "Unbekannte Option: $1"
                                show_help
                                exit 1
        esac
        shift
    done
    
    local start_time=$(date +%s)
    
    log_info "Character Motion System Build-Prozess gestartet"
    
    # Systemanforderungen prüfen
    check_requirements
    
    # Entwicklermodus (shorthand für clean+build+package+run)
    if [ $do_dev -eq 1 ]; then
        dev_mode
        exit $?
    fi
    
    # Einzelne Schritte ausführen
    [ $do_clean -eq 1 ] && cleanup
    [ $do_compile -eq 1 ] && compile_project
    [ $do_build -eq 1 ] && build_project
    [ $do_test -eq 1 ] && run_tests
    [ $do_jacoco -eq 1 ] && run_jacoco
    [ $do_package -eq 1 ] && package_project
    [ $do_sonar -eq 1 ] && run_sonar_analysis
    [ $do_docker -eq 1 ] && build_docker_image
    [ $do_docker_run -eq 1 ] && run_docker_container
    [ $do_run -eq 1 ] && run_application
    
    local end_time=$(date +%s)
    local duration=$((end_time - start_time))
    
    log_section "Build-Prozess abgeschlossen"
    log_info "Gesamtdauer: ${duration} Sekunden"
    
    return 0
}

# Skript ausführen
main "$@"
