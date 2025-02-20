#!/bin/bash

# Farben für Konsolenausgaben
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

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

    # Prüfe Docker
    if ! command -v docker &> /dev/null; then
        log_error "Docker ist nicht installiert"
        exit 1
    fi
    log_info "Docker gefunden: $(docker --version)"

    # Prüfe Java
    if ! command -v java &> /dev/null; then
        log_error "Java ist nicht installiert"
        exit 1
    fi
    log_info "Java gefunden: $(java -version 2>&1 | head -n 1)"
}

# Maven Build
build_project() {
    log_info "Starte Maven Build..."
    
    # Clean und Build
    if ! mvn clean compile; then
        log_error "Maven Build fehlgeschlagen"
        exit 1
    fi
    log_info "Maven Build erfolgreich"

    # Tests ausführen
    log_info "Führe Tests aus..."
    if ! mvn test; then
        log_error "Tests fehlgeschlagen"
        exit 1
    fi
    log_info "Tests erfolgreich"

    # Paket erstellen
    log_info "Erstelle JAR-Archiv..."
    if ! mvn package assembly:single -DskipTests; then
        log_error "Paketerstellung fehlgeschlagen"
        exit 1
    fi
    log_info "JAR-Archiv erfolgreich erstellt"
}

# Docker Image bauen
build_docker_image() {
    local image_name="motion-system"
    local image_tag="latest"
    
    log_info "Baue Docker Image ${image_name}:${image_tag}..."
    
    if ! docker build -t "${image_name}:${image_tag}" .; then
        log_error "Docker Build fehlgeschlagen"
        exit 1
    fi
    log_info "Docker Image erfolgreich erstellt"

    # Optional: Test des Docker Images
    log_info "Teste Docker Image..."
    if ! docker run --rm "${image_name}:${image_tag}" java -version; then
        log_error "Docker Image Test fehlgeschlagen"
        exit 1
    fi
    log_info "Docker Image Test erfolgreich"
}

# Aufräumen
cleanup() {
    log_info "Räume temporäre Dateien auf..."
    
    # Liste der zu löschenden Verzeichnisse
    local dirs_to_clean=(
        "target"
        ".m2/repository/com/example/motion-system"
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
    # Start-Zeit speichern
    local start_time=$(date +%s)
    
    log_info "Starte Build-Prozess..."
    
    # Prüfe Voraussetzungen
    check_requirements
    
    # Build durchführen
    build_project
    
    # Docker Image erstellen
    build_docker_image
    
    # Ende-Zeit und Dauer berechnen
    local end_time=$(date +%s)
    local duration=$((end_time - start_time))
    
    log_info "Build erfolgreich abgeschlossen in ${duration} Sekunden"
}

# Skript ausführen
main "$@"