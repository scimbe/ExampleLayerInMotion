# Character Motion System

Ein Layer-basiertes System zur Verwaltung und Steuerung von Charakterbewegungen in verteilten Systemen.

<div align="center">
  <!-- Hier könnte ein Logo eingefügt werden -->
  
  [![Build Status](https://img.shields.io/badge/build-passing-brightgreen)]()
  [![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://adoptium.net/)
  [![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.2-green)](https://spring.io/projects/spring-boot)
  [![License](https://img.shields.io/badge/License-MIT-yellow.svg)](#lizenz)
</div>

## 🚀 Schnellstart

```bash
# Repository klonen
git clone https://github.com/scimbe/ExampleLayerInMotion.git
cd ExampleLayerInMotion

# Build ausführen
chmod +x build.sh
./build.sh --dev  # Build und direkt starten

# Alternative: Nur Build
./build.sh --build --package

# Alternative: Mit Docker
./build.sh --docker-run
```

## 📋 Inhaltsverzeichnis

- [Projektübersicht](#projektübersicht)
- [Architektur](#architektur)
- [Systemvoraussetzungen](#systemvoraussetzungen)
- [Installation und Build](#installation-und-build)
- [Anwendung starten](#anwendung-starten)
- [Features testen](#features-testen)
- [Entwicklung](#entwicklung)
- [Qualitätssicherung](#qualitätssicherung)
- [Fehlerbehebung](#fehlerbehebung)
- [Lizenz](#lizenz)

## 📖 Projektübersicht

Das Character Motion System ist eine Plattform zur Verwaltung und Steuerung von Charakterbewegungen in virtuellen Umgebungen. Es ist speziell dafür konzipiert, realistische und anpassbare Bewegungen zu ermöglichen, die über verschiedene Layer definiert werden können.

**Hauptmerkmale:**

- 🏗️ Layer-basierte Architektur für flexible und kombinierbare Bewegungsmuster
- 🔌 REST-API und WebSocket-Schnittstellen für Echtzeit-Steuerung
- 🎮 Web-basierte Demo-Anwendung zur visuellen Darstellung
- 💾 InMemory-Datenspeicherung mit Repository-Struktur
- 📊 Testabdeckung und Qualitätsmetriken

## 🏛️ Architektur

Das Character Motion System implementiert eine dreischichtige Architektur:

1. **Motion-API-Schicht (Präsentationsschicht)**
   - REST-API und WebSocket-Endpunkte
   - Client-seitige Webanwendung
   - Transformationslogik für externe Kommunikation

2. **Motion-Logik-Schicht (Geschäftslogik)**
   - Layer-Management für verschiedene Bewegungstypen
   - Bewegungs- und Physikberechnung
   - Event-Handling und State-Management

3. **Motion-Daten-Schicht (Persistenzschicht)**
   - Speicherung von Bewegungszuständen und Animationen
   - InMemory-Repository (erweiterbar für externe Datenbanken)
   - Historie und Versionierung

### Detailliertes Architekturdiagramm

Weitere Details zur Architektur finden Sie in der [CONCEPT.md](CONCEPT.md) Datei.

## 💻 Systemvoraussetzungen

- **Java**: JDK 17 oder höher (für JaCoCo 0.8.12 empfohlen: JDK 17-21)
- **Maven**: 3.9.4 oder höher
- **Docker**: Optional, für Container-basierte Ausführung
- **Betriebssystem**: Linux, macOS oder Windows mit Bash-Unterstützung
- **Hardware**: Mindestens 4GB RAM, 2GB freier Festplattenspeicher

## 🔧 Installation und Build

### Quellcode beziehen

```bash
# Repository klonen
git clone https://github.com/scimbe/ExampleLayerInMotion.git
cd ExampleLayerInMotion

# Ausführungsrechte für Skripte setzen
chmod +x build.sh
chmod +x setup-java-env.sh
```

### Build-Optionen

Das Projekt verfügt über ein flexibles Build-Skript (`build.sh`), das verschiedene Optionen bietet:

```bash
# Hilfe anzeigen
./build.sh --help

# Vollständiger Build mit Tests
./build.sh --build

# JAR-Paket erstellen
./build.sh --package

# Nur Tests ausführen
./build.sh --test

# Bereinigen und neu bauen
./build.sh --clean --build

# Docker-Image erstellen
./build.sh --docker

# Anwendung im Docker-Container starten
./build.sh --docker-run

# Entwicklermodus: Clean, Build, Package und direkt starten
./build.sh --dev
```

### Maven direkt verwenden

Alternativ können Sie Maven direkt verwenden:

```bash
# Kompilieren
mvn clean compile

# Tests ausführen
mvn test

# JAR-Paket erstellen
mvn clean package

# Spring Boot Anwendung starten
mvn spring-boot:run

# Mit spezifischem Profil (z.B. dev)
mvn spring-boot:run -Dspring.profiles.active=dev
```

## 🚀 Anwendung starten

### Mit dem Build-Skript

```bash
# Anwendung bauen und starten
./build.sh --dev

# Alternativ: Nur starten (wenn bereits gebaut)
./build.sh --run
```

### Mit Java direkt

```bash
# Nach dem Build
java -jar target/motion-system-1.0-SNAPSHOT.jar
```

### Mit Docker

```bash
# Docker-Image erstellen und Container starten
./build.sh --docker-run

# Alternativ: Manuell Docker-Container starten
docker run -p 8080:8080 motion-system:latest
```

## 🧪 Features testen

Nach dem Start der Anwendung können Sie die folgenden Features über Ihren Webbrowser testen:

### Webbasierte Demo-Oberfläche

```
http://localhost:8080/
```
Die Hauptseite zeigt eine interaktive Animation des Charakters und bietet Steuerelemente zur Bewegung und zum Wechseln zwischen verschiedenen Bewegungslayern.

### Animierte Oberfläche

```
http://localhost:8080/animated-surface
```
Eine spezifische Visualisierung, die Bewegung, Layer-Wechsel und Koordinaten auf einem Gitter anzeigt.

### Motion Behavior Demo

```
http://localhost:8080/demo-motion
```
Eine technische Demo, die verschiedene Bewegungsverhalten zeigt und deren Erfolgreiche Ausführung bestätigt.

### Aktuelle Position

```
http://localhost:8080/current-position
```
Zeigt die aktuelle Position und Geschwindigkeit des Charakters in einem Gitterformat an.

### API-Dokumentation

```
http://localhost:8080/swagger-ui
```
Vollständige API-Dokumentation mit Swagger UI, über die Sie die API-Endpunkte direkt testen können.

## 💻 Entwicklung

### IDE-Unterstützung

Das Projekt enthält Konfigurationsdateien für Visual Studio Code. Diese erleichtern die Entwicklung mit:

- Integrierte Java-Entwicklungstools
- Debugging-Konfiguration
- SonarLint-Integration
- Test-Runner-Konfiguration

Um die VS Code-Integration zu nutzen:

1. Öffnen Sie VS Code
2. Installieren Sie die empfohlenen Erweiterungen:
   - Java Extension Pack
   - Spring Boot Extension Pack
   - SonarLint
   - Test Runner for Java

### Dev Container

Für eine konsistente Entwicklungsumgebung wird ein Dev Container bereitgestellt:

```bash
# VS Code mit installierter Remote Development Extension öffnen
code .

# ODER wenn das Kommando 'code' nicht im PATH ist:
# In VS Code: Cmd + Shift + P → "Remote-Containers: Open Folder in Container"
```

Installation des 'code'-Kommandos in VS Code:
1. Öffnen Sie VS Code
2. Drücken Sie `Cmd + Shift + P` (macOS) oder `Ctrl + Shift + P` (Windows/Linux)
3. Tippen Sie "Shell Command: Install 'code' command in PATH"
4. Bestätigen Sie mit Enter

### Projekt-Struktur

```
ExampleLayerInMotion/
├── src/
│   ├── main/
│   │   ├── java/com/example/motion/
│   │   │   ├── api/           # REST API Controller
│   │   │   ├── config/        # Spring Konfiguration
│   │   │   ├── interfaces/    # Öffentliche Interfaces
│   │   │   ├── services/      # Service-Implementierungen
│   │   │   ├── sys/
│   │   │   │   ├── behavior/  # Motion Layer Implementierungen
│   │   │   │   ├── data/      # Daten-Repositories
│   │   │   │   └── model/     # Datenmodelle
│   │   │   ├── web/           # Web-Controller
│   │   │   └── websocket/     # WebSocket-Handler
│   │   └── resources/
│   │       ├── static/        # Webseiten und Client-Code
│   │       └── application.properties
│   └── test/                  # Testklassen
├── .devcontainer/             # Dev Container Konfiguration
├── .github/                   # GitHub Workflows
├── .vscode/                   # VS Code Konfiguration
├── docs/                      # Dokumentation
├── build.sh                   # Build-Skript
├── setup-java-env.sh          # Java-Umgebungssetup
├── pom.xml                    # Maven-Konfiguration
└── README.md                  # Diese Datei
```

### Debugging

VS Code bietet vorkonfigurierte Launch-Konfigurationen:

1. `Run MotionSystemDemo`: Führt die Demo-Anwendung aus
2. `Run LayerSwitchingDemo`: Führt die Layer-Wechsel-Demo aus
3. `Debug Current File`: Debuggt die aktuell geöffnete Datei
4. `Run All Tests`: Führt alle Tests aus
5. `Debug Current Test`: Debuggt den aktuellen Test

Für das Debugging in der Kommandozeile:

```bash
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
```

Dann können Sie den Remote-Debugger Ihrer IDE mit Port 5005 verbinden.

## 📊 Qualitätssicherung

### Testen

```bash
# Alle Tests ausführen
./build.sh --test

# JaCoCo-Report generieren
./build.sh --jacoco
```

Der JaCoCo-Report wird in `target/site/jacoco/index.html` erstellt.

### SonarQube-Integration

#### Lokale Einrichtung

1. SonarQube-Server starten:
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

#### Analyse ausführen

```bash
# SonarQube-Analyse durchführen
./build.sh --sonar

# Oder vollständiger Build mit Analyse
./build.sh --all-sonar
```

### CI/CD-Pipeline

Das Projekt enthält eine vorkonfigurierte GitHub-Actions-Pipeline (`.github/workflows/ci-cd.yml`), die automatisch folgende Schritte ausführt:

1. Build und Tests
2. JaCoCo-Report
3. SonarQube-Analyse
4. Docker-Image-Erstellung
5. Container-Tests

Für Jenkins ist eine Jenkinsfile-Konfiguration verfügbar.

## 🔧 Fehlerbehebung

### Häufige Probleme

#### JaCoCo-Instrumentierungsfehler mit Java 23+

**Problem:** Fehlermeldung `Unsupported class file major version 67`

**Lösung:**
```bash
# Verwenden Sie Java 17-21 oder
./build.sh --test -P skip-jacoco
```

#### Spring Bean-Konflikt

**Problem:** Anwendung startet nicht wegen doppelter Bean-Definitionen

**Lösung:** Stellen Sie sicher, dass Bean-Definitionen eindeutig sind oder verwenden Sie `@Primary`/`@Qualifier`.

#### Docker-Probleme

**Problem:** Docker-Build oder -Run schlägt fehl

**Lösung:**
```bash
# Docker-Dienst prüfen
sudo systemctl status docker

# Docker ohne sudo verwenden
sudo usermod -aG docker $USER
newgrp docker
```

### Logs anzeigen

```bash
# Spring Boot-Logs
java -jar target/motion-system-1.0-SNAPSHOT.jar --debug

# Docker-Container-Logs
docker logs motion-system-app
```

### Support erhalten

Bei Problemen können Sie:

1. Ein GitHub Issue öffnen
2. Die Dokumentation unter `/docs` konsultieren
3. Das [Fehlerbehebungs-Wiki](https://github.com/scimbe/ExampleLayerInMotion/wiki/Troubleshooting) (falls vorhanden) besuchen

## 📄 Lizenz

MIT License - siehe [LICENSE](LICENSE) Datei.

---

&copy; 2025 Character Motion System Team
