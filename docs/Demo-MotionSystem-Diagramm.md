# Demo-MotionSystem-Diagramm

## Übersicht

Dieses Dokument beschreibt das Layer-basierte Charakter-Motion-System und seine Komponenten. Es enthält Diagramme, die die Struktur und das Verhalten des Systems veranschaulichen, sowie detaillierte Erklärungen der Abläufe.

## Klassendiagramm

```mermaid
classDiagram
    class com.example.motion.demo.LayerSwitchingDemo {
        +main(String[] args)
        +demonstrateLayerSwitching(ICharacterMotionService, UUID, BasicWalkingLayer, AdvancedWalkingLayer)
    }
    class com.example.motion.demo.MotionSystemDemo {
        +main(String[] args)
        +createSampleAnimations(IMotionDataRepository)
        +demonstrateMotionSequence(ICharacterMotionService, UUID)
    }
    class com.example.motion.services.CharacterMotionServiceImpl {
        +addMotionLayer(IMotionLayer, int)
        +removeMotionLayer(IMotionLayer)
        +updateLayerPriority(IMotionLayer, int)
        +getActiveLayers()
        +playAnimation(UUID, String, float)
        +setMovementDirection(UUID, Direction, float)
        +stopMotion(UUID)
        +registerMotionCallback(UUID, MotionCallback)
        +getMotionState(UUID)
    }
    class com.example.motion.services.ICharacterMotionService {
        <<interface>>
        +addMotionLayer(IMotionLayer, int)
        +removeMotionLayer(IMotionLayer)
        +updateLayerPriority(IMotionLayer, int)
        +getActiveLayers()
        +playAnimation(UUID, String, float)
        +setMovementDirection(UUID, Direction, float)
        +stopMotion(UUID)
        +registerMotionCallback(UUID, MotionCallback)
        +getMotionState(UUID)
    }
    class com.example.motion.sys.behavior.AdvancedWalkingLayer {
        +setGaitType(UUID, GaitType)
        +processMotion(UUID, MotionState, float)
        +checkCollision(UUID, MotionState)
        +processPhysics(UUID, PhysicsData)
        +validateMotionState(MotionState)
        +interpolateStates(MotionState, MotionState, float)
    }
    class com.example.motion.sys.behavior.BasicWalkingLayer {
        +processMotion(UUID, MotionState, float)
        +checkCollision(UUID, MotionState)
        +processPhysics(UUID, PhysicsData)
        +validateMotionState(MotionState)
        +interpolateStates(MotionState, MotionState, float)
    }
    class com.example.motion.sys.behavior.IdleLayer {
        +processMotion(UUID, MotionState, float)
        +checkCollision(UUID, MotionState)
        +processPhysics(UUID, PhysicsData)
        +validateMotionState(MotionState)
        +interpolateStates(MotionState, MotionState, float)
    }
    class com.example.motion.sys.behavior.RunningLayer {
        +processMotion(UUID, MotionState, float)
        +checkCollision(UUID, MotionState)
        +processPhysics(UUID, PhysicsData)
        +validateMotionState(MotionState)
        +interpolateStates(MotionState, MotionState, float)
    }
    class com.example.motion.sys.behavior.IMotionLayer {
        <<interface>>
        +processMotion(UUID, MotionState, float)
        +checkCollision(UUID, MotionState)
        +processPhysics(UUID, PhysicsData)
        +validateMotionState(MotionState)
        +interpolateStates(MotionState, MotionState, float)
    }
    class com.example.motion.sys.data.IMotionDataRepository {
        <<interface>>
        +saveMotionState(UUID, MotionState)
        +loadMotionState(UUID)
        +getAnimationData(String)
        +getMotionHistory(UUID, int)
        +cleanupMotionData(UUID, long)
        +saveAnimationData(String, AnimationData)
    }
    class com.example.motion.sys.data.InMemoryMotionDataRepository {
        +saveMotionState(UUID, MotionState)
        +loadMotionState(UUID)
        +getAnimationData(String)
        +getMotionHistory(UUID, int)
        +cleanupMotionData(UUID, long)
        +saveAnimationData(String, AnimationData)
    }
    class com.example.motion.sys.model.AnimationData {
        +interpolateAtTime(float, MotionState)
    }
    class com.example.motion.sys.model.CollisionData
    class com.example.motion.sys.model.Direction {
        +toRotation()
    }
    class com.example.motion.sys.model.MotionState {
        +withPosition(Position)
        +withRotation(Rotation)
        +withSpeed(float)
    }
    class com.example.motion.sys.model.PhysicsData
    class com.example.motion.sys.model.Position
    class com.example.motion.sys.model.Rotation
    class com.example.motion.sys.model.Vector3D {
        +normalize()
        +add(Vector3D)
        +subtract(Vector3D)
        +multiply(float)
        +length()
        +dot(Vector3D)
        +cross(Vector3D)
    }

    com.example.motion.demo.LayerSwitchingDemo --> com.example.motion.services.ICharacterMotionService
    com.example.motion.demo.LayerSwitchingDemo --> com.example.motion.sys.behavior.BasicWalkingLayer
    com.example.motion.demo.LayerSwitchingDemo --> com.example.motion.sys.behavior.AdvancedWalkingLayer
    com.example.motion.demo.MotionSystemDemo --> com.example.motion.services.ICharacterMotionService
    com.example.motion.demo.MotionSystemDemo --> com.example.motion.sys.behavior.IdleLayer
    com.example.motion.demo.MotionSystemDemo --> com.example.motion.sys.behavior.BasicWalkingLayer
    com.example.motion.demo.MotionSystemDemo --> com.example.motion.sys.behavior.RunningLayer
    com.example.motion.services.CharacterMotionServiceImpl --> com.example.motion.sys.behavior.IMotionLayer
    com.example.motion.services.CharacterMotionServiceImpl --> com.example.motion.sys.data.IMotionDataRepository
    com.example.motion.services.CharacterMotionServiceImpl --> com.example.motion.sys.model.MotionState
    com.example.motion.services.CharacterMotionServiceImpl --> com.example.motion.sys.model.PhysicsData
    com.example.motion.services.ICharacterMotionService --> com.example.motion.sys.behavior.IMotionLayer
    com.example.motion.services.ICharacterMotionService --> com.example.motion.sys.data.IMotionDataRepository
    com.example.motion.sys.behavior.AdvancedWalkingLayer --> com.example.motion.sys.model.MotionState
    com.example.motion.sys.behavior.AdvancedWalkingLayer --> com.example.motion.sys.model.PhysicsData
    com.example.motion.sys.behavior.BasicWalkingLayer --> com.example.motion.sys.model.MotionState
    com.example.motion.sys.behavior.BasicWalkingLayer --> com.example.motion.sys.model.PhysicsData
    com.example.motion.sys.behavior.IdleLayer --> com.example.motion.sys.model.MotionState
    com.example.motion.sys.behavior.IdleLayer --> com.example.motion.sys.model.PhysicsData
    com.example.motion.sys.behavior.RunningLayer --> com.example.motion.sys.model.MotionState
    com.example.motion.sys.behavior.RunningLayer --> com.example.motion.sys.model.PhysicsData
    com.example.motion.sys.data.InMemoryMotionDataRepository --> com.example.motion.sys.model.MotionState
    com.example.motion.sys.data.InMemoryMotionDataRepository --> com.example.motion.sys.model.AnimationData
    com.example.motion.sys.model.AnimationData --> com.example.motion.sys.model.MotionState
    com.example.motion.sys.model.Direction --> com.example.motion.sys.model.Vector3D
    com.example.motion.sys.model.MotionState --> com.example.motion.sys.model.Position
    com.example.motion.sys.model.MotionState --> com.example.motion.sys.model.Rotation
    com.example.motion.sys.model.PhysicsData --> com.example.motion.sys.model.Position
    com.example.motion.sys.model.PhysicsData --> com.example.motion.sys.model.Rotation
    com.example.motion.sys.model.PhysicsData --> com.example.motion.sys.model.Vector3D
    com.example.motion.sys.model.Vector3D --> com.example.motion.sys.model.Position
    com.example.motion.sys.model.Vector3D --> com.example.motion.sys.model.Rotation
```

## Ablaufdiagramm

### Initialisierung und Layer-Wechsel

```mermaid
graph TD
    A[Start] --> B[Initialisierung]
    B --> C[Erstellen von BasicWalkingLayer]
    C --> D[Erstellen von AdvancedWalkingLayer]
    D --> E[Erstellen von CharacterMotionServiceImpl]
    E --> F[Registrieren von MotionCallback]
    F --> G[Ausführen der Demo-Sequenz]
    G --> H[Bewegung mit Basic Layer]
    H --> I[Wechsel zu Advanced Layer]
    I --> J[Demonstration verschiedener Gangarten]
    J --> K[Bewegung stoppen]
    K --> L[Zurück zu Basic Layer]
    L --> M[Finale Bewegung]
    M --> N[Ende]
```

### Bewegungssequenz

```mermaid
graph TD
    A[Start] --> B[Idle-Animation abspielen]
    B --> C[Laufbewegung starten]
    C --> D[Walk-Animation mit Bewegung kombinieren]
    D --> E[Richtung ändern]
    E --> F[Bewegung stoppen]
    F --> G[Ende]
```
# Sequenzdiagramme

### LayerSwitchingDemo

```mermaid
sequenceDiagram
    participant Main
    participant Service as CharacterMotionServiceImpl
    participant BasicLayer as BasicWalkingLayer
    participant AdvancedLayer as AdvancedWalkingLayer

    Main->>Service: addMotionLayer(BasicLayer, 1)
    Main->>Service: setMovementDirection(characterId, walkDirection, 0.5f)
    Service->>BasicLayer: processMotion(characterId, currentState, deltaTime)
    Main->>Service: removeMotionLayer(BasicLayer)
    Main->>Service: addMotionLayer(AdvancedLayer, 1)
    Main->>Service: setMovementDirection(characterId, walkDirection, 0.5f)
    Service->>AdvancedLayer: processMotion(characterId, currentState, deltaTime)
    Main->>Service: stopMotion(characterId)
```

### MotionSystemDemo

```mermaid
sequenceDiagram
    participant Main
    participant Service as CharacterMotionServiceImpl
    participant IdleLayer as IdleLayer
    participant WalkingLayer as BasicWalkingLayer
    participant RunningLayer as RunningLayer

    Main->>Service: addMotionLayer(IdleLayer, 1)
    Main->>Service: addMotionLayer(WalkingLayer, 2)
    Main->>Service: addMotionLayer(RunningLayer, 3)
    Main->>Service: playAnimation(characterId, "idle_breathing", 1.0f)
    Service->>IdleLayer: processMotion(characterId, currentState, deltaTime)
    Main->>Service: setMovementDirection(characterId, walkDirection, 1.0f)
    Service->>WalkingLayer: processMotion(characterId, currentState, deltaTime)
    Main->>Service: playAnimation(characterId, "basic_walk", 1.0f)
    Service->>WalkingLayer: processMotion(characterId, currentState, deltaTime)
    Main->>Service: setMovementDirection(characterId, newDirection, 1.0f)
    Service->>RunningLayer: processMotion(characterId, currentState, deltaTime)
    Main->>Service: stopMotion(characterId)
```

## Detaillierte Erklärungen

### Initialisierung

Die Initialisierung des Motion-Systems erfolgt durch das Erstellen der verschiedenen Layer und des `CharacterMotionServiceImpl`. Anschließend wird ein Bewegungs-Callback registriert, um Statusausgaben zu ermöglichen.

### Layer-Wechsel

Der Layer-Wechsel wird durch das Hinzufügen und Entfernen von Motion-Layern im `CharacterMotionServiceImpl` realisiert. Dies ermöglicht es, verschiedene Bewegungsarten dynamisch zu demonstrieren.

### Bewegungssequenz

Die Bewegungssequenz zeigt die verschiedenen Schritte, die ein Charakter durchläuft, einschließlich Idle-Animation, Laufbewegung und Richtungsänderung. Jede dieser Bewegungen wird durch die entsprechenden Layer und Animationen gesteuert.

## Fazit

Das Layer-basierte Charakter-Motion-System bietet eine flexible und erweiterbare Architektur zur Verwaltung von Charakterbewegungen. Durch die Verwendung von Layern und Animationen können verschiedene Bewegungsarten und -sequenzen einfach implementiert und demonstriert werden.
