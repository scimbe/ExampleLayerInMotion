# GitHub Actions CI/CD-Workflow

Diese Dokumentation beschreibt den automatisierten CI/CD-Workflow für das Character Motion System.

## Workflow-Übersicht

Der Workflow wird ausgelöst durch:
- Push auf den `main` Branch
- Pull Requests gegen den `main` Branch
- Manuelle Auslösung (workflow_dispatch)

## Jobs

### 1. Build und Test
- Checkout des Codes
- Java 17 Setup
- Maven Build
- Ausführung der Tests
- JaCoCo Coverage Report
- SonarQube Analyse
- Erstellung des JAR-Pakets

### 2. Docker Build
- Nur für `main` Branch
- Verwendet das Build-Artefakt
- Multi-Stage Build
- Push zu Docker Hub
- Build-Cache Optimierung

### 3. Deployment
- Nur für `main` Branch
- Production Environment
- Deployment-Verifizierung
- Health Checks

### 4. Benachrichtigungen
- Status-E-Mails
- Issue-Erstellung bei Fehlern
- Pipeline-Status-Updates

## Konfiguration

### Erforderliche Secrets

```yaml
# GitHub Secrets
SONAR_HOST_URL: "http://your-sonar-server:9000"
SONAR_TOKEN: "your-sonar-token"
DOCKERHUB_USERNAME: "your-dockerhub-username"
DOCKERHUB_TOKEN: "your-dockerhub-token"
SMTP_SERVER: "smtp.example.com"
SMTP_PORT: "587"
SMTP_USERNAME: "notifications@example.com"
SMTP_PASSWORD: "your-smtp-password"
NOTIFICATION_EMAIL: "team@example.com"
```

### Environment-Variablen

```yaml
JAVA_VERSION: '17'
MAVEN_VERSION: '3.9.4'
DOCKER_IMAGE: 'motion-system'
DOCKER_TAG: ${GITHUB_SHA}
```

## Workflow-Details

### Build-Optimierungen

1. Caching
   - Maven Dependencies
   - SonarQube Cache
   - Docker Layer Cache

2. Parallelisierung
   - Unabhängige Jobs laufen parallel
   - Optimierte Build-Matrix

3. Ressourcen-Management
   - Artifact Retention
   - Cache Cleanup
   - Build Timeout

### Qualitätssicherung

1. Code-Qualität
   - SonarQube Integration
   - Coverage Reports
   - Quality Gates

2. Tests
   - Unit Tests
   - Integration Tests
   - Coverage Checks

3. Security
   - SAST Scans
   - Dependency Checks
   - Docker Image Scans

### Deployment-Strategie

1. Production Deployment
   - Automatisch für `main`
   - Health Checks
   - Rollback-Möglichkeit

2. Umgebungen
   - Production
   - (Erweiterbar für Staging/QA)

3. Verifizierung
   - Post-Deployment Tests
   - Monitoring
   - Alerts

## Troubleshooting

### Häufige Probleme

1. Build Fehler
   ```bash
   # Maven Build neu starten
   mvn clean install
   ```

2. SonarQube Fehler
   - Token überprüfen
   - Server-Erreichbarkeit testen
   - Quality Gates prüfen

3. Docker Build
   - Cache löschen
   - Build neu starten
   - Logs überprüfen

### Workflow Debugging

1. Action Runner
   ```bash
   # Debug Logs aktivieren
   echo "ACTIONS_RUNNER_DEBUG=true" >> $GITHUB_ENV
   ```

2. Job Ausführung
   - Workflow neu starten
   - Einzelne Jobs ausführen
   - Logs analysieren

## Best Practices

1. Commits
   - Aussagekräftige Commit-Messages
   - Feature Branches
   - Pull Request Reviews

2. Pipeline
   - Regelmäßige Updates
   - Security Patches
   - Performance Monitoring

3. Dokumentation
   - Änderungen dokumentieren
   - Konfiguration aktuell halten
   - Troubleshooting Guide pflegen

## Wartung

### Regelmäßige Tasks

1. Wöchentlich
   - Log Review
   - Performance Check
   - Security Updates

2. Monatlich
   - Dependency Updates
   - Cache Cleanup
   - Konfigurationsreview

3. Quartalsweise
   - Pipeline Optimierung
   - Ressourcen-Audit
   - Dokumentations-Update

### Monitoring

1. Metriken
   - Build-Dauer
   - Erfolgsrate
   - Resource Usage

2. Alerts
   - Build Failures
   - Deployment Issues
   - Security Alerts

3. Reporting
   - Success Rate
   - Performance Trends
   - Coverage Trends