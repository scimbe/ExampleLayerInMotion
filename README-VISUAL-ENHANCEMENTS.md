# Visuelle Erweiterungen für das Motion-System

Diese Erweiterung fügt erweiterte visuelle Effekte zum Layer-basierten Charakter-Motion-System hinzu, um die Bewegungen lebendiger und nachvollziehbarer zu gestalten.

## Hauptkomponenten

### 1. Animationskurven

Die Implementierung von Easing-Funktionen ermöglicht weichere und natürlichere Übergänge zwischen verschiedenen Bewegungszuständen:

- **TransitionAnimator**: Führt interpolierte Übergänge zwischen Bewegungszuständen durch
- **EasingFunctions**: Stellt verschiedene Interpolationskurven für organische Bewegungen bereit
- Automatische Auswahl der passenden Kurve basierend auf dem Bewegungstyp (z.B. Beschleunigung, Abbremsung)

### 2. Footstep-Visualisierung

Dynamische Fußspuren machen die Bewegung des Charakters visuell nachvollziehbar:

- Anpassung der Fußspuren an den aktiven Bewegungslayer (Größe, Form, Farbe)
- Automatische Generierung und zeitliches Verblassen
- Links-/Rechts-Alternierung für realistischen Gang

### 3. Partikelsystem

Ein leichtgewichtiges Partikelsystem für reaktive visuelle Effekte:

- **Staubeffekte** bei Bewegungen
- **Glitzereffekte** bei Zielerfassung
- **Layer-Wechsel-Partikel** für visuelle Rückmeldung beim Wechsel der Bewegungsart

## Integration

Die visuellen Erweiterungen sind über den `EnhancedCharacterMotionService` nahtlos in das bestehende System integriert:

- Dekoriert den Standard-`CharacterMotionService`
- Reagiert automatisch auf Bewegungsänderungen und Layer-Wechsel
- Kein Eingriff in die Kern-Funktionalität erforderlich

## Frontend-Integration

Die Erweiterungen werden auch im Frontend-Interface unterstützt:

- JavaScript-Klasse `VisualEffects` für Client-seitige Darstellung
- CSS-Styles für Partikel und Fußspuren
- Konfigurationsmöglichkeit für Endbenutzer

## Konfiguration

Die visuellen Effekte können granular konfiguriert werden:

- Aktivieren/Deaktivieren einzelner Effekte (Fußspuren, Partikel)
- Anpassung der Partikeleigenschaften pro Layer-Typ
- Einstellung der Lebensdauer und Opazität

## Leistungsoptimierung

Alle visuellen Effekte sind leistungsoptimiert:

- Automatische Begrenzung der Anzahl von Partikeln und Fußspuren
- Level-of-Detail-Anpassung basierend auf Performance
- Effiziente Rendering-Techniken

## Erweiterbarkeit

Das System ist modular aufgebaut und einfach erweiterbar:

- Neue Partikeleffekte können einfach hinzugefügt werden
- Anpassbare Animations-Kurven
- Erweiterbare Fußspuren-Visualisierung

## Verwendung

### Backend

```java
// Einfache Integration durch Dependency Injection
@Autowired
private VisualEffectsController visualEffects;

// Manuelles Erzeugen von Effekten
visualEffects.createGoalReachedEffect(characterId, position);

// Aktivieren/Deaktivieren von Effekten
visualEffects.setEnabled(true);
```

### Frontend

```javascript
// Initialisieren der Effekte
const visualEffects = new VisualEffects(gameCanvas);

// Erstellen von Fußspuren
visualEffects.createFootstep(x, y, angle, isLeftFoot, 'RunningLayer');

// Erstellen von Partikeleffekten
visualEffects.createDustParticles(x, y, directionAngle, speed, 'RunningLayer', 5);
visualEffects.createGoalReachedEffect(x, y, 30);
visualEffects.createLayerChangeEffect(x, y, 'AdvancedWalkingLayer', 20);
```

## Beispiele

### Layer-Wechsel mit visuellen Effekten

Beim Wechsel zwischen verschiedenen Bewegungslayern (z.B. von `BasicWalkingLayer` zu `RunningLayer`) werden automatisch passende visuelle Effekte generiert:

1. Animierte Übergänge für weiche Beschleunigung
2. Passende Fußspuren für die jeweilige Gangart
3. Layer-Wechsel-Partikeleffekte für sofortiges visuelles Feedback

### Zielerfassung mit visuellen Effekten

Wenn ein Charakter ein Ziel erreicht, wird ein visueller Effekt erzeugt:

1. Goldene Glitzerpartikel zur Hervorhebung
2. Aufleuchtender Effekt
3. Dynamische Bewegung der Partikel

## Installation

Die visuellen Erweiterungen sind vollständig in das bestehende System integriert. Nach dem Merge dieses Branches stehen die Funktionen automatisch zur Verfügung.

## Kompatibilität

Die Erweiterungen wurden für optimale Kompatibilität konzipiert:

- Vollständige Unterstützung für alle existierenden Layer-Typen
- Fallback-Mechanismen für neue oder unbekannte Layer-Typen
- Funktioniert sowohl im Online- als auch im Offline-Modus

## Technische Details

- Java-Backend mit Spring-Integration
- Canvas-basiertes Frontend-Rendering
- Optimierte Animations-Berechnungen
