# Character Motion System

Ein Layer-basiertes System zur Verwaltung und Steuerung von Charakterbewegungen in verteilten Systemen.

## Projektübersicht

Das Character Motion System implementiert eine dreischichtige Architektur zur Verwaltung von Charakterbewegungen:
- Motion-API-Schicht für externe Schnittstellen
- Motion-Logik-Schicht für Bewegungsberechnungen
- Motion-Daten-Schicht für Persistenz

## Voraussetzungen

- Java JDK 17 oder höher
- Maven 3.9.4 oder höher
- Docker (optional)
- SonarQube Server (für Code-Qualitätsanalyse)

## Quick Start

1. Repository klonen:
   ```bash
   git clone https://github.com/scimbe/ExampleLayerInMotion.git
   cd ExampleLayerInMotion
   ```

2. Build ausführen:
   ```bash
   chmod +x build.sh
   ./build.sh --all
   ```

## SonarQube Integration

### Lokale Einrichtung

1. SonarQube Server starten:
   ```bash
   docker run -d --name sonarqube -p 9000:9000 sonarqube:latest
   ```

2. Initiale Einrichtung:
   - Öffnen Sie http://localhost:9000 (Standard-Login: admin/admin)
   - Erstellen Sie ein neues Projekt
   - Generieren Sie einen Token unter User > My Account > Security
   - Setzen Sie die Umgebungsvariablen:
     ```bash
     export SONAR_TOKEN="your-token"
     export SONAR_HOST_URL="http://localhost:9000"
     ```

### Lokale Analyse ausführen

Das build.sh-Skript bietet verschiedene Optionen für die SonarQube-Analyse:

```bash
# Nur SonarQube-Analyse
./build.sh --sonar

# Vollständiger Build mit Analyse
./build.sh --all

# Hilfe anzeigen
./build.sh --help
```

### Analyse in der CI/CD-Pipeline

Die Jenkins-Pipeline führt automatisch eine SonarQube-Analyse durch:

1. Konfiguration in Jenkins:
   - Installieren Sie das SonarQube Scanner Plugin
   - Konfigurieren Sie die SonarQube Server-Instanz unter "Configure System"
   - Fügen Sie die Credentials für SONAR_TOKEN hinzu

2. Pipeline ausführen:
   - Die Analyse wird automatisch nach den Tests ausgeführt
   - Quality Gates werden überprüft
   - Detaillierte Metriken werden erfasst

### Ergebnisse überprüfen

1. SonarQube Dashboard:
   - Öffnen Sie http://localhost:9000
   - Navigieren Sie zu Ihrem Projekt
   - Überprüfen Sie:
     - Code Coverage
     - Code Smells
     - Bugs und Vulnerabilities
     - Duplications
     - Quality Gate Status

2. Quality Gates:
   - Standard-Regeln:
     - Mindestens 80% Coverage
     - Keine kritischen Bugs
     - Keine Vulnerabilities
     - Maximale technische Schuld: 5 Tage

3. Jenkins Integration:
   - Quality Gate Status im Build
   - Detaillierte Analyse im "SonarQube" Tab
   - E-Mail-Benachrichtigungen bei Qualitätsproblemen

### Problembehandlung

1. Analyse schlägt fehl:
   - Prüfen Sie die Verbindung zum SonarQube Server
   - Validieren Sie den SONAR_TOKEN
   - Überprüfen Sie die Logs im build.sh-Output

2. Quality Gate fehlgeschlagen:
   - Analysieren Sie die Metriken im Dashboard
   - Prüfen Sie die spezifischen Regelverletzungen
   - Implementieren Sie die notwendigen Verbesserungen

3. Coverage-Probleme:
   - Stellen Sie sicher, dass JaCoCo korrekt konfiguriert ist
   - Überprüfen Sie den Test-Report
   - Fügen Sie fehlende Tests hinzu

### Best Practices

1. Code-Qualität:
   - Regelmäßige Analyse durchführen
   - Quality Gates beachten
   - Technische Schuld aktiv managen

2. Team-Integration:
   - Pull Requests mit SonarQube-Analyse
   - Code Reviews mit Qualitätsmetriken
   - Gemeinsame Qualitätsstandards

3. Kontinuierliche Verbesserung:
   - Regelmäßige Überprüfung der Metriken
   - Anpassung der Quality Gates
   - Schulung des Teams

## Development

### Build & Test

```bash
# Maven Build
mvn clean install

# Tests ausführen
mvn test

# JaCoCo Report erstellen
mvn jacoco:report

# Docker Image bauen
docker build -t motion-system .
```

### VS Code Integration

Der Projekt enthält VS Code Konfigurationen für:
- Java Development
- Debugging
- SonarLint Integration
- Test Runner

### Dev Container

Entwicklungsumgebung mit Docker:
```bash
# Dev Container starten
code --remote-containers .
```

## Lizenz

MIT License - siehe [LICENSE](LICENSE) Datei.