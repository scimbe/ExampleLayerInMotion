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

- Neue Partikeleffekte können durch Erweiterung der Konfigurationsklassen hinzugefügt werden
- Eigene Fußspuren-Visualisierungen für spezielle Bewegungstypen
- Anpassbare Animationskurven für spezifische Übergänge

## Implementierungsdetails

### Paketstruktur

Die visuellen Erweiterungen sind in folgende Pakete organisiert:

```
com.example.motion.visual/
├── animation/           # Animationskurven und Übergänge
│   ├── EasingFunctions.java
│   └── TransitionAnimator.java
├── footstep/            # Fußspuren-Visualisierung
│   ├── Footstep.java
│   └── FootstepVisualizer.java
├── particle/            # Partikelsystem
│   ├── Particle.java
│   ├── ParticleConfig.java
│   └── ParticleSystem.java
└── VisualEffectsController.java  # Zentrale Steuerung
```

### Frontend-Integration

Die Frontend-Komponenten sind in folgende Dateien organisiert:

```
src/main/resources/
├── static/css/
│   └── visual-effects.css   # CSS-Styles für die Effekte
├── static/js/
│   └── visual-effects.js    # JavaScript-Klasse für die Effekte
```

## Aktivierung und Nutzung

Um die visuellen Erweiterungen zu aktivieren:

1. Spring Boot Konfiguration:
   ```java
   @Configuration
   public class MotionConfig {
       @Bean
       @Primary
       public ICharacterMotionService characterMotionService(
           IMotionDataRepository repository,
           VisualEffectsController visualEffects) {
           return new EnhancedCharacterMotionService(repository, visualEffects);
       }
   }
   ```

2. Frontend-Integration:
   ```html
   <!-- Im <head> der HTML-Datei -->
   <link rel="stylesheet" href="css/visual-effects.css">

   <!-- Vor dem schließenden </body> Tag -->
   <script src="js/visual-effects.js"></script>
   <script>
     // Nach der Initialisierung des Game-Canvas
     const visualEffects = new VisualEffects(gameCanvas);
     
     // Bei Bewegung Effekte erzeugen
     visualEffects.createFootstep(x, y, angle, isLeftFoot, "RunningLayer");
     visualEffects.createDustParticles(x, y, directionAngle, speed, "RunningLayer");
     
     // Bei Zielerfassung
     visualEffects.createGoalReachedEffect(goalX, goalY);
     
     // Bei Layer-Wechsel
     visualEffects.createLayerChangeEffect(x, y, "AdvancedWalkingLayer");
   </script>
   ```

## Beispiele

### Bewegungsübergänge mit Easing

```java
// Smooth Übergang zwischen zwei Zuständen
MotionState startState = getMotionState(characterId);
MotionState targetState = calculateNewState(direction, speed);

visualEffects.animateTransition(
    startState, 
    targetState, 
    state -> updateCharacterState(state),
    () -> onTransitionComplete()
);
```

### Footstep-Erzeugung

```java
// Im Bewegungsupdate
float strideDistance = calculateStrideDistance(layerType, speed);
if (distanceTraveled >= strideDistance) {
    footstepVisualizer.updateFootsteps(state, layerType);
    distanceTraveled = 0;
}
```

### Partikeleffekte

```java
// Bei Zielerfassung
particleSystem.createGoalReachedEmitter(position, 30);

// Bei Layer-Wechsel
particleSystem.updateEmitter(
    transitionEmitterId, 
    state.getPosition(),
    ParticleConfig.createLayerTransitionConfig(newLayerType)
);
```
