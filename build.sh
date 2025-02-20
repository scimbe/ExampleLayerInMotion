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

# Rest of the script remains the same as in the previous version
# (Include the entire previous content of build.sh here)
