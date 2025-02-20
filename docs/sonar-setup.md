# SonarQube Quality Gate Einrichtung

Diese Anleitung beschreibt die Einrichtung des projektspezifischen Quality Gates in SonarQube.

## Quality Gate Konfiguration

Das Quality Gate "Motion System Quality Gate" definiert folgende Mindestanforderungen:

### Code-Abdeckung und Tests
- Mindestens 80% Gesamt-Coverage (Warning bei < 90%)
- 100% erfolgreiche Unit-Tests
- Mindestens 70% Branch-Coverage (Warning bei < 80%)

### Code-Qualität
- Maximale Code-Duplikation: 3% (Warning bei > 2%)
- Maintainability Rating: mindestens B
- Reliability Rating: A (keine Bugs)
- Security Rating: A (keine Vulnerabilities)
- Maximale technische Schuld: 5 Tage

### Komplexität und Struktur
- Maximal 100 Code Smells (Warning bei > 50)
- Maximale Projekt-Komplexität: 500 (Warning bei > 400)
- Mindestens 20% Kommentarzeilen
- Maximale Funktionskomplexität: 10 (Warning bei > 8)

## Einrichtung im SonarQube Server

1. Login als Administrator
   ```
   URL: http://your-sonar-server:9000
   Standard Login: admin/admin
   ```

2. Quality Gate erstellen
   - Navigieren zu: Quality Gates > Create
   - Name eingeben: "Motion System Quality Gate"
   - Conditions aus sonar-quality-gate.yml kopieren

3. Quality Gate dem Projekt zuweisen
   - Projekt auswählen
   - Administration > Quality Gate
   - "Motion System Quality Gate" auswählen
   - Speichern

## Überprüfung der Konfiguration

1. Test-Analyse durchführen
   ```bash
   ./build.sh --sonar
   ```

2. Ergebnisse überprüfen
   - Quality Gate Status im Dashboard
   - Detailansicht der Metriken
   - Warnungen und Fehler

## Fehlerbehebung

### Quality Gate fehlgeschlagen

1. Coverage zu niedrig
   - Tests hinzufügen
   - Coverage-Ausschlüsse prüfen
   - JaCoCo-Konfiguration validieren

2. Code Smells
   - SonarLint in IDE aktivieren
   - Automatische Fixes anwenden
   - Code Review durchführen

3. Komplexität
   - Große Methoden aufteilen
   - Funktionen extrahieren
   - Design Patterns anwenden

## Best Practices

1. Kontinuierliche Überwachung
   - Regelmäßige Analysen
   - Trends beobachten
   - Frühzeitig reagieren

2. Team-Integration
   - Quality Gate in CI/CD
   - Pull Request Analysen
   - Automatische Reviews

3. Dokumentation
   - Änderungen dokumentieren
   - Ausnahmen begründen
   - Metriken nachverfolgen

## Metriken-Details

### Coverage Metrics
- `coverage`: Gesamtabdeckung durch Tests
- `branch_coverage`: Abdeckung von Code-Verzweigungen
- `test_success_density`: Erfolgsrate der Tests

### Code Quality Metrics
- `duplicated_lines_density`: Anteil duplizierter Codezeilen
- `sqale_rating`: Wartbarkeits-Rating (A-E)
- `reliability_rating`: Zuverlässigkeits-Rating (A-E)
- `security_rating`: Sicherheits-Rating (A-E)

### Complexity Metrics
- `complexity`: Zyklomatische Komplexität
- `cognitive_complexity`: Kognitive Komplexität
- `function_complexity`: Durchschnittliche Funktionskomplexität

## Automatisierung

Das Quality Gate wird automatisch in folgenden Szenarien überprüft:

1. Jenkins Pipeline
   ```groovy
   stage('Quality Gate') {
       steps {
           timeout(time: 1, unit: 'HOURS') {
               waitForQualityGate abortPipeline: true
           }
       }
   }
   ```

2. Lokaler Build
   ```bash
   ./build.sh --sonar
   ```

3. Pull Requests
   - Automatische Analyse
   - Status-Check
   - Kommentare zu Problemen

## Anpassung der Schwellenwerte

Die Schwellenwerte können bei Bedarf angepasst werden:

1. Temporäre Anpassung
   - In SonarQube UI
   - Projekt-spezifisch
   - Dokumentation erforderlich

2. Permanente Änderung
   - sonar-quality-gate.yml aktualisieren
   - Team-Review durchführen
   - Änderungen committen